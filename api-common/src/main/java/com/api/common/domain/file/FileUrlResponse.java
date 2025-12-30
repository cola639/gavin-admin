package com.api.common.domain.file;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileUrlResponse {
  String url;
  Instant expireAt;
}
