package com.api.common.domain.file;

import com.api.common.enums.FileVisibilityEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_file_object")
public class SysFileObject {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "file_id")
  private Long fileId;

  @Column(nullable = false, length = 128)
  private String bucket;

  @Column(name = "object_key", nullable = false, length = 512, unique = true)
  private String objectKey;

  @Column(name = "original_name", nullable = false, length = 255)
  private String originalName;

  @Column(name = "content_type", length = 128)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @Column(length = 128)
  private String etag;

  @Column(name = "owner_user_id", nullable = false)
  private Long ownerUserId;

  @Column(nullable = false, length = 32)
  private String category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private FileVisibilityEnum visibility;

  @Column(name = "biz_type", length = 64)
  private String bizType;

  @Column(name = "biz_id", length = 64)
  private String bizId;

  @Column(nullable = false)
  private boolean deleted;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @PrePersist
  public void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
