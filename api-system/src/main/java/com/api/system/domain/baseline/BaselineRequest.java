package com.api.system.domain.baseline;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "baseline_request")
public class BaselineRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_no", nullable = false, length = 64)
  private String requestNo;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "owner_id", nullable = false, length = 64)
  private String ownerId;

  @Column(name = "owner_name", nullable = false, length = 128)
  private String ownerName;

  @Column(name = "reviewer_id", length = 64)
  private String reviewerId;

  @Column(name = "reviewer_name", length = 128)
  private String reviewerName;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "approval_status", nullable = false, length = 32)
  private String approvalStatus;

  @Column(name = "current_step", nullable = false, length = 32)
  private String currentStep;

  @Column(name = "pending_action_type", length = 32)
  private String pendingActionType;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "submitted_at")
  private LocalDateTime submittedAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "last_reviewed_at")
  private LocalDateTime lastReviewedAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "retired_at")
  private LocalDateTime retiredAt;

  @Version
  @Column(name = "version")
  private Integer version;

  @Column(name = "created_by", length = 64)
  private String createdBy;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_by", length = 64)
  private String updatedBy;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (version == null) {
      version = 0;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
