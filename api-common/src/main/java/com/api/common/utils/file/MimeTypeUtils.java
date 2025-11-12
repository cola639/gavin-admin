package com.api.common.utils.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Utility class for handling MIME types and file extensions.
 *
 * <p>Provides predefined mappings for common file formats (images, videos, documents, archives) and
 * helper methods to determine file categories or resolve extensions from MIME type prefixes.
 *
 * <p>Example usage:
 *
 * <pre>
 *   MimeTypeUtils.getExtension("image/jpeg");  // → "jpeg"
 *   MimeTypeUtils.isImageExtension("png");      // → true
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MimeTypeUtils {

  // ------------------------------------------------------------------------
  // 🧩 MIME Type Constants
  // ------------------------------------------------------------------------

  public static final String IMAGE_PNG = "image/png";
  public static final String IMAGE_JPG = "image/jpg";
  public static final String IMAGE_JPEG = "image/jpeg";
  public static final String IMAGE_BMP = "image/bmp";
  public static final String IMAGE_GIF = "image/gif";

  // ------------------------------------------------------------------------
  // 📁 File Type Extension Groups
  // ------------------------------------------------------------------------

  public static final List<String> IMAGE_EXTENSIONS = List.of("bmp", "gif", "jpg", "jpeg", "png");
  public static final List<String> FLASH_EXTENSIONS = List.of("swf", "flv");
  public static final List<String> MEDIA_EXTENSIONS =
      List.of("swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg", "asf", "rm", "rmvb");
  public static final List<String> VIDEO_EXTENSIONS = List.of("mp4", "avi", "rmvb");

  public static final List<String> DEFAULT_ALLOWED_EXTENSIONS =
      List.of(
          // Images
          "bmp",
          "gif",
          "jpg",
          "jpeg",
          "png",
          // Documents
          "doc",
          "docx",
          "xls",
          "xlsx",
          "ppt",
          "pptx",
          "html",
          "htm",
          "txt",
          // Archives
          "rar",
          "zip",
          "gz",
          "bz2",
          // Video
          "mp4",
          "avi",
          "rmvb",
          // PDF
          "pdf");

  // ------------------------------------------------------------------------
  // 🧠 MIME ↔ Extension Mapping
  // ------------------------------------------------------------------------

  private static final Map<String, String> MIME_TO_EXTENSION = new HashMap<>();
  private static final Map<String, String> EXTENSION_TO_MIME = new HashMap<>();

  static {
    MIME_TO_EXTENSION.put(IMAGE_PNG, "png");
    MIME_TO_EXTENSION.put(IMAGE_JPG, "jpg");
    MIME_TO_EXTENSION.put(IMAGE_JPEG, "jpeg");
    MIME_TO_EXTENSION.put(IMAGE_BMP, "bmp");
    MIME_TO_EXTENSION.put(IMAGE_GIF, "gif");

    // Inverse mapping for convenience
    MIME_TO_EXTENSION.forEach((mime, ext) -> EXTENSION_TO_MIME.put(ext, mime));
  }

  // ------------------------------------------------------------------------
  // ⚙️ Public API
  // ------------------------------------------------------------------------

  /**
   * Returns the file extension corresponding to the given MIME type.
   *
   * @param mimeType MIME type (e.g. "image/png")
   * @return file extension (e.g. "png"), or an empty string if not recognized
   */
  public static String getExtension(String mimeType) {
    return MIME_TO_EXTENSION.getOrDefault(mimeType.toLowerCase(Locale.ROOT), "");
  }

  /**
   * Returns the MIME type corresponding to a given file extension.
   *
   * @param extension file extension (e.g. "png")
   * @return MIME type (e.g. "image/png"), or "application/octet-stream" if not recognized
   */
  public static String getMimeType(String extension) {
    return EXTENSION_TO_MIME.getOrDefault(
        extension.toLowerCase(Locale.ROOT), "application/octet-stream");
  }

  /** Checks if the given extension is an image type. */
  public static boolean isImageExtension(String extension) {
    return IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
  }

  /** Checks if the given extension is a video type. */
  public static boolean isVideoExtension(String extension) {
    return VIDEO_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
  }

  /** Checks if the given extension is allowed in uploads. */
  public static boolean isAllowedExtension(String extension) {
    return DEFAULT_ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
  }

  /** Returns all allowed extensions as a comma-separated string (for configuration or logs). */
  public static String allowedExtensionsAsString() {
    return String.join(", ", DEFAULT_ALLOWED_EXTENSIONS);
  }
}
