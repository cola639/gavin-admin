package com.api.framework.config.email;

import com.api.framework.client.email.EmailSender;
import com.api.framework.client.email.impl.SesEmailSender;
import com.api.framework.template.email.EmailTemplateRenderer;
import com.api.framework.template.impl.ThymeleafEmailTemplateRenderer;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Slf4j
@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailAutoConfiguration {

  @Bean
  public SesV2Client sesV2Client(EmailProperties props) {
    var builder =
        SesV2Client.builder()
            .region(Region.of(props.getSes().getRegion()))
            .httpClient(UrlConnectionHttpClient.create());

    var accessKey = safeTrim(props.getSes().getAccessKey());
    var secretKey = safeTrim(props.getSes().getSecretKey());

    if (!accessKey.isEmpty() && !secretKey.isEmpty()) {
      log.info("Email: SES client uses StaticCredentialsProvider (env/config).");
      builder.credentialsProvider(
          StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
    } else {
      log.info("Email: SES client uses DefaultCredentialsProvider (env/instance profile).");
      builder.credentialsProvider(DefaultCredentialsProvider.create());
    }

    return builder.build();
  }

  @Bean
  public EmailSender emailSender(SesV2Client client, EmailProperties props) {
    return new SesEmailSender(client, props);
  }

  @Bean("emailHtmlTemplateEngine")
  public TemplateEngine emailHtmlTemplateEngine(EmailProperties props) {
    var resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix(normalizePrefix(props.getTemplateRoot()));
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resolver.setCacheable(true);
    resolver.setCheckExistence(true);

    var engine = new TemplateEngine();
    engine.setTemplateResolver(resolver);
    return engine;
  }

  @Bean("emailTextTemplateEngine")
  public TemplateEngine emailTextTemplateEngine(EmailProperties props) {
    var resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix(normalizePrefix(props.getTemplateRoot()));
    resolver.setSuffix(".txt");
    resolver.setTemplateMode(TemplateMode.TEXT);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resolver.setCacheable(true);
    resolver.setCheckExistence(true);

    var engine = new TemplateEngine();
    engine.setTemplateResolver(resolver);
    return engine;
  }

  @Bean
  public EmailTemplateRenderer emailTemplateRenderer(
      @Qualifier("emailHtmlTemplateEngine") TemplateEngine htmlEngine,
      @Qualifier("emailTextTemplateEngine") TemplateEngine textEngine) {
    return new ThymeleafEmailTemplateRenderer(htmlEngine, textEngine);
  }

  private static String normalizePrefix(String templateRoot) {
    var root = safeTrim(templateRoot);
    if (root.isEmpty()) {
      root = "templates/email";
    }
    if (!root.endsWith("/")) {
      root = root + "/";
    }
    return root;
  }

  private static String safeTrim(String v) {
    return v == null ? "" : v.trim();
  }
}
