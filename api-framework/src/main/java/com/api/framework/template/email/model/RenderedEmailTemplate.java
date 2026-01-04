package com.api.framework.template.email.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RenderedEmailTemplate {
    private String html;
    private String text;
}
