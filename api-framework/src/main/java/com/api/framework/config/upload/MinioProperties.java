package com.api.framework.config.upload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

  @NotBlank private String endpoint;
  @NotBlank private String accessKey;
  @NotBlank private String secretKey;

  @NotBlank private String bucket;

  @NotNull private Integer defaultPresignExpirySeconds = 3600;

  /** Optional: public reverse-proxy domain for PUBLIC objects. */
  private String publicBaseUrl;
}
