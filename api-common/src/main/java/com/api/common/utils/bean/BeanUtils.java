package com.api.common.utils.bean;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for working with JavaBeans.
 *
 * <p>Provides enhanced methods for copying bean properties and discovering getter/setter methods
 * using reflection.
 */
@Slf4j
public final class BeanUtils {

  /** Prefix length for property methods (getX/setX). */
  private static final int BEAN_METHOD_PROP_INDEX = 3;

  /** Regex for identifying getter methods. */
  private static final Pattern GETTER_PATTERN = Pattern.compile("get(\\p{javaUpperCase}\\w*)");

  /** Regex for identifying setter methods. */
  private static final Pattern SETTER_PATTERN = Pattern.compile("set(\\p{javaUpperCase}\\w*)");

  private BeanUtils() {
    // Utility class, prevent instantiation
  }

  /**
   * Copies properties from one bean to another.
   *
   * @param target the target object
   * @param source the source object
   */
  public static void copyBeanProperties(Object target, Object source) {
    try {
      org.springframework.beans.BeanUtils.copyProperties(source, target);
    } catch (Exception e) {
      log.error(
          "Failed to copy properties from {} to {}",
          source.getClass().getSimpleName(),
          target.getClass().getSimpleName(),
          e);
    }
  }

  /**
   * Returns a list of all setter methods for the given object.
   *
   * @param obj the object to inspect
   * @return list of setter methods
   */
  public static List<Method> getSetterMethods(Object obj) {
    return Stream.of(obj.getClass().getMethods())
        .filter(method -> SETTER_PATTERN.matcher(method.getName()).matches())
        .filter(method -> method.getParameterCount() == 1)
        .collect(Collectors.toList());
  }

  /**
   * Returns a list of all getter methods for the given object.
   *
   * @param obj the object to inspect
   * @return list of getter methods
   */
  public static List<Method> getGetterMethods(Object obj) {
    return Stream.of(obj.getClass().getMethods())
        .filter(method -> GETTER_PATTERN.matcher(method.getName()).matches())
        .filter(method -> method.getParameterCount() == 0)
        .collect(Collectors.toList());
  }

  /**
   * Checks if two bean methods refer to the same property.
   *
   * <p>Example: {@code getName()} and {@code setName()} → true <br>
   * {@code getName()} and {@code setAge()} → false
   *
   * @param methodName1 first method name
   * @param methodName2 second method name
   * @return {@code true} if both methods operate on the same property
   */
  public static boolean isMethodPropertyEqual(String methodName1, String methodName2) {
    if (methodName1 == null
        || methodName2 == null
        || methodName1.length() <= BEAN_METHOD_PROP_INDEX
        || methodName2.length() <= BEAN_METHOD_PROP_INDEX) {
      return false;
    }
    return methodName1
        .substring(BEAN_METHOD_PROP_INDEX)
        .equals(methodName2.substring(BEAN_METHOD_PROP_INDEX));
  }
}
