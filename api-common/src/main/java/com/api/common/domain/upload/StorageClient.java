package com.api.common.domain.upload;

import java.io.InputStream;
import java.time.Duration;
import java.util.stream.Stream;

public interface StorageClient {

  StoredObjectMeta putObject(
      String bucket, String objectKey, InputStream stream, long size, String contentType);

  PresignedUrl presignGet(String bucket, String objectKey, Duration expiry);

  PresignedUrl presignPut(String bucket, String objectKey, Duration expiry);

  void removeObject(String bucket, String objectKey);

  StoredObjectMeta statObject(String bucket, String objectKey);

  Stream<ListedObject> listObjects(String bucket, String prefix, boolean recursive, int limit);
}
