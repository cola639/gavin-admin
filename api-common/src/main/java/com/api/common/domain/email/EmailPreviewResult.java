package com.api.common.domain.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailPreviewResult {
    private String html;
    private String text;
}
