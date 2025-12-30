package com.api.common.domain.upload;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class BatchDeleteRequest {
  @NotEmpty private List<Long> fileIds;
}
