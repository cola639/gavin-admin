package com.api.common.utils.file;

import com.api.common.config.AppConfig;
import com.api.common.constant.Constants;
import com.api.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Image utility class for reading images from both local file storage and remote URLs.
 *
 * <p>This class supports:
 *
 * <ul>
 *   <li>Reading images as byte arrays or input streams
 *   <li>Handling both HTTP(S) and local filesystem paths
 *   <li>Automatic integration with configured {@link AppConfig#getProfile()} path
 * </ul>
 *
 * <p>Compatible with Spring Boot 3.5 and Java 17.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUtils {

  private final AppConfig appConfig;

  // ------------------------------------------------------------------------
  // 🖼️ Public Methods
  // ------------------------------------------------------------------------

  /**
   * Reads an image as byte array.
   *
   * @param imagePath the image path (can be HTTP URL or local path under /profile)
   * @return byte[] image content, or {@code null} if an error occurs
   */
  public byte[] getImage(String imagePath) {
    try (InputStream is = getFile(imagePath)) {
      if (is == null) {
        log.warn("⚠️ Image not found or could not be read: {}", imagePath);
        return null;
      }
      return IOUtils.toByteArray(is);
    } catch (Exception e) {
      log.error("❌ Failed to load image [{}]", imagePath, e);
      return null;
    }
  }

  /**
   * Returns an {@link InputStream} for a given image path. Supports HTTP(S) URLs and local file
   * system paths.
   *
   * @param imagePath image path or URL
   * @return InputStream if successful, null otherwise
   */
  public InputStream getFile(String imagePath) {
    try {
      byte[] data = readFile(imagePath);
      return (data != null) ? new ByteArrayInputStream(data) : null;
    } catch (Exception e) {
      log.error("❌ Error while opening image file: {}", imagePath, e);
      return null;
    }
  }

  /**
   * Reads the image content as a byte array.
   *
   * @param pathOrUrl either a full URL (http/https) or a local relative path (under /profile)
   * @return byte array, or null if an error occurs
   */
  public byte[] readFile(String pathOrUrl) {
    try (InputStream in = openInputStream(pathOrUrl)) {
      if (in == null) {
        log.warn("⚠️ Unable to open input stream for {}", pathOrUrl);
        return null;
      }
      return IOUtils.toByteArray(in);
    } catch (IOException e) {
      log.error("❌ Failed to read image from {}", pathOrUrl, e);
      return null;
    }
  }

  // ------------------------------------------------------------------------
  // ⚙️ Internal Helpers
  // ------------------------------------------------------------------------

  /** Opens an InputStream for either HTTP or local file. */
  private InputStream openInputStream(String pathOrUrl) {
    try {
      if (StringUtils.isEmpty(pathOrUrl)) {
        log.warn("⚠️ Empty image path provided.");
        return null;
      }

      if (pathOrUrl.startsWith("http")) {
        // Remote image
        URL url = new URL(pathOrUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(60_000);
        conn.setDoInput(true);
        return conn.getInputStream();
      } else {
        // Local image (under configured profile directory)
        String basePath = appConfig.getProfile();
        String relativePath = StringUtils.substringAfter(pathOrUrl, Constants.RESOURCE_PREFIX);
        Path fullPath = Paths.get(basePath, relativePath);

        if (!Files.exists(fullPath)) {
          log.warn("⚠️ Local file not found: {}", fullPath);
          return null;
        }

        return Files.newInputStream(fullPath);
      }

    } catch (Exception e) {
      log.error("❌ Error opening input stream for {}", pathOrUrl, e);
      return null;
    }
  }
}
