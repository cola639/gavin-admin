package com.api.system.service.upload;

import com.api.common.domain.file.*;
import com.api.common.enums.FileCategoryEnum;
import com.api.common.enums.FileVisibilityEnum;
import com.api.framework.config.upload.MinioProperties;
import com.api.system.repository.SysFileObjectRepository;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileAppService {

  private final MinioProperties minioProperties;
  private final StorageClient storageClient;
  private final SysFileObjectRepository repository;
  private final FilePolicyService policyService;
  private final FilePermissionService permissionService;

  // ------------------------------------------------------------------------
  // Phase-1: Multipart upload
  // ------------------------------------------------------------------------

  @Transactional
  public FileUploadResult uploadOne(
      String category, MultipartFile file, String bizType, String bizId) {
    FileCategoryEnum cat = FileCategoryEnum.parse(category);
    FilePolicyService.Policy policy = policyService.getPolicy(cat);

    Long userId = currentUserId();

    validateMultipartFile(file, policy);

    String objectKey = buildObjectKey(policy.getPrefix(), userId, file);
    String bucket = minioProperties.getBucket();

    try (InputStream inputStream = file.getInputStream()) {
      StoredObjectMeta meta =
          storageClient.putObject(
              bucket,
              objectKey,
              inputStream,
              file.getSize(),
              safeContentType(file.getContentType()));

      SysFileObject saved =
          repository.save(
              SysFileObject.builder()
                  .bucket(bucket)
                  .objectKey(objectKey)
                  .originalName(file.getOriginalFilename())
                  .contentType(safeContentType(file.getContentType()))
                  .sizeBytes(file.getSize())
                  .etag(meta.getEtag())
                  .ownerUserId(userId)
                  .category(cat.name())
                  .visibility(policy.getVisibility())
                  .bizType(bizType)
                  .bizId(bizId)
                  .deleted(false)
                  .build());

      String url = buildUrl(saved, Duration.ofSeconds(policy.getPresignExpirySeconds()));

      log.info(
          "Uploaded file. fileId={}, category={}, objectKey={}",
          saved.getFileId(),
          cat.name(),
          objectKey);

      return FileUploadResult.builder()
          .fileId(saved.getFileId())
          .bucket(saved.getBucket())
          .objectKey(saved.getObjectKey())
          .originalName(saved.getOriginalName())
          .contentType(saved.getContentType())
          .sizeBytes(saved.getSizeBytes())
          .etag(saved.getEtag())
          .visibility(saved.getVisibility())
          .url(url)
          .build();

    } catch (Exception e) {
      log.error(
          "Upload failed. category={}, originalName={}", category, file.getOriginalFilename(), e);
      throw new IllegalStateException("Upload failed: " + e.getMessage(), e);
    }
  }

  @Transactional
  public List<FileUploadResult> uploadMany(
      String category, List<MultipartFile> files, String bizType, String bizId) {
    return files.stream().map(f -> uploadOne(category, f, bizType, bizId)).toList();
  }

  // ------------------------------------------------------------------------
  // Phase-1: Access (presigned GET)
  // ------------------------------------------------------------------------

  @Transactional(readOnly = true)
  public FileUrlResponse getFileUrl(Long fileId, Integer expirySeconds) {
    SysFileObject obj =
        repository
            .findByFileIdAndDeletedFalse(fileId)
            .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

    Long userId = currentUserId();
    if (!permissionService.canRead(obj, userId)) {
      throw new SecurityException("No permission to read file: " + fileId);
    }

    if (obj.getEtag() == null || obj.getEtag().isBlank()) {
      throw new IllegalStateException("File is not ready yet (pending upload confirm): " + fileId);
    }

    int seconds =
        expirySeconds != null && expirySeconds > 0
            ? expirySeconds
            : minioProperties.getDefaultPresignExpirySeconds();

    PresignedUrl url =
        storageClient.presignGet(obj.getBucket(), obj.getObjectKey(), Duration.ofSeconds(seconds));
    return FileUrlResponse.builder().url(url.getUrl()).expireAt(url.getExpireAt()).build();
  }

  // ------------------------------------------------------------------------
  // Phase-1: Delete
  // ------------------------------------------------------------------------

  @Transactional
  public void deleteOne(Long fileId) {
    SysFileObject obj =
        repository
            .findByFileIdAndDeletedFalse(fileId)
            .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

    Long userId = currentUserId();
    if (!permissionService.canDelete(obj, userId)) {
      throw new SecurityException("No permission to delete file: " + fileId);
    }

    storageClient.removeObject(obj.getBucket(), obj.getObjectKey());

    obj.setDeleted(true);
    obj.setDeletedAt(LocalDateTime.now());
    obj.setDeletedBy(userId);
    repository.save(obj);

    log.info("Deleted file. fileId={}, objectKey={}", fileId, obj.getObjectKey());
  }

  @Transactional
  public List<Long> deleteBatch(List<Long> fileIds) {
    fileIds.forEach(this::deleteOne);
    return fileIds;
  }

  // ------------------------------------------------------------------------
  // Phase-2: Presigned PUT (browser direct upload)
  // ------------------------------------------------------------------------

  @Transactional
  public PresignPutResponse presignPut(PresignPutRequest request) {
    FileCategoryEnum cat = FileCategoryEnum.parse(request.getCategory());
    FilePolicyService.Policy policy = policyService.getPolicy(cat);

    Long userId = currentUserId();

    validatePresignRequest(request, policy);

    String bucket = minioProperties.getBucket();
    String objectKey =
        buildObjectKey(
            policy.getPrefix(), userId, request.getOriginalName(), request.getContentType());

    int seconds =
        request.getExpirySeconds() != null && request.getExpirySeconds() > 0
            ? request.getExpirySeconds()
            : policy.getPresignExpirySeconds();

    PresignedUrl presignedUrl =
        storageClient.presignPut(bucket, objectKey, Duration.ofSeconds(seconds));

    SysFileObject saved =
        repository.save(
            SysFileObject.builder()
                .bucket(bucket)
                .objectKey(objectKey)
                .originalName(request.getOriginalName())
                .contentType(safeContentType(request.getContentType()))
                .sizeBytes(request.getSizeBytes())
                // etag = null => pending
                .ownerUserId(userId)
                .category(cat.name())
                .visibility(policy.getVisibility())
                .bizType(request.getBizType())
                .bizId(request.getBizId())
                .deleted(false)
                .build());

    log.info(
        "Created presigned PUT session. fileId={}, bucket={}, objectKey={}, expireAt={}",
        saved.getFileId(),
        bucket,
        objectKey,
        presignedUrl.getExpireAt());

    return PresignPutResponse.builder()
        .fileId(saved.getFileId())
        .bucket(bucket)
        .objectKey(objectKey)
        .uploadUrl(presignedUrl.getUrl())
        .expireAt(presignedUrl.getExpireAt())
        .build();
  }

  @Transactional
  public FileUploadResult confirmUpload(UploadConfirmRequest request) {
    SysFileObject obj =
        repository
            .findByFileIdAndDeletedFalse(request.getFileId())
            .orElseThrow(
                () -> new IllegalArgumentException("File not found: " + request.getFileId()));

    Long userId = currentUserId();
    if (!permissionService.canRead(obj, userId)) {
      throw new SecurityException("No permission to confirm file: " + request.getFileId());
    }

    if (obj.getEtag() != null && !obj.getEtag().isBlank()) {
      log.info(
          "Upload already confirmed. fileId={}, objectKey={}", obj.getFileId(), obj.getObjectKey());
      return FileUploadResult.builder()
          .fileId(obj.getFileId())
          .bucket(obj.getBucket())
          .objectKey(obj.getObjectKey())
          .originalName(obj.getOriginalName())
          .contentType(obj.getContentType())
          .sizeBytes(obj.getSizeBytes())
          .etag(obj.getEtag())
          .visibility(obj.getVisibility())
          .url(buildUrl(obj, Duration.ofSeconds(minioProperties.getDefaultPresignExpirySeconds())))
          .build();
    }

    StoredObjectMeta stat = storageClient.statObject(obj.getBucket(), obj.getObjectKey());

    if (obj.getSizeBytes() != null
        && obj.getSizeBytes() > 0
        && stat.getSize() != obj.getSizeBytes()) {
      log.warn(
          "Size mismatch on confirm. fileId={}, expected={}, actual={}",
          obj.getFileId(),
          obj.getSizeBytes(),
          stat.getSize());
    }

    obj.setEtag(stat.getEtag());
    obj.setSizeBytes(stat.getSize());
    obj.setContentType(stat.getContentType());
    repository.save(obj);

    log.info(
        "Confirmed upload. fileId={}, objectKey={}, etag={}",
        obj.getFileId(),
        obj.getObjectKey(),
        obj.getEtag());

    return FileUploadResult.builder()
        .fileId(obj.getFileId())
        .bucket(obj.getBucket())
        .objectKey(obj.getObjectKey())
        .originalName(obj.getOriginalName())
        .contentType(obj.getContentType())
        .sizeBytes(obj.getSizeBytes())
        .etag(obj.getEtag())
        .visibility(obj.getVisibility())
        .url(buildUrl(obj, Duration.ofSeconds(minioProperties.getDefaultPresignExpirySeconds())))
        .build();
  }

  // ------------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------------

  private void validateMultipartFile(MultipartFile file, FilePolicyService.Policy policy) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("file is required");
    }

    long size = file.getSize();
    if (size > policy.getMaxSizeBytes()) {
      throw new IllegalArgumentException(
          "File too large. maxSizeBytes=" + policy.getMaxSizeBytes());
    }

    String ext = getExtension(file.getOriginalFilename(), file.getContentType());
    boolean allowed = policy.getAllowedExtensions().stream().anyMatch(x -> x.equalsIgnoreCase(ext));
    if (!allowed) {
      throw new IllegalArgumentException(
          "Invalid extension: " + ext + ", allowed=" + policy.getAllowedExtensions());
    }
  }

  private void validatePresignRequest(PresignPutRequest request, FilePolicyService.Policy policy) {
    if (request.getSizeBytes() == null || request.getSizeBytes() <= 0) {
      throw new IllegalArgumentException("sizeBytes is required");
    }
    if (request.getSizeBytes() > policy.getMaxSizeBytes()) {
      throw new IllegalArgumentException(
          "File too large. maxSizeBytes=" + policy.getMaxSizeBytes());
    }

    String ext = getExtension(request.getOriginalName(), request.getContentType());
    boolean allowed = policy.getAllowedExtensions().stream().anyMatch(x -> x.equalsIgnoreCase(ext));
    if (!allowed) {
      throw new IllegalArgumentException(
          "Invalid extension: " + ext + ", allowed=" + policy.getAllowedExtensions());
    }
  }

  private String buildObjectKey(String prefix, Long userId, MultipartFile file) {
    String datePath = com.api.common.utils.DateEnhancedUtil.datePath();
    String ext = getExtension(file.getOriginalFilename(), file.getContentType());
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("%s/%s/u%s_%s.%s", prefix, datePath, userId, uuid, ext);
  }

  private String buildObjectKey(
      String prefix, Long userId, String originalName, String contentType) {
    String datePath = com.api.common.utils.DateEnhancedUtil.datePath();
    String ext = getExtension(originalName, contentType);
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("%s/%s/u%s_%s.%s", prefix, datePath, userId, uuid, ext);
  }

  private String getExtension(String originalName, String contentType) {
    String ext = FilenameUtils.getExtension(originalName);
    if (ext == null || ext.isBlank()) {
      ext = com.api.common.utils.file.MimeTypeUtils.getExtension(safeContentType(contentType));
    }
    return ext.toLowerCase(Locale.ROOT);
  }

  private String safeContentType(String contentType) {
    return (contentType == null || contentType.isBlank())
        ? "application/octet-stream"
        : contentType;
  }

  private String buildUrl(SysFileObject obj, Duration expiry) {
    if (obj.getVisibility() == FileVisibilityEnum.PUBLIC) {
      String publicBase = minioProperties.getPublicBaseUrl();
      if (publicBase != null && !publicBase.isBlank()) {
        String base =
            publicBase.endsWith("/")
                ? publicBase.substring(0, publicBase.length() - 1)
                : publicBase;
        return base + "/" + obj.getBucket() + "/" + obj.getObjectKey();
      }
    }
    return storageClient.presignGet(obj.getBucket(), obj.getObjectKey(), expiry).getUrl();
  }

  private Long currentUserId() {
    return com.api.common.utils.SecurityUtils.getUserId();
  }
}
