package com.api.boot.controller.common;

import com.api.common.domain.AjaxResult;
import com.api.common.domain.upload.*;
import com.api.system.service.upload.FileAppService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
public class FileController {

  private final FileAppService fileAppService;

  // ------------------------------------------------------------------------
  // Phase-1: Multipart upload
  // ------------------------------------------------------------------------

  @PostMapping("/upload")
  public AjaxResult upload(
      @RequestParam String category,
      @RequestParam MultipartFile file,
      @RequestParam(required = false) String bizType,
      @RequestParam(required = false) String bizId) {

    try {
      FileUploadResult result = fileAppService.uploadOne(category, file, bizType, bizId);
      return AjaxResult.success("File uploaded successfully", result);
    } catch (Exception e) {
      log.error(
          "Upload failed. category={}, originalName={}", category, file.getOriginalFilename(), e);
      return AjaxResult.error("Upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/uploads")
  public AjaxResult uploads(
      @RequestParam String category,
      @RequestParam List<MultipartFile> files,
      @RequestParam(required = false) String bizType,
      @RequestParam(required = false) String bizId) {

    try {
      List<FileUploadResult> results = fileAppService.uploadMany(category, files, bizType, bizId);
      return AjaxResult.success("Files uploaded successfully", results);
    } catch (Exception e) {
      log.error("Batch upload failed. category={}", category, e);
      return AjaxResult.error("Upload failed: " + e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // Phase-1: Access (presigned GET)
  // ------------------------------------------------------------------------

  @GetMapping("/file-url")
  public AjaxResult fileUrl(
      @RequestParam Long fileId, @RequestParam(required = false) Integer expirySeconds) {
    try {
      FileUrlResponse url = fileAppService.getFileUrl(fileId, expirySeconds);
      return AjaxResult.success("OK", url);
    } catch (Exception e) {
      log.error("Failed to get file url. fileId={}", fileId, e);
      return AjaxResult.error(e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // Phase-1: Delete
  // ------------------------------------------------------------------------

  @DeleteMapping("/files/{fileId}")
  public AjaxResult deleteOne(@PathVariable Long fileId) {
    try {
      fileAppService.deleteOne(fileId);
      return AjaxResult.success("Deleted");
    } catch (Exception e) {
      log.error("Delete failed. fileId={}", fileId, e);
      return AjaxResult.error(e.getMessage());
    }
  }

  @DeleteMapping("/files")
  public AjaxResult deleteBatch(@RequestBody @Valid BatchDeleteRequest request) {
    try {
      List<Long> deleted = fileAppService.deleteBatch(request.getFileIds());
      return AjaxResult.success("Deleted", deleted);
    } catch (Exception e) {
      log.error("Batch delete failed.", e);
      return AjaxResult.error(e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // Phase-2: Presigned PUT (browser direct upload)
  // ------------------------------------------------------------------------

  @PostMapping("/presign-put")
  public AjaxResult presignPut(@RequestBody @Valid PresignPutRequest request) {
    try {
      PresignPutResponse resp = fileAppService.presignPut(request);
      return AjaxResult.success("OK", resp);
    } catch (Exception e) {
      log.error(
          "Failed to presign PUT. category={}, originalName={}",
          request.getCategory(),
          request.getOriginalName(),
          e);
      return AjaxResult.error(e.getMessage());
    }
  }

  @PostMapping("/upload-confirm")
  public AjaxResult uploadConfirm(@RequestBody @Valid UploadConfirmRequest request) {
    try {
      FileUploadResult result = fileAppService.confirmUpload(request);
      return AjaxResult.success("OK", result);
    } catch (Exception e) {
      log.error("Upload confirm failed. fileId={}", request.getFileId(), e);
      return AjaxResult.error(e.getMessage());
    }
  }

  // ------------------------------------------------------------------------
  // Image access: stable image URL (browser can open / <img src="...">)
  // ------------------------------------------------------------------------

  @GetMapping("/image/{fileId}")
  public ResponseEntity<Void> image(
      @PathVariable Long fileId, @RequestParam(required = false) Integer expirySeconds) {

    try {
      FileUrlResponse url = fileAppService.getFileUrl(fileId, expirySeconds);

      HttpHeaders headers = new HttpHeaders();
      headers.setLocation(URI.create(url.getUrl()));

      // 302 redirect to presigned/public url
      return new ResponseEntity<>(headers, HttpStatus.FOUND);

    } catch (Exception e) {
      log.error("Failed to redirect image url. fileId={}", fileId, e);
      // Keep your AjaxResult style for errors? Browser expects an image/redirect.
      // Here we return 404 to avoid leaking details.
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
