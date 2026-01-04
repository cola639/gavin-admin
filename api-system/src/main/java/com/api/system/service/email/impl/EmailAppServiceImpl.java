package com.api.system.service.email.impl;

import com.api.common.domain.email.*;
import com.api.framework.client.email.EmailSender;
import com.api.framework.client.email.model.EmailSendCommand;
import com.api.framework.client.email.model.EmailSendResponse;
import com.api.framework.config.email.EmailProperties;
import com.api.framework.exception.ServiceException;
import com.api.framework.template.email.EmailTemplateRenderer;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailAppServiceImpl implements com.api.system.service.email.EmailAppService {

    /**
     * SES Destination recipient limit (safe default).
     */
    private static final int SES_MAX_RECIPIENTS_PER_CALL = 50;

    private final EmailProperties props;
    private final EmailSender emailSender;
    private final EmailTemplateRenderer templateRenderer;
    private final com.api.system.service.email.EmailTemplateTestDataFactory testDataFactory;

    @Value("${app.name:App}")
    private String appName;

    @Value("${app.copyrightYear:2025}")
    private String copyrightYear;

    @Override
    public EmailPreviewResult preview(EmailPreviewRequest request) {
        var template = safeTrim(request.getTemplate());
        if (template.isEmpty()) {
            throw new ServiceException("Email template must not be blank.");
        }

        var vars = mergeDefaultVars(request.getVariables(), template);

        var rendered = templateRenderer.render(template, vars);
        var mode = request.getBodyMode() == null ? EmailBodyMode.BOTH : request.getBodyMode();

        return EmailPreviewResult.builder()
                .html(mode == EmailBodyMode.TEXT_ONLY ? null : rendered.getHtml())
                .text(mode == EmailBodyMode.HTML_ONLY ? null : rendered.getText())
                .build();
    }

    @Override
    public BatchEmailSendResult sendBatch(BatchEmailSendRequest request) {
        var requestId = UUID.randomUUID().toString();
        var subject = safeTrim(request.getSubject());
        var template = safeTrim(request.getTemplate());
        var mode = request.getBodyMode() == null ? EmailBodyMode.BOTH : request.getBodyMode();

        if (subject.isEmpty()) {
            throw new ServiceException("Email subject must not be blank.");
        }
        if (template.isEmpty()) {
            throw new ServiceException("Email template must not be blank.");
        }
        if (props.getFrom() == null || props.getFrom().isBlank()) {
            throw new ServiceException("Email 'from' address is not configured (app.email.from).");
        }

        var recipients = normalizeRecipients(request.getRecipients());
        if (recipients.isEmpty()) {
            throw new ServiceException("Recipients must not be empty.");
        }

        var vars = mergeDefaultVars(request.getVariables(), template);
        var rendered = templateRenderer.render(template, vars);

        var html = (mode == EmailBodyMode.TEXT_ONLY) ? null : rendered.getHtml();
        var text = (mode == EmailBodyMode.HTML_ONLY) ? null : rendered.getText();

        if ((html == null || html.isBlank()) && (text == null || text.isBlank())) {
            throw new ServiceException("No email body rendered. Please check template files (.html/.txt).");
        }
        if (mode == EmailBodyMode.HTML_ONLY && (html == null || html.isBlank())) {
            throw new ServiceException("HTML_ONLY requested but HTML template not found.");
        }
        if (mode == EmailBodyMode.TEXT_ONLY && (text == null || text.isBlank())) {
            throw new ServiceException("TEXT_ONLY requested but TEXT template not found.");
        }

        var perCallLimit = Math.min(props.getMaxBatchSize(), SES_MAX_RECIPIENTS_PER_CALL);
        if (perCallLimit <= 0) {
            throw new ServiceException("Invalid maxBatchSize configuration.");
        }

        log.info("Email: requestId={}, totalRecipients={}, subject={}, template={}, mode={}, perCallLimit={}",
                requestId, recipients.size(), subject, template, mode, perCallLimit);

        var results = new ArrayList<EmailSendItemResult>();
        var batches = chunk(recipients, perCallLimit);

        for (var batch : batches) {
            var cmd = EmailSendCommand.builder()
                    .from(props.getFrom())
                    .toAddresses(batch)
                    .subject(subject)
                    .htmlBody(html)
                    .textBody(text)
                    .configurationSetName(props.getSes().getConfigurationSet())
                    .build();

            try {
                var resp = sendWithRetry(cmd);
                batch.forEach(mail -> results.add(EmailSendItemResult.builder()
                        .email(mail)
                        .success(true)
                        .messageId(resp.getMessageId())
                        .build()));
            } catch (Exception ex) {
                log.error("Email: batch send failed, requestId={}, batchSize={}", requestId, batch.size(), ex);
                batch.forEach(mail -> results.add(EmailSendItemResult.builder()
                        .email(mail)
                        .success(false)
                        .error(ex.getMessage())
                        .build()));
            }
        }

        var success = (int) results.stream().filter(EmailSendItemResult::isSuccess).count();
        var failed = results.size() - success;

        return BatchEmailSendResult.builder()
                .requestId(requestId)
                .total(results.size())
                .success(success)
                .failed(failed)
                .items(results)
                .build();
    }

    private EmailSendResponse sendWithRetry(EmailSendCommand cmd) {
        var maxRetries = Math.max(0, props.getMaxRetries());
        var attempt = 0;

        while (true) {
            try {
                attempt++;
                log.info("Email: sending attempt={}, toCount={}", attempt, cmd.getToAddresses().size());
                return emailSender.send(cmd);
            } catch (SesV2Exception ex) {
                var status = ex.statusCode();
                var retryable = (status == 429) || (status >= 500);

                if (!retryable || attempt > maxRetries) {
                    throw ex;
                }

                var sleepMs = backoffMs(attempt);
                log.warn("Email: transient SES error, status={}, attempt={}, sleepMs={}, message={}",
                        status, attempt, sleepMs, ex.getMessage());

                sleepQuietly(sleepMs);
            }
        }
    }

    private long backoffMs(int attempt) {
        var base = 300L * (1L << Math.min(attempt, 6)); // capped growth
        var jitter = ThreadLocalRandom.current().nextLong(0, 200);
        return Math.min(base + jitter, 5_000L);
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, Object> mergeDefaultVars(Map<String, Object> input, String template) {
        var vars = new HashMap<String, Object>();

        if (input != null) {
            vars.putAll(input);
        }

        // If user did not pass variables, use sample test data for preview/layout validation
        if (vars.isEmpty()) {
            vars.putAll(testDataFactory.sampleVariables(template));
        }

        vars.putIfAbsent("appName", appName);
        vars.putIfAbsent("copyrightYear", copyrightYear);

        return vars;
    }

    private List<String> normalizeRecipients(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        // keep order, remove duplicates, trim blanks
        return raw.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private List<List<String>> chunk(List<String> list, int size) {
        var chunks = new ArrayList<List<String>>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return chunks;
    }

    private String safeTrim(String v) {
        return v == null ? "" : v.trim();
    }
}
