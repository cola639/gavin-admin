package com.api.system.service.email;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailTemplateTestDataFactory {

    public Map<String, Object> sampleVariables(String template) {
        var vars = new HashMap<String, Object>();
        vars.put("now", OffsetDateTime.now().toString());

        switch (template) {
            case "test/welcome" -> {
                vars.put("userName", "Gavin");
                vars.put("actionUrl", "https://example.com/welcome");
                vars.put("supportEmail", "support@gogogavin.uk");
            }
            case "test/password-reset" -> {
                vars.put("userName", "Gavin");
                vars.put("code", "123456");
                vars.put("expireMinutes", 10);
                vars.put("supportEmail", "support@gogogavin.uk");
            }
            default -> log.warn("No predefined sample variables for template={}", template);
        }

        return vars;
    }
}
