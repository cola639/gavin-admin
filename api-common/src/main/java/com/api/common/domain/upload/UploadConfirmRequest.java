package com.api.common.domain.upload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadConfirmRequest {
  @NotNull private Long fileId;
}
