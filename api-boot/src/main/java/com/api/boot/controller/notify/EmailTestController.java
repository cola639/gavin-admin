package com.api.boot.controller.notify;

import com.api.common.domain.AjaxResult;
import com.api.common.domain.email.BatchEmailSendRequest;
import com.api.common.domain.email.EmailPreviewRequest;
import com.api.system.service.email.EmailAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/test/email")
public class EmailTestController {

    private final EmailAppService emailAppService;

    /**
     * Preview HTML in browser.
     * Example: /test/email/preview/html?template=test/welcome
     */
    @GetMapping(value = "/preview/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewHtml(@RequestParam String template) {
        var req = new EmailPreviewRequest();
        req.setTemplate(template);
        req.setBodyMode(com.api.common.domain.email.EmailBodyMode.HTML_ONLY);

        var result = emailAppService.preview(req);
        return ResponseEntity.ok(result.getHtml() == null ? "<!-- no html template -->" : result.getHtml());
    }

    /**
     * Preview text.
     * Example: /test/email/preview/text?template=test/welcome
     */
    @GetMapping(value = "/preview/text", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> previewText(@RequestParam String template) {
        var req = new EmailPreviewRequest();
        req.setTemplate(template);
        req.setBodyMode(com.api.common.domain.email.EmailBodyMode.TEXT_ONLY);

        var result = emailAppService.preview(req);
        return ResponseEntity.ok(result.getText() == null ? "no text template" : result.getText());
    }

    /**
     * JSON preview (returns both html/text if exists).
     */
    @PostMapping("/preview")
    public AjaxResult preview(@Valid @RequestBody EmailPreviewRequest request) {
        return AjaxResult.success(emailAppService.preview(request));
    }

    /**
     * Send test email (batch).
     */
    @PostMapping("/send")
    public AjaxResult send(@Valid @RequestBody BatchEmailSendRequest request) {
        var result = emailAppService.sendBatch(request);
        return AjaxResult.success(result);
    }
}
