package com.api.common.domain.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalizedEmailSendRequest {

  @NotBlank @Email private String recipient;

  @NotBlank private String subject;

  @NotBlank private String template;

  /** Default BOTH for better compatibility (HTML + text fallback). */
  private EmailBodyMode bodyMode;

  private Map<String, Object> variables;
}
