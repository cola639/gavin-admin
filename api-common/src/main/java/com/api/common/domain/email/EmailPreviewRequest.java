package com.api.common.domain.email;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class EmailPreviewRequest {

    @NotBlank
    private String template;

    private EmailBodyMode bodyMode = EmailBodyMode.BOTH;

    private Map<String, Object> variables = new HashMap<>();
}
