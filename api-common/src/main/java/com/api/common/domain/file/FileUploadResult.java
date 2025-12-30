package com.api.common.domain.file;

import com.api.common.enums.FileVisibilityEnum;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FileUploadResult {
  Long fileId;
  String bucket;
  String objectKey;
  String originalName;
  String contentType;
  long sizeBytes;
  String etag;
  FileVisibilityEnum visibility;

  /** Optional (presigned or public). */
  String url;
}
