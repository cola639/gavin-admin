package com.api.system.domain.baseline;

public final class BaselineWorkflowConstants {

  private BaselineWorkflowConstants() {}

  public static final String STATUS_DRAFT = "DRAFT";
  public static final String STATUS_PUBLISHED = "PUBLISHED";
  public static final String STATUS_RETIRED = "RETIRED";

  public static final String APPROVAL_PENDING = "PENDING";
  public static final String APPROVAL_APPROVED = "APPROVED";
  public static final String APPROVAL_REJECTED = "REJECTED";
  public static final String APPROVAL_NOT_REQUIRED = "NOT_REQUIRED";

  public static final String STEP_OWNER_SUBMIT = "OWNER_SUBMIT";
  public static final String STEP_SME_REVIEW = "SME_REVIEW";
  public static final String STEP_POST_ACTION_REVIEW = "POST_ACTION_REVIEW";
  public static final String STEP_END = "END";

  public static final String PENDING_ACTION_RETIRE = "RETIRE";
  public static final String PENDING_ACTION_DELETE = "DELETE";

  public static final String TASK_STATUS_PENDING = "PENDING";
  public static final String TASK_STATUS_APPROVED = "APPROVED";
  public static final String TASK_STATUS_REJECTED = "REJECTED";
  public static final String TASK_STATUS_CANCELLED = "CANCELLED";

  public static final String DECISION_APPROVE = "APPROVE";
  public static final String DECISION_REJECT = "REJECT";

  public static final String ROLE_OWNER = "OWNER";
  public static final String ROLE_SME = "CYBER_SME";
  public static final String ROLE_SYSTEM = "SYSTEM";

  public static final String EVENT_SUBMIT = "SUBMIT";
  public static final String EVENT_REVIEW_REQUESTED = "REVIEW_REQUESTED";
  public static final String EVENT_APPROVE = "APPROVE";
  public static final String EVENT_REJECT = "REJECT";
  public static final String EVENT_PUBLISH = "PUBLISH";
  public static final String EVENT_POST_ACTION_REQUEST = "POST_ACTION_REQUEST";
  public static final String EVENT_POST_ACTION_APPROVE = "POST_ACTION_APPROVE";
  public static final String EVENT_POST_ACTION_REJECT = "POST_ACTION_REJECT";
}
