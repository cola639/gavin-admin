package com.api.system.domain.baseline.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaselineTaskSummary {

  private Long taskId;
  private Long requestId;
  private String stepCode;
  private String assigneeRole;
  private String assigneeId;
  private String assigneeName;
  private String status;
  private String decision;
  private String comment;
  private LocalDateTime createdAt;
  private LocalDateTime actedAt;

  private String requestNo;
  private String title;
  private String baselineStatus;
  private String approvalStatus;
  private String pendingActionType;
  private String currentStep;

  public BaselineTaskSummary(
      Long taskId,
      Long requestId,
      String stepCode,
      String assigneeRole,
      String assigneeId,
      String assigneeName,
      String status,
      String decision,
      String comment,
      LocalDateTime createdAt,
      LocalDateTime actedAt,
      String requestNo,
      String title,
      String baselineStatus,
      String approvalStatus,
      String pendingActionType,
      String currentStep) {
    this.taskId = taskId;
    this.requestId = requestId;
    this.stepCode = stepCode;
    this.assigneeRole = assigneeRole;
    this.assigneeId = assigneeId;
    this.assigneeName = assigneeName;
    this.status = status;
    this.decision = decision;
    this.comment = comment;
    this.createdAt = createdAt;
    this.actedAt = actedAt;
    this.requestNo = requestNo;
    this.title = title;
    this.baselineStatus = baselineStatus;
    this.approvalStatus = approvalStatus;
    this.pendingActionType = pendingActionType;
    this.currentStep = currentStep;
  }
}
