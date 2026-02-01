package com.api.system.service;

import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.common.utils.uuid.IdUtils;
import com.api.framework.exception.ServiceException;
import com.api.system.domain.baseline.BaselineEvent;
import com.api.system.domain.baseline.BaselineRequest;
import com.api.system.domain.baseline.BaselineTask;
import com.api.system.domain.baseline.BaselineWorkflowConstants;
import com.api.system.domain.baseline.dto.BaselineCreateRequest;
import com.api.system.domain.baseline.dto.BaselinePostActionRequest;
import com.api.system.domain.baseline.dto.BaselineSubmitRequest;
import com.api.system.domain.baseline.dto.BaselineSummary;
import com.api.system.domain.baseline.dto.BaselineTaskSummary;
import com.api.system.domain.baseline.dto.TaskDecisionRequest;
import com.api.system.repository.BaselineEventRepository;
import com.api.system.repository.BaselineRequestRepository;
import com.api.system.repository.BaselineTaskRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaselineWorkflowService {

  private static final String SYSTEM_NAME = "System";

  private final BaselineRequestRepository requestRepository;
  private final BaselineTaskRepository taskRepository;
  private final BaselineEventRepository eventRepository;

  public Page<BaselineSummary> listBaselines(
      String status,
      String approvalStatus,
      String ownerId,
      String reviewerId,
      String pendingActionType,
      Pageable pageable) {

    SpecificationBuilder<BaselineRequest> builder = SpecificationBuilder.<BaselineRequest>builder();

    builder
        .eq("status", normalizeUpper(status))
        .eq("approvalStatus", normalizeUpper(approvalStatus))
        .eq("ownerId", trimToNull(ownerId))
        .eq("reviewerId", trimToNull(reviewerId))
        .eq("pendingActionType", normalizeUpper(pendingActionType));

    Specification<BaselineRequest> spec = builder;
    Page<BaselineRequest> page = requestRepository.findAll(spec, pageable);
    return page.map(BaselineSummary::fromEntity);
  }

  public BaselineRequest getBaseline(Long id) {
    return requestRepository
        .findById(id)
        .orElseThrow(() -> new ServiceException("Baseline not found for id=" + id));
  }

  public List<BaselineEvent> getTimeline(Long requestId) {
    getBaseline(requestId);
    return eventRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
  }

  @Transactional
  public BaselineRequest createDraft(BaselineCreateRequest request) {
    LocalDateTime now = LocalDateTime.now();
    BaselineRequest baseline =
        BaselineRequest.builder()
            .requestNo("TEMP-" + IdUtils.fastSimpleUUID())
            .title(request.getTitle().trim())
            .ownerId(request.getOwnerId().trim())
            .ownerName(request.getOwnerName().trim())
            .reviewerId(trimToNull(request.getReviewerId()))
            .reviewerName(trimToNull(request.getReviewerName()))
            .status(BaselineWorkflowConstants.STATUS_DRAFT)
            .approvalStatus(BaselineWorkflowConstants.APPROVAL_NOT_REQUIRED)
            .currentStep(BaselineWorkflowConstants.STEP_OWNER_SUBMIT)
            .pendingActionType(null)
            .submittedAt(null)
            .lastReviewedAt(null)
            .publishedAt(null)
            .retiredAt(null)
            .version(0)
            .createdBy(request.getOwnerId().trim())
            .createdAt(now)
            .updatedBy(request.getOwnerId().trim())
            .updatedAt(now)
            .build();

    BaselineRequest saved = requestRepository.save(baseline);
    saved.setRequestNo(generateRequestNo(saved.getId()));
    saved = requestRepository.save(saved);

    log.info(
        "Baseline draft created requestId={} requestNo={} ownerId={}",
        saved.getId(),
        saved.getRequestNo(),
        saved.getOwnerId());

    return saved;
  }

  @Transactional
  public BaselineRequest submitBaseline(Long id, BaselineSubmitRequest request) {
    BaselineRequest baseline = getBaseline(id);

    if (!BaselineWorkflowConstants.STATUS_DRAFT.equals(baseline.getStatus())
        || !BaselineWorkflowConstants.STEP_OWNER_SUBMIT.equals(baseline.getCurrentStep())) {
      throw new ServiceException("Baseline is not in a submittable draft state");
    }

    String reviewerId = resolveValue(request.getReviewerId(), baseline.getReviewerId());
    String reviewerName = resolveValue(request.getReviewerName(), baseline.getReviewerName());

    if (!StringUtils.hasText(reviewerId) || !StringUtils.hasText(reviewerName)) {
      throw new ServiceException("Reviewer information is required to submit");
    }

    LocalDateTime now = LocalDateTime.now();

    baseline.setReviewerId(reviewerId);
    baseline.setReviewerName(reviewerName);
    baseline.setApprovalStatus(BaselineWorkflowConstants.APPROVAL_PENDING);
    baseline.setCurrentStep(BaselineWorkflowConstants.STEP_SME_REVIEW);
    baseline.setSubmittedAt(now);
    baseline.setUpdatedBy(resolveActorId(request.getActorId(), baseline.getOwnerId()));
    baseline.setUpdatedAt(now);

    requestRepository.save(baseline);

    BaselineTask task =
        BaselineTask.builder()
            .requestId(baseline.getId())
            .stepCode(BaselineWorkflowConstants.STEP_SME_REVIEW)
            .assigneeRole(BaselineWorkflowConstants.ROLE_SME)
            .assigneeId(reviewerId)
            .assigneeName(reviewerName)
            .status(BaselineWorkflowConstants.TASK_STATUS_PENDING)
            .createdAt(now)
            .build();

    task = taskRepository.save(task);

    String actorId = resolveActorId(request.getActorId(), baseline.getOwnerId());
    String actorName = resolveActorName(request.getActorName(), baseline.getOwnerName());
    String actorRole = resolveActorRole(request.getActorRole(), BaselineWorkflowConstants.ROLE_OWNER);

    saveEvent(
        baseline.getId(),
        task.getId(),
        BaselineWorkflowConstants.EVENT_SUBMIT,
        actorRole,
        actorId,
        actorName,
        "Submitted baseline request");

    saveEvent(
        baseline.getId(),
        task.getId(),
        BaselineWorkflowConstants.EVENT_REVIEW_REQUESTED,
        BaselineWorkflowConstants.ROLE_SYSTEM,
        null,
        SYSTEM_NAME,
        "Review requested for baseline");

    log.info(
        "Baseline submitted requestId={} requestNo={} reviewerId={} actorId={}",
        baseline.getId(),
        baseline.getRequestNo(),
        reviewerId,
        actorId);

    return baseline;
  }

  @Transactional
  public BaselineRequest requestPostAction(Long id, BaselinePostActionRequest request) {
    BaselineRequest baseline = getBaseline(id);

    if (!BaselineWorkflowConstants.STATUS_PUBLISHED.equals(baseline.getStatus())) {
      throw new ServiceException("Post-action requests are only allowed for published baselines");
    }

    if (StringUtils.hasText(baseline.getPendingActionType())) {
      throw new ServiceException("A post-action request is already pending for this baseline");
    }

    String actionType = normalizeUpper(request.getActionType());
    if (!BaselineWorkflowConstants.PENDING_ACTION_RETIRE.equals(actionType)
        && !BaselineWorkflowConstants.PENDING_ACTION_DELETE.equals(actionType)) {
      throw new ServiceException("Unsupported post-action type: " + request.getActionType());
    }

    String reviewerId = resolveValue(request.getReviewerId(), baseline.getReviewerId());
    String reviewerName = resolveValue(request.getReviewerName(), baseline.getReviewerName());

    if (!StringUtils.hasText(reviewerId) || !StringUtils.hasText(reviewerName)) {
      throw new ServiceException("Reviewer information is required for post-action review");
    }

    LocalDateTime now = LocalDateTime.now();

    baseline.setPendingActionType(actionType);
    baseline.setReviewerId(reviewerId);
    baseline.setReviewerName(reviewerName);
    baseline.setUpdatedBy(resolveActorId(request.getActorId(), baseline.getOwnerId()));
    baseline.setUpdatedAt(now);
    requestRepository.save(baseline);

    BaselineTask task =
        BaselineTask.builder()
            .requestId(baseline.getId())
            .stepCode(BaselineWorkflowConstants.STEP_POST_ACTION_REVIEW)
            .assigneeRole(BaselineWorkflowConstants.ROLE_SME)
            .assigneeId(reviewerId)
            .assigneeName(reviewerName)
            .status(BaselineWorkflowConstants.TASK_STATUS_PENDING)
            .createdAt(now)
            .build();

    task = taskRepository.save(task);

    String actorId = resolveActorId(request.getActorId(), baseline.getOwnerId());
    String actorName = resolveActorName(request.getActorName(), baseline.getOwnerName());
    String actorRole = resolveActorRole(request.getActorRole(), BaselineWorkflowConstants.ROLE_OWNER);

    String message =
        StringUtils.hasText(request.getReason())
            ? String.format("Requested %s: %s", actionType, request.getReason())
            : String.format("Requested %s", actionType);

    saveEvent(
        baseline.getId(),
        task.getId(),
        BaselineWorkflowConstants.EVENT_POST_ACTION_REQUEST,
        actorRole,
        actorId,
        actorName,
        message);

    log.info(
        "Baseline post-action requested requestId={} requestNo={} action={} actorId={}",
        baseline.getId(),
        baseline.getRequestNo(),
        actionType,
        actorId);

    return baseline;
  }

  public Page<BaselineTaskSummary> listMyTasks(
      String assigneeId,
      String assigneeRole,
      String status,
      String stepCode,
      Pageable pageable) {
    return taskRepository.findTaskSummaries(
        trimToNull(assigneeId),
        normalizeUpper(assigneeRole),
        normalizeUpper(status),
        normalizeUpper(stepCode),
        pageable);
  }

  @Transactional
  public BaselineRequest decideTask(Long taskId, TaskDecisionRequest request) {
    BaselineTask task =
        taskRepository
            .findById(taskId)
            .orElseThrow(() -> new ServiceException("Task not found for id=" + taskId));

    if (!BaselineWorkflowConstants.TASK_STATUS_PENDING.equals(task.getStatus())) {
      throw new ServiceException("Task is not pending and cannot be decided");
    }

    String decision = normalizeUpper(request.getDecision());
    if (!BaselineWorkflowConstants.DECISION_APPROVE.equals(decision)
        && !BaselineWorkflowConstants.DECISION_REJECT.equals(decision)) {
      throw new ServiceException("Unsupported decision: " + request.getDecision());
    }

    BaselineRequest baseline = getBaseline(task.getRequestId());
    LocalDateTime now = LocalDateTime.now();

    String actorId = resolveActorId(request.getActorId(), task.getAssigneeId());
    String actorName = resolveActorName(request.getActorName(), task.getAssigneeName());
    String actorRole = resolveActorRole(request.getActorRole(), BaselineWorkflowConstants.ROLE_SME);

    task.setDecision(decision);
    task.setComment(trimToNull(request.getComment()));
    task.setStatus(
        BaselineWorkflowConstants.DECISION_APPROVE.equals(decision)
            ? BaselineWorkflowConstants.TASK_STATUS_APPROVED
            : BaselineWorkflowConstants.TASK_STATUS_REJECTED);
    task.setActedAt(now);
    taskRepository.save(task);

    if (BaselineWorkflowConstants.STEP_SME_REVIEW.equals(task.getStepCode())) {
      handleInitialReviewDecision(baseline, task, decision, request.getComment(), actorRole, actorId, actorName, now);
    } else if (BaselineWorkflowConstants.STEP_POST_ACTION_REVIEW.equals(task.getStepCode())) {
      handlePostActionDecision(baseline, task, decision, request.getComment(), actorRole, actorId, actorName, now);
    } else {
      throw new ServiceException("Unsupported task step: " + task.getStepCode());
    }

    log.info(
        "Task decision recorded taskId={} requestId={} decision={} actorId={}",
        taskId,
        baseline.getId(),
        decision,
        actorId);

    return baseline;
  }

  private void handleInitialReviewDecision(
      BaselineRequest baseline,
      BaselineTask task,
      String decision,
      String comment,
      String actorRole,
      String actorId,
      String actorName,
      LocalDateTime now) {

    baseline.setLastReviewedAt(now);
    baseline.setUpdatedBy(actorId);
    baseline.setUpdatedAt(now);

    if (BaselineWorkflowConstants.DECISION_APPROVE.equals(decision)) {
      baseline.setApprovalStatus(BaselineWorkflowConstants.APPROVAL_APPROVED);
      baseline.setStatus(BaselineWorkflowConstants.STATUS_PUBLISHED);
      baseline.setCurrentStep(BaselineWorkflowConstants.STEP_END);
      baseline.setPublishedAt(now);

      requestRepository.save(baseline);

      saveEvent(
          baseline.getId(),
          task.getId(),
          BaselineWorkflowConstants.EVENT_APPROVE,
          actorRole,
          actorId,
          actorName,
          buildDecisionMessage("Approved baseline request", comment));

      saveEvent(
          baseline.getId(),
          task.getId(),
          BaselineWorkflowConstants.EVENT_PUBLISH,
          BaselineWorkflowConstants.ROLE_SYSTEM,
          null,
          SYSTEM_NAME,
          "Published baseline");
    } else {
      baseline.setApprovalStatus(BaselineWorkflowConstants.APPROVAL_REJECTED);
      baseline.setCurrentStep(BaselineWorkflowConstants.STEP_END);
      if (!BaselineWorkflowConstants.STATUS_RETIRED.equals(baseline.getStatus())) {
        baseline.setStatus(BaselineWorkflowConstants.STATUS_DRAFT);
      }

      requestRepository.save(baseline);

      saveEvent(
          baseline.getId(),
          task.getId(),
          BaselineWorkflowConstants.EVENT_REJECT,
          actorRole,
          actorId,
          actorName,
          buildDecisionMessage("Rejected baseline request", comment));
    }
  }

  private void handlePostActionDecision(
      BaselineRequest baseline,
      BaselineTask task,
      String decision,
      String comment,
      String actorRole,
      String actorId,
      String actorName,
      LocalDateTime now) {

    String actionType = baseline.getPendingActionType();
    if (!StringUtils.hasText(actionType)) {
      throw new ServiceException("No pending post-action found for this baseline");
    }

    baseline.setLastReviewedAt(now);
    baseline.setUpdatedBy(actorId);
    baseline.setUpdatedAt(now);

    if (BaselineWorkflowConstants.DECISION_APPROVE.equals(decision)) {
      baseline.setPendingActionType(null);
      baseline.setStatus(BaselineWorkflowConstants.STATUS_RETIRED);
      baseline.setRetiredAt(now);
      requestRepository.save(baseline);

      saveEvent(
          baseline.getId(),
          task.getId(),
          BaselineWorkflowConstants.EVENT_POST_ACTION_APPROVE,
          actorRole,
          actorId,
          actorName,
          buildDecisionMessage("Approved " + actionType + " request", comment));
    } else {
      baseline.setPendingActionType(null);
      requestRepository.save(baseline);

      saveEvent(
          baseline.getId(),
          task.getId(),
          BaselineWorkflowConstants.EVENT_POST_ACTION_REJECT,
          actorRole,
          actorId,
          actorName,
          buildDecisionMessage("Rejected " + actionType + " request", comment));
    }
  }

  private void saveEvent(
      Long requestId,
      Long taskId,
      String eventType,
      String actorRole,
      String actorId,
      String actorName,
      String message) {
    BaselineEvent event =
        BaselineEvent.builder()
            .requestId(requestId)
            .taskId(taskId)
            .eventType(eventType)
            .actorRole(actorRole)
            .actorId(actorId)
            .actorName(actorName)
            .message(message)
            .createdAt(LocalDateTime.now())
            .build();
    eventRepository.save(event);
  }

  private String resolveActorId(String provided, String fallback) {
    String value = trimToNull(provided);
    return value != null ? value : trimToNull(fallback);
  }

  private String resolveActorName(String provided, String fallback) {
    String value = trimToNull(provided);
    if (value != null) {
      return value;
    }
    String fallbackValue = trimToNull(fallback);
    return fallbackValue != null ? fallbackValue : SYSTEM_NAME;
  }

  private String resolveActorRole(String provided, String fallback) {
    String value = normalizeUpper(provided);
    return value != null ? value : fallback;
  }

  private String resolveValue(String provided, String fallback) {
    String value = trimToNull(provided);
    return value != null ? value : trimToNull(fallback);
  }

  private String buildDecisionMessage(String baseMessage, String comment) {
    if (StringUtils.hasText(comment)) {
      return baseMessage + ": " + comment.trim();
    }
    return baseMessage;
  }

  private String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return value.trim();
  }

  private String normalizeUpper(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    return value.trim().toUpperCase(Locale.ROOT);
  }

  private String generateRequestNo(Long id) {
    String year = String.valueOf(LocalDate.now().getYear());
    return String.format("BR-%s-%04d", year, id);
  }
}
