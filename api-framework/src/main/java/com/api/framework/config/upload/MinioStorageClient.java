package com.api.framework.config.upload;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.api.common.domain.file.ListedObject;
import com.api.common.domain.file.PresignedUrl;
import com.api.common.domain.file.StorageClient;
import com.api.common.domain.file.StoredObjectMeta;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioStorageClient implements StorageClient {

  private final MinioClient minioClient;

  @Override
  public StoredObjectMeta putObject(
      String bucket, String objectKey, InputStream stream, long size, String contentType) {

    try {
      log.info(
          "Uploading object to MinIO. bucket={}, objectKey={}, size={}, contentType={}",
          bucket,
          objectKey,
          size,
          contentType);

      PutObjectArgs args =
          PutObjectArgs.builder().bucket(bucket).object(objectKey).stream(stream, size, -1)
              .contentType(contentType)
              .build();

      ObjectWriteResponse resp = minioClient.putObject(args);

      return StoredObjectMeta.builder()
          .bucket(bucket)
          .objectKey(objectKey)
          .etag(resp.etag())
          .size(size)
          .contentType(contentType)
          .build();
    } catch (Exception e) {
      log.error("Failed to upload object. bucket={}, objectKey={}", bucket, objectKey, e);
      throw new IllegalStateException("MinIO upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public PresignedUrl presignGet(String bucket, String objectKey, Duration expiry) {
    try {
      int seconds = (int) Math.min(Math.max(expiry.getSeconds(), 60), 7 * 24 * 3600);
      String url =
          minioClient.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.GET)
                  .bucket(bucket)
                  .object(objectKey)
                  .expiry(seconds)
                  .build());

      return PresignedUrl.builder().url(url).expireAt(Instant.now().plusSeconds(seconds)).build();
    } catch (Exception e) {
      log.error("Failed to presign GET url. bucket={}, objectKey={}", bucket, objectKey, e);
      throw new IllegalStateException("MinIO presign GET failed: " + e.getMessage(), e);
    }
  }

  @Override
  public PresignedUrl presignPut(String bucket, String objectKey, Duration expiry) {
    try {
      int seconds = (int) Math.min(Math.max(expiry.getSeconds(), 60), 7 * 24 * 3600);
      String url =
          minioClient.getPresignedObjectUrl(
              GetPresignedObjectUrlArgs.builder()
                  .method(Method.PUT)
                  .bucket(bucket)
                  .object(objectKey)
                  .expiry(seconds)
                  .build());

      return PresignedUrl.builder().url(url).expireAt(Instant.now().plusSeconds(seconds)).build();
    } catch (Exception e) {
      log.error("Failed to presign PUT url. bucket={}, objectKey={}", bucket, objectKey, e);
      throw new IllegalStateException("MinIO presign PUT failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void removeObject(String bucket, String objectKey) {
    try {
      log.info("Removing object from MinIO. bucket={}, objectKey={}", bucket, objectKey);
      minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
    } catch (Exception e) {
      log.error("Failed to remove object. bucket={}, objectKey={}", bucket, objectKey, e);
      throw new IllegalStateException("MinIO remove failed: " + e.getMessage(), e);
    }
  }

  @Override
  public StoredObjectMeta statObject(String bucket, String objectKey) {
    try {
      StatObjectResponse stat =
          minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());

      return StoredObjectMeta.builder()
          .bucket(bucket)
          .objectKey(objectKey)
          .etag(stat.etag())
          .size(stat.size())
          .contentType(stat.contentType())
          .build();
    } catch (Exception e) {
      log.error("Failed to stat object. bucket={}, objectKey={}", bucket, objectKey, e);
      throw new IllegalStateException("MinIO stat failed: " + e.getMessage(), e);
    }
  }

  @Override
  public Stream<ListedObject> listObjects(
      String bucket, String prefix, boolean recursive, int limit) {
    try {
      Iterable<Result<Item>> results =
          minioClient.listObjects(
              ListObjectsArgs.builder()
                  .bucket(bucket)
                  .prefix(prefix == null ? "" : prefix)
                  .recursive(recursive)
                  .build());

      Stream<Result<Item>> stream =
          StreamSupport.stream(Spliterators.spliteratorUnknownSize(results.iterator(), 0), false);

      return stream
          .limit(Math.max(limit, 1))
          .map(
              r -> {
                try {
                  Item it = r.get();
                  Instant lm =
                      it.lastModified() == null ? Instant.EPOCH : it.lastModified().toInstant();
                  return ListedObject.builder()
                      .objectKey(it.objectName())
                      .size(it.size())
                      .lastModified(lm)
                      .build();
                } catch (Exception e) {
                  log.warn("Skip invalid list item. bucket={}, prefix={}", bucket, prefix, e);
                  return null;
                }
              })
          .filter(x -> x != null);
    } catch (Exception e) {
      log.error("Failed to list objects. bucket={}, prefix={}", bucket, prefix, e);
      return Stream.empty();
    }
  }
}
