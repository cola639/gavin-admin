# 1) baseline_request (the main record)
create table baseline_request
(
    id               BIGINT primary key auto_increment,

    request_no       VARCHAR(64)  not null comment 'Business id, e.g. BR-2026-0001',
    title            VARCHAR(255) not null comment 'Baseline request title',

    -- Who raised it (Platform owner)
    owner_id         VARCHAR(64)  not null,
    owner_name       VARCHAR(128) not null,

    -- Assigned reviewer (Cyber SME) - optional if assigned later
    reviewer_id      VARCHAR(64)  null,
    reviewer_name    VARCHAR(128) null,

    -- Lifecycle status: what user sees
    status           VARCHAR(32)  not null comment 'DRAFT/PUBLISHED/RETIRED',
    -- Approval status: workflow state
    approval_status  VARCHAR(32)  not null comment 'PENDING/APPROVED/REJECTED/NOT_REQUIRED',

    current_step     VARCHAR(32)  not null comment 'OWNER_SUBMIT/SME_REVIEW/END',

    submitted_at     DATETIME     null,
    last_reviewed_at DATETIME     null,
    published_at     DATETIME     null,
    retired_at       DATETIME     null,

    version          INT          not null default 0,

    created_by       VARCHAR(64)  null,
    created_at       DATETIME     not null default current_timestamp,
    updated_by       VARCHAR(64)  null,
    updated_at       DATETIME     not null default current_timestamp on update current_timestamp,

    unique key uk_request_no (request_no),
    key idx_status_approval (status, approval_status),
    key idx_owner (owner_id),
    key idx_reviewer (reviewer_id)
) engine = InnoDB
  default charset = utf8mb4;

# 2) baseline_task (one review task per stage/person)
create table baseline_task
(
    id            BIGINT primary key auto_increment,
    request_id    BIGINT       not null,

    step_code     VARCHAR(32)  not null comment 'SME_REVIEW',
    assignee_role VARCHAR(32)  not null comment 'CYBER_SME',
    assignee_id   VARCHAR(64)  not null,
    assignee_name VARCHAR(128) null,

    status        VARCHAR(32)  not null comment 'PENDING/APPROVED/REJECTED/CANCELLED',
    decision      VARCHAR(16)  null comment 'APPROVE/REJECT',
    comment       VARCHAR(512) null,

    created_at    DATETIME     not null default current_timestamp,
    acted_at      DATETIME     null,

    key idx_request (request_id),
    key idx_assignee_status (assignee_id, status),

    constraint fk_task_request foreign key (request_id) references baseline_request (id)
) engine = InnoDB
  default charset = utf8mb4;


# 3) baseline_event (timeline / audit trail)
create table baseline_event
(
    id         BIGINT primary key auto_increment,
    request_id BIGINT       not null,
    task_id    BIGINT       null,

    event_type VARCHAR(32)  not null comment 'SUBMIT/REVIEW_REQUESTED/APPROVE/REJECT/PUBLISH/RETIRE/RECALL',
    actor_role VARCHAR(32)  not null comment 'OWNER/CYBER_SME/SYSTEM',
    actor_id   VARCHAR(64)  null,
    actor_name VARCHAR(128) not null,

    message    VARCHAR(512) not null,
    created_at DATETIME     not null default current_timestamp,

    key idx_request_time (request_id, created_at),

    constraint fk_event_request foreign key (request_id) references baseline_request (id)
) engine = InnoDB
  default charset = utf8mb4;


# 4) baseline_notification_outbox (email notifications, reliable)
create table baseline_notification_outbox
(
    id            BIGINT primary key auto_increment,
    request_id    BIGINT       not null,
    event_id      BIGINT       null,

    channel       VARCHAR(16)  not null default 'EMAIL',
    template_code VARCHAR(64)  not null comment 'e.g. BASELINE_SUBMITTED',
    to_address    VARCHAR(255) not null,
    subject       VARCHAR(255) not null,
    payload_json  JSON         null,

    status        VARCHAR(16)  not null default 'PENDING' comment 'PENDING/SENT/FAILED',
    retry_count   INT          not null default 0,
    last_error    VARCHAR(512) null,

    created_at    DATETIME     not null default current_timestamp,
    sent_at       DATETIME     null,

    key idx_status_time (status, created_at),
    key idx_request (request_id),

    constraint fk_notify_request foreign key (request_id) references baseline_request (id)
) engine = InnoDB
  default charset = utf8mb4;
