package com.api.common.domain.upload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PresignPutRequest {

  @NotBlank
  private String category;

  @NotBlank
  private String originalName;

  /** Optional; if missing will fallback to application/octet-stream. */
  private String contentType;

  /** Required for policy check. */
  @NotNull
  @Min(1)
  private Long sizeBytes;

  private String bizType;
  private String bizId;

  /** Optional, override category TTL for presign. */
  private Integer expirySeconds;
}
