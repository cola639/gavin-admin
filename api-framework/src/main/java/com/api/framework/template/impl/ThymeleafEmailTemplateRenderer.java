package com.api.framework.template.impl;

import com.api.framework.template.email.EmailTemplateRenderer;
import com.api.framework.template.email.model.RenderedEmailTemplate;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

@Slf4j
@RequiredArgsConstructor
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRenderer {

    private final TemplateEngine htmlEngine;
    private final TemplateEngine textEngine;

    @Override
    public RenderedEmailTemplate render(String templateName, Map<String, Object> variables) {
        var ctx = new Context(Locale.getDefault());
        ctx.setVariables(variables);

        var html = tryRender(htmlEngine, templateName, ctx, "HTML");
        var text = tryRender(textEngine, templateName, ctx, "TEXT");

        return RenderedEmailTemplate.builder()
                .html(html)
                .text(text)
                .build();
    }

    private String tryRender(TemplateEngine engine, String templateName, Context ctx, String type) {
        try {
            return engine.process(templateName, ctx);
        } catch (TemplateInputException ex) {
            log.warn("Email template not found for type={}, template={}", type, templateName);
            return null;
        } catch (Exception ex) {
            log.error("Email template render failed, type={}, template={}", type, templateName, ex);
            throw ex;
        }
    }
}
