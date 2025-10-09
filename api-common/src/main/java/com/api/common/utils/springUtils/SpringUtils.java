package com.api.common.utils.springUtils;

import com.api.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring utility class for convenient access to beans and environment configuration.
 *
 * <p>Allows retrieval of Spring-managed beans from non-managed classes, access to active profiles,
 * and safe AOP proxy resolution.
 *
 * <p>All method names remain identical to the original Ruoyi implementation for backward
 * compatibility.
 *
 * @author Gavin
 */
@Slf4j
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware {

  /** The Spring BeanFactory — set by Spring during context initialization. */
  private static ConfigurableListableBeanFactory beanFactory;

  /** The global ApplicationContext — available after Spring startup. */
  private static ApplicationContext applicationContext;

  // ─────────────────────────────────────────────────────────────
  // Spring lifecycle callbacks
  // ─────────────────────────────────────────────────────────────

  @Override
  public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    SpringUtils.beanFactory = beanFactory;
  }

  @Override
  public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
    SpringUtils.applicationContext = context;
  }

  // ─────────────────────────────────────────────────────────────
  // Bean access methods
  // ─────────────────────────────────────────────────────────────

  /**
   * Get a bean instance by its name.
   *
   * @param name the name of the bean
   * @return the bean instance
   * @throws BeansException if no such bean exists
   */
  @SuppressWarnings("unchecked")
  public static <T> T getBean(String name) throws BeansException {
    assertBeanFactory();
    return (T) beanFactory.getBean(name);
  }

  /**
   * Get a bean instance by its type.
   *
   * @param clz the required bean type
   * @return the bean instance
   * @throws BeansException if no such bean exists
   */
  public static <T> T getBean(Class<T> clz) throws BeansException {
    assertBeanFactory();
    return beanFactory.getBean(clz);
  }

  /** Check if a bean with the given name exists in the Spring context. */
  public static boolean containsBean(String name) {
    assertBeanFactory();
    return beanFactory.containsBean(name);
  }

  /** Check whether the given bean name refers to a singleton. */
  public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
    assertBeanFactory();
    return beanFactory.isSingleton(name);
  }

  /** Get the type of a bean by name. */
  public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
    assertBeanFactory();
    return beanFactory.getType(name);
  }

  /** Get aliases associated with a bean definition. */
  public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
    assertBeanFactory();
    return beanFactory.getAliases(name);
  }

  // ─────────────────────────────────────────────────────────────
  // AOP proxy utilities
  // ─────────────────────────────────────────────────────────────

  /**
   * Retrieve the current AOP proxy of the given bean.
   *
   * <p>If the bean is not proxied or AOP context is not active, returns the bean itself.
   *
   * @param invoker original bean instance
   * @return proxy instance if available; otherwise the original object
   */
  @SuppressWarnings("unchecked")
  public static <T> T getAopProxy(T invoker) {
    try {
      Object proxy = AopContext.currentProxy();
      if (proxy instanceof Advised advised) {
        if (advised.getTargetSource().getTargetClass() == invoker.getClass()) {
          return (T) proxy;
        }
      }
    } catch (IllegalStateException e) {
      // AOP context is not active — fallback to original
      log.debug("AOP proxy not available for class {}", invoker.getClass().getSimpleName());
    }
    return invoker;
  }

  // ─────────────────────────────────────────────────────────────
  // Environment / Profile utilities
  // ─────────────────────────────────────────────────────────────

  /** Get the currently active Spring environment profiles. */
  public static String[] getActiveProfiles() {
    assertApplicationContext();
    Environment env = applicationContext.getEnvironment();
    return env.getActiveProfiles();
  }

  /** Get the first active Spring environment profile (if any). */
  public static String getActiveProfile() {
    String[] profiles = getActiveProfiles();
    return StringUtils.isNotEmpty(profiles) ? profiles[0] : null;
  }

  /**
   * Retrieve a required property from the active environment.
   *
   * @param key the property key
   * @return the property value
   * @throws IllegalStateException if property not found
   */
  public static String getRequiredProperty(String key) {
    assertApplicationContext();
    return applicationContext.getEnvironment().getRequiredProperty(key);
  }

  // ─────────────────────────────────────────────────────────────
  // Internal helpers
  // ─────────────────────────────────────────────────────────────

  /** Ensure that BeanFactory has been initialized. */
  private static void assertBeanFactory() {
    if (beanFactory == null) {
      throw new IllegalStateException("Spring BeanFactory not initialized yet.");
    }
  }

  /** Ensure that ApplicationContext has been initialized. */
  private static void assertApplicationContext() {
    if (applicationContext == null) {
      throw new IllegalStateException("Spring ApplicationContext not initialized yet.");
    }
  }
}
