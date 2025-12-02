package com.api.boot.controller.common;

import com.api.common.config.AppConfig;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.StringUtils;
import com.api.common.utils.file.FileUploadUtils;
import com.api.common.utils.file.FileUtils;
import com.api.common.utils.file.MimeTypeUtils;
import com.api.framework.config.ServerConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 🌐 Common File Controller
 *
 * <p>Handles file uploads and downloads for local resources using {@link FileUploadUtils} and
 * {@link FileUtils}.
 *
 * <p>Refactored for Spring Boot 3.5 and Java 17. Uses dependency injection instead of static
 * configuration from legacy RuoYi.
 */
@Slf4j
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

  private final ServerConfig serverConfig;
  private final AppConfig appConfig;
  private final FileUploadUtils fileUploadUtils;
  private final FileUtils fileUtils;

  private static final String FILE_DELIMITER = ",";

  // ------------------------------------------------------------------------
  // 📥 File Download
  // ------------------------------------------------------------------------

  /**
   * Handles file download requests.
   *
   * @param fileName The file name to download.
   * @param delete Whether to delete the file after download.
   */
  @GetMapping("/download")
  public void downloadFile(
      @RequestParam String fileName,
      @RequestParam(required = false, defaultValue = "false") boolean delete,
      HttpServletResponse response,
      HttpServletRequest request) {

    try {
      if (!FileUtils.checkAllowDownload(fileName)) {
        throw new IllegalArgumentException(StringUtils.format("Illegal file name: {}", fileName));
      }

      String realFileName =
          System.currentTimeMillis() + "_" + StringUtils.substringAfter(fileName, "_");
      String filePath = appConfig.getDownloadPath() + "/" + fileName;

      response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      FileUtils.setAttachmentResponseHeader(response, realFileName);
      FileUtils.writeBytes(filePath, response.getOutputStream());

      if (delete) {
        FileUtils.deleteFile(filePath);
      }

      log.info("✅ File [{}] downloaded successfully", fileName);
    } catch (Exception e) {
      log.error("❌ Failed to download file: {}", fileName, e);
    }
  }

  /**
   * Handle avatar file upload
   *
   * @param file The uploaded multipart file.
   * @return A JSON response containing file metadata.
   */
  @PostMapping("/upload-avatar")
  public AjaxResult uploadAvatar(@RequestParam MultipartFile file) {
    try {
      String fileName =
          fileUploadUtils.upload(
              appConfig.getAvatarPath(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSIONS);
      String fileUrl = serverConfig.getUrl() + fileName;

      AjaxResult result = AjaxResult.success("File uploaded successfully");
      result.put("url", fileUrl);
      result.put("fileName", fileName);
      result.put("newFileName", FileUtils.getName(fileName));
      result.put("originalFilename", file.getOriginalFilename());
      return result;
    } catch (Exception e) {
      log.error("❌ File upload failed: {}", file.getOriginalFilename(), e);
      return AjaxResult.error("Upload failed: " + e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // 📤 Single File Upload
  // ------------------------------------------------------------------------

  /**
   * Handles single file uploads.
   *
   * @param file The uploaded multipart file.
   * @return A JSON response containing file metadata.
   */
  @PostMapping("/upload")
  public AjaxResult uploadFile(@RequestParam MultipartFile file) {
    try {
      String fileName = fileUploadUtils.upload(file);
      String fileUrl = serverConfig.getUrl() + fileName;

      AjaxResult result = AjaxResult.success("File uploaded successfully");
      result.put("url", fileUrl);
      result.put("fileName", fileName);
      result.put("newFileName", FileUtils.getName(fileName));
      result.put("originalFilename", file.getOriginalFilename());
      return result;
    } catch (Exception e) {
      log.error("❌ File upload failed: {}", file.getOriginalFilename(), e);
      return AjaxResult.error("Upload failed: " + e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // 📤 Multiple File Upload
  // ------------------------------------------------------------------------

  /**
   * Handles multiple file uploads.
   *
   * @param files The list of uploaded files.
   * @return A JSON response containing metadata for all uploaded files.
   */
  @PostMapping("/uploads")
  public AjaxResult uploadFiles(@RequestParam List<MultipartFile> files) {
    List<String> urls = new ArrayList<>();
    List<String> fileNames = new ArrayList<>();
    List<String> newFileNames = new ArrayList<>();
    List<String> originalFilenames = new ArrayList<>();

    try {
      for (MultipartFile file : files) {
        String fileName = fileUploadUtils.upload(file);
        String fileUrl = serverConfig.getUrl() + fileName;

        urls.add(fileUrl);
        fileNames.add(fileName);
        newFileNames.add(FileUtils.getName(fileName));
        originalFilenames.add(file.getOriginalFilename());
      }

      AjaxResult result = AjaxResult.success("Files uploaded successfully");
      result.put("urls", String.join(FILE_DELIMITER, urls));
      result.put("fileNames", String.join(FILE_DELIMITER, fileNames));
      result.put("newFileNames", String.join(FILE_DELIMITER, newFileNames));
      result.put("originalFilenames", String.join(FILE_DELIMITER, originalFilenames));
      return result;
    } catch (Exception e) {
      log.error("❌ Failed to upload multiple files", e);
      return AjaxResult.error("Upload failed: " + e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // 📦 Local Resource Download
  // ------------------------------------------------------------------------

  /**
   * Downloads a local resource file by its path.
   *
   * @param resource The resource path (e.g. /profile/upload/2025/11/12/test.png).
   */
  @GetMapping("/download/resource")
  public void downloadResource(
      @RequestParam String resource, HttpServletRequest request, HttpServletResponse response) {
    try {
      if (!FileUtils.checkAllowDownload(resource)) {
        throw new IllegalArgumentException(
            StringUtils.format("Illegal resource path: {}", resource));
      }

      String localPath = appConfig.getProfile();
      String downloadPath = localPath + FileUtils.stripPrefix(resource);
      String downloadName = StringUtils.substringAfterLast(downloadPath, "/");

      response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      FileUtils.setAttachmentResponseHeader(response, downloadName);
      FileUtils.writeBytes(downloadPath, response.getOutputStream());

      log.info("✅ Resource [{}] downloaded successfully", resource);
    } catch (IOException e) {
      log.error("❌ I/O error during resource download: {}", resource, e);
    } catch (Exception e) {
      log.error("❌ Resource download failed: {}", resource, e);
    }
  }
}
