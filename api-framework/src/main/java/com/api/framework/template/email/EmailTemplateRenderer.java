package com.api.framework.template.email;

import com.api.framework.template.email.model.RenderedEmailTemplate;
import java.util.Map;

public interface EmailTemplateRenderer {
    RenderedEmailTemplate render(String templateName, Map<String, Object> variables);
}
