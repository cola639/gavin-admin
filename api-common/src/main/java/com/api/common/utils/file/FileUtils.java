package com.api.common.utils.file;

import com.api.common.config.AppConfig;
import com.api.common.constant.Constants;
import com.api.common.utils.DateEnhancedUtil;
import com.api.common.utils.StringUtils;
import com.api.common.utils.uuid.IdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

/**
 * File utility class for reading, writing, downloading, and validating files.
 *
 * <p>Refactored for Spring Boot 3.5 + Java 17. Uses {@link AppConfig} for path resolution and
 * integrates cleanly with {@link FileUploadUtils} for consistency.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileUtils {

  private final AppConfig appConfig;
  private final FileUploadUtils fileUploadUtils;

  /** Valid filename pattern (letters, digits, underscore, dash, Chinese characters, etc.) */
  public static final String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

  // ------------------------------------------------------------------------
  // 📤 File Output Methods
  // ------------------------------------------------------------------------

  /**
   * Writes a file’s bytes to an output stream.
   *
   * @param filePath absolute path of the file
   * @param os output stream
   */
  public static void writeBytes(String filePath, OutputStream os) throws IOException {
    Path path = Paths.get(filePath);
    if (!Files.exists(path)) {
      throw new FileNotFoundException(filePath);
    }
    try (InputStream fis = Files.newInputStream(path)) {
      IOUtils.copy(fis, os);
    } finally {
      IOUtils.closeQuietly(os);
    }
  }

  /**
   * Writes byte array data into the application’s import directory.
   *
   * @param data file data
   * @return saved relative file path
   */
  public String writeImportBytes(byte[] data) throws IOException {
    return writeBytes(data, appConfig.getImportPath());
  }

  /**
   * Writes byte array data into a given directory.
   *
   * @param data file data
   * @param uploadDir target directory
   * @return saved relative file path (e.g. /profile/upload/2025/11/12/uuid.png)
   */
  public String writeBytes(byte[] data, String uploadDir) throws IOException {
    String extension = getFileExtensionFromBytes(data);
    String fileName =
        String.format("%s/%s.%s", DateEnhancedUtil.datePath(), IdUtils.fastUUID(), extension);

    File targetFile = FileUploadUtils.getAbsoluteFile(uploadDir, fileName);
    try (OutputStream fos = new FileOutputStream(targetFile)) {
      fos.write(data);
    }

    // reuse consistent relative-path generator
    return resolveRelativePath(uploadDir, fileName);
  }

  // ------------------------------------------------------------------------
  // ⚙️ Path Handling
  // ------------------------------------------------------------------------

  /** Generates a /profile/... relative path using AppConfig. */
  private String resolveRelativePath(String uploadDir, String fileName) {
    String profilePath = appConfig.getProfile();
    if (uploadDir.startsWith(profilePath)) {
      String relative = uploadDir.substring(profilePath.length()).replace("\\", "/");
      return "/profile" + relative + "/" + fileName;
    }
    return uploadDir + "/" + fileName;
  }

  /** Removes the /profile prefix (for internal file handling). */
  public static String stripPrefix(String filePath) {
    return StringUtils.substringAfter(filePath, Constants.RESOURCE_PREFIX);
  }

  /** Deletes a file safely. */
  public static boolean deleteFile(String filePath) {
    File file = new File(filePath);
    return file.isFile() && file.exists() && file.delete();
  }

  // ------------------------------------------------------------------------
  // 🧩 Validation Utilities
  // ------------------------------------------------------------------------

  /** Validates filename pattern. */
  public static boolean isValidFilename(String filename) {
    return filename != null && filename.matches(FILENAME_PATTERN);
  }

  /** Checks if file type is allowed for download. */
  public static boolean checkAllowDownload(String resource) {
    if (StringUtils.contains(resource, "..")) {
      return false; // prevent directory traversal
    }
    String fileType = FileTypeUtils.getFileType(resource);
    return MimeTypeUtils.isAllowedExtension(fileType);
  }

  // ------------------------------------------------------------------------
  // 🌐 HTTP Download Headers
  // ------------------------------------------------------------------------

  /** Encodes filename per browser type. */
  public static String setFileDownloadHeader(HttpServletRequest request, String fileName)
      throws UnsupportedEncodingException {
    String agent = request.getHeader("USER-AGENT");
    if (agent == null) {
      return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }

    if (agent.contains("MSIE")) {
      return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", " ");
    } else if (agent.contains("Firefox")) {
      return new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
    } else {
      return URLEncoder.encode(fileName, StandardCharsets.UTF_8);
    }
  }

  /** Adds HTTP headers to support file download. */
  public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName)
      throws UnsupportedEncodingException {
    String encoded = percentEncode(realFileName);
    String disposition = "attachment; filename=" + encoded + ";" + "filename*=utf-8''" + encoded;
    response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
    response.setHeader("Content-Disposition", disposition);
    response.setHeader("download-filename", encoded);
  }

  /** URL-safe percent encoding for file names. */
  public static String percentEncode(String s) throws UnsupportedEncodingException {
    return URLEncoder.encode(s, StandardCharsets.UTF_8.toString()).replace("+", "%20");
  }

  // ------------------------------------------------------------------------
  // 🖼️ File Metadata Helpers
  // ------------------------------------------------------------------------

  /** Determines file extension from file header bytes. */
  public static String getFileExtensionFromBytes(byte[] data) {
    if (data == null || data.length < 10) return "bin";

    if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') return "gif";
    if (data[6] == 'J' && data[7] == 'F' && data[8] == 'I' && data[9] == 'F') return "jpg";
    if (data[0] == 'B' && data[1] == 'M') return "bmp";
    if (data[1] == 'P' && data[2] == 'N' && data[3] == 'G') return "png";

    return "jpg";
  }

  /** Extracts only the file name (without path). */
  public static String getName(String fileName) {
    if (fileName == null) return null;
    return fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
  }

  /** Extracts the base name without extension. */
  public static String getNameWithoutExtension(String fileName) {
    return fileName == null ? null : FilenameUtils.getBaseName(fileName);
  }
}
