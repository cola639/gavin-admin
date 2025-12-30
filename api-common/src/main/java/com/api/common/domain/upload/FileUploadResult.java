package com.api.common.domain.upload;

import com.api.common.enums.upload.FileVisibilityEnum;
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
