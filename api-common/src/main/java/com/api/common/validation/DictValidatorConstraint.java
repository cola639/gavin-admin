package com.api.common.validation;

import com.api.common.enums.DictEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/** Constraint validator for @DictValidator. */
@Slf4j
public class DictValidatorConstraint implements ConstraintValidator<DictValidator, String> {

  private Set<String> validValues;
  private boolean allowNull;

  @Override
  public void initialize(DictValidator constraintAnnotation) {
    this.allowNull = constraintAnnotation.allowNull();

    Class<? extends DictEnum<?>> enumClass = constraintAnnotation.value();
    DictEnum<?>[] enumConstants = enumClass.getEnumConstants();

    Set<String> codes = new HashSet<String>();
    if (enumConstants != null) {
      for (DictEnum<?> dictEnum : enumConstants) {
        // Always convert to String for comparison
        codes.add(String.valueOf(dictEnum.getCode()));
      }
    }

    this.validValues = codes;
    log.debug(
        "Initialized DictValidator for enum: {}, valid values: {}",
        enumClass.getSimpleName(),
        validValues);
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return allowNull;
    }
    return validValues.contains(value);
  }
}
