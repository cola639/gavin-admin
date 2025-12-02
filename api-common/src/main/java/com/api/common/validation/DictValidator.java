package com.api.common.validation;

import com.api.common.enums.DictEnum;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Generic dictionary validator. Example: @DictValidator(UserStatusEnum.class) private String
 * status;
 */
@Documented
@Constraint(validatedBy = DictValidatorConstraint.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictValidator {

  /** Enum class that implements DictEnum. */
  Class<? extends DictEnum<?>> value();

  /** Default error message. */
  String message() default "Invalid dictionary value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** If true, null or blank is valid. */
  boolean allowNull() default true;
}
