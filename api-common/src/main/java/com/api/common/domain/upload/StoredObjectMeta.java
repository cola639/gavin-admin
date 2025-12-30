package com.api.common.domain.upload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StoredObjectMeta {
  String bucket;
  String objectKey;
  String etag;
  long size;
  String contentType;
}
