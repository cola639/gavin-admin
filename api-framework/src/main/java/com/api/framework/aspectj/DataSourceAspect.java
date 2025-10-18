package com.api.framework.aspectj;

import com.api.common.annotation.DataSource;
import com.api.framework.datasource.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Order(1)
@Component
public class DataSourceAspect {

  @Around(
      "@annotation(com.api.common.annotation.DataSource) || @within(com.api.common.annotation.DataSource)")
  public Object switchDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
    var signature = (MethodSignature) joinPoint.getSignature();
    var method = signature.getMethod();

    DataSource dataSource = AnnotationUtils.findAnnotation(method, DataSource.class);
    if (dataSource == null) {
      dataSource = AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }

    if (dataSource != null) {
      DynamicDataSourceContextHolder.set(dataSource.value().name());
    }

    try {
      return joinPoint.proceed();
    } finally {
      DynamicDataSourceContextHolder.clear();
    }
  }
}
