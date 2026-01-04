package com.api.common.domain.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class BatchEmailSendRequest {

    @NotEmpty
    private List<@Email String> recipients;

    @NotBlank
    private String subject;

    @NotBlank
    private String template;

    private EmailBodyMode bodyMode = EmailBodyMode.BOTH;

    private Map<String, Object> variables = new HashMap<>();
}
