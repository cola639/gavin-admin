package com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.api")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableJpaRepositories(basePackages = "com.api") // ✅ scan all module repositories
@EntityScan(basePackages = "com.api") // ✅ scan all module entities
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
