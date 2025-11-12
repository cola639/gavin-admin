package com.api.common.utils.file;

import com.api.common.config.AppConfig;
import com.api.common.exceptions.file.FileNameLengthLimitExceededException;
import com.api.common.exceptions.file.FileSizeLimitExceededException;
import com.api.common.exceptions.file.InvalidExtensionException;
import com.api.common.utils.DateEnhancedUtil;
import com.api.common.utils.uuid.Seq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

/**
 * Modern file upload utility class.
 *
 * <p>Provides safe file handling with size limits, extension validation, and configurable storage
 * paths through {@link AppConfig}.
 *
 * <p>Compatible with Spring Boot 3.5 and Java 17.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadUtils {

  /** Maximum upload size (50 MB). */
  public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024L;

  /** Maximum file name length (100 characters). */
  public static final int DEFAULT_FILE_NAME_LENGTH = 100;

  /** Injected application configuration for path resolution. */
  private final AppConfig appConfig;

  // ------------------------------------------------------------------------
  // 📦 Core Upload Logic
  // ------------------------------------------------------------------------

  /**
   * Uploads a file using the default base directory and allowed extensions.
   *
   * @param file the uploaded file
   * @return saved relative file path (e.g. /profile/upload/2025/10/18/file_123.png)
   */
  public String upload(MultipartFile file)
      throws IOException,
          FileSizeLimitExceededException,
          FileNameLengthLimitExceededException,
          InvalidExtensionException {
    return upload(appConfig.getUploadPath(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSIONS);
  }

  /**
   * Uploads a file to a specified directory with allowed extensions.
   *
   * @param baseDir upload directory base (absolute or relative to profile)
   * @param file uploaded file
   * @param allowedExtensions array of allowed file extensions
   * @return saved relative file path
   */
  public String upload(String baseDir, MultipartFile file, java.util.List<String> allowedExtensions)
      throws IOException,
          FileSizeLimitExceededException,
          FileNameLengthLimitExceededException,
          InvalidExtensionException {

    String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
    if (originalFilename.length() > DEFAULT_FILE_NAME_LENGTH) {
      throw new FileNameLengthLimitExceededException(DEFAULT_FILE_NAME_LENGTH);
    }

    assertAllowed(file, allowedExtensions);

    // Generate unique name like "2025/10/18/filename_12345.png"
    String fileName = buildUniqueFilename(file);

    // Save file to disk
    File targetFile = getAbsoluteFile(baseDir, fileName);
    file.transferTo(targetFile.toPath());

    String path = resolveRelativePath(baseDir, fileName);
    log.info("✅ Uploaded file: [{}] → [{}]", originalFilename, path);
    return path;
  }

  // ------------------------------------------------------------------------
  // 📁 Path Helpers
  // ------------------------------------------------------------------------

  /** Resolves the absolute file on disk, creating parent directories if needed. */
  public static File getAbsoluteFile(String uploadDir, String fileName) throws IOException {
    Path targetPath = Paths.get(uploadDir, fileName);
    File dest = targetPath.toFile();

    if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
      log.warn("⚠️ Failed to create upload directory: {}", dest.getParent());
    }

    return dest;
  }

  /** Converts an absolute upload directory + file name into a relative /profile/... path. */
  private String resolveRelativePath(String uploadDir, String fileName) {
    String profilePath = appConfig.getProfile();
    if (uploadDir.startsWith(profilePath)) {
      String relative = uploadDir.substring(profilePath.length()).replace("\\", "/");
      return "/profile" + relative + "/" + fileName;
    }
    return uploadDir + "/" + fileName;
  }

  /**
   * Builds a unique, date-based file name.
   *
   * <p>Example: 2025/10/18/avatar_12345.png
   */
  private String buildUniqueFilename(MultipartFile file) {
    String extension = getExtension(file);
    String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
    String datePath = DateEnhancedUtil.datePath(); // e.g. 2025/10/18
    String uniqueId = Seq.getId(Seq.uploadSeqType);
    return String.format("%s/%s_%s.%s", datePath, baseName, uniqueId, extension);
  }

  // ------------------------------------------------------------------------
  // 🧩 Validation
  // ------------------------------------------------------------------------

  /** Validates file size and allowed extensions. */
  private void assertAllowed(MultipartFile file, java.util.List<String> allowedExtensions)
      throws FileSizeLimitExceededException, InvalidExtensionException {

    long size = file.getSize();
    if (size > DEFAULT_MAX_SIZE) {
      throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
    }

    String extension = getExtension(file);
    if (!MimeTypeUtils.isAllowedExtension(extension)) {
      throw new InvalidExtensionException(
          allowedExtensions.toArray(new String[0]), extension, file.getOriginalFilename());
    }
  }

  /** Extracts the file extension safely, falling back to MIME detection if missing. */
  private String getExtension(MultipartFile file) {
    String extension = FilenameUtils.getExtension(file.getOriginalFilename());
    if (extension == null || extension.isBlank()) {
      extension =
          MimeTypeUtils.getExtension(
              Objects.requireNonNull(file.getContentType(), "Missing content type"));
    }
    return extension.toLowerCase(Locale.ROOT);
  }
}
