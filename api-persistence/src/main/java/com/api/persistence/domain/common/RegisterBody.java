package com.api.persistence.domain.common;

import lombok.EqualsAndHashCode;

/**
 * DTO representing user registration request payload.
 *
 * <p>Extends {@link LoginBody}, reuses username, password, captcha, and uuid fields.
 */
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {}
