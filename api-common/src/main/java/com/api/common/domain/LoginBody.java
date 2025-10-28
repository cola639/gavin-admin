package com.api.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing user login request payload.
 *
 * <p>Contains: - username - password - captcha verification code - unique identifier (uuid)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginBody {

  /** Username */
  private String email;

  /** Password */
  private String password;

  /** Captcha code */
  private String code;

  /** Unique identifier for captcha/session */
  private String uuid;
}
