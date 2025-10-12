package com.api.framework.config;

import com.api.framework.interceptor.TrackSQLDetailInspector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/** Provides Spring ApplicationContext to Hibernate StatementInspector. */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    TrackSQLDetailInspector.setApplicationContext(applicationContext);
  }
}
