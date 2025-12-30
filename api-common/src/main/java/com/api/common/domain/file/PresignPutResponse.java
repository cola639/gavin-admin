package com.api.common.domain.file;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PresignPutResponse {
  Long fileId;
  String bucket;
  String objectKey;

  String uploadUrl;
  Instant expireAt;
}
