package com.api.common.domain.file;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UploadConfirmRequest {
  @NotNull private Long fileId;
}
