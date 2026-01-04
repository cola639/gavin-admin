package com.api.framework.client.email.impl;

import com.api.framework.client.email.EmailSender;
import com.api.framework.client.email.model.EmailSendCommand;
import com.api.framework.client.email.model.EmailSendResponse;
import com.api.framework.config.email.EmailProperties;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Slf4j
@RequiredArgsConstructor
public class SesEmailSender implements EmailSender {

    private final SesV2Client client;
    private final EmailProperties props;

    @Override
    public EmailSendResponse send(EmailSendCommand cmd) {
        Objects.requireNonNull(cmd, "EmailSendCommand must not be null");

        var subject = Content.builder().data(cmd.getSubject()).charset("UTF-8").build();

        var bodyBuilder = Body.builder();

        if (cmd.getHtmlBody() != null && !cmd.getHtmlBody().isBlank()) {
            bodyBuilder.html(Content.builder().data(cmd.getHtmlBody()).charset("UTF-8").build());
        }
        if (cmd.getTextBody() != null && !cmd.getTextBody().isBlank()) {
            bodyBuilder.text(Content.builder().data(cmd.getTextBody()).charset("UTF-8").build());
        }

        var message = Message.builder()
                .subject(subject)
                .body(bodyBuilder.build())
                .build();

        var requestBuilder = SendEmailRequest.builder()
                .fromEmailAddress(cmd.getFrom())
                .destination(Destination.builder().toAddresses(cmd.getToAddresses()).build())
                .content(EmailContent.builder().simple(message).build());

        var configSet = cmd.getConfigurationSetName();
        if (configSet != null && !configSet.isBlank()) {
            requestBuilder.configurationSetName(configSet.trim());
        }

        log.info("Email: sending via SES, toCount={}, subject={}", cmd.getToAddresses().size(), cmd.getSubject());

        var resp = client.sendEmail(requestBuilder.build());
        return EmailSendResponse.builder()
                .messageId(resp.messageId())
                .acceptedCount(cmd.getToAddresses().size())
                .build();
    }
}
