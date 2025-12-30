package com.api.common.domain.upload;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListedObject {
  String objectKey;
  long size;
  Instant lastModified;
}
