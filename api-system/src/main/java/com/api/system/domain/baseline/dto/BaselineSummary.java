package com.api.system.domain.baseline.dto;

import com.api.system.domain.baseline.BaselineRequest;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaselineSummary {

  private Long id;
  private String requestNo;
  private String title;
  private String status;
  private String approvalStatus;
  private String currentStep;
  private String pendingActionType;
  private String ownerId;
  private String ownerName;
  private String reviewerId;
  private String reviewerName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime publishedAt;

  public static BaselineSummary fromEntity(BaselineRequest entity) {
    if (entity == null) {
      return null;
    }
    return BaselineSummary.builder()
        .id(entity.getId())
        .requestNo(entity.getRequestNo())
        .title(entity.getTitle())
        .status(entity.getStatus())
        .approvalStatus(entity.getApprovalStatus())
        .currentStep(entity.getCurrentStep())
        .pendingActionType(entity.getPendingActionType())
        .ownerId(entity.getOwnerId())
        .ownerName(entity.getOwnerName())
        .reviewerId(entity.getReviewerId())
        .reviewerName(entity.getReviewerName())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .publishedAt(entity.getPublishedAt())
        .build();
  }
}
