Below is a **combined workflow design plan + MySQL DDLs** that supports:

* Timeline UI (events)
* Multi-stage approval (stages + tasks)
* **Recall** (withdraw) and **Drop** (cancel/terminate)
* **“Completed but drop requested (pending approval) still stays in Completed list”** (via post-action request + `void_state`)

---

# Design plan

## 1) Tables overview

### Definition (reusable templates)

* **`wf_definition`**: workflow template (code/version)
* **`wf_stage_definition`**: stage config (order/policy)
* **`wf_stage_assignee_def`**: who can approve each stage (user/group/role)

### Runtime (one request instance)

* **`wf_instance`**: one workflow request (ticket)
* **`wf_stage_instance`**: runtime stages created for this instance
* **`wf_task`**: per-approver tasks (multiple approvers supported)
* **`wf_event`**: audit/timeline log (drives UI)

### Post-actions (after completion)

* **`wf_post_action_request`**: supports “drop after completion needs approval”
* Also keep **`wf_instance.void_state`** as a denormalized “badge field” for fast lists/UI.

---

## 2) Recall & Drop behavior

### Recall (withdraw)

* Allowed only when instance is `SUBMITTED/IN_PROGRESS` and **no task acted yet** in current stage.
* Implementation: set `wf_instance.status = RECALLED`, cancel open tasks, mark current stage `RECALLED`, write `wf_event(RECALL)`.

### Drop (cancel/terminate)

* For **non-completed** instances: set `wf_instance.status = DROPPED`, cancel tasks, cancel active stages, write `wf_event(DROP)`.

### Drop request after completion (your special case)

* Do **NOT** change `wf_instance.status` (keep `COMPLETED` so it stays in Completed list).
* Create `wf_post_action_request(action_type=DROP, status=PENDING)`.
* Update `wf_instance.void_state = DROP_PENDING` (badge).
* When approved: set `wf_instance.void_state = DROPPED`, write events.
* When rejected: set `wf_instance.void_state = DROP_REJECTED`, write events.

---

# DDLs (MySQL / InnoDB / utf8mb4)

> Notes:
>
> * MySQL 5.7 doesn’t enforce CHECK well, so statuses are `varchar` + comments.
> * Add enums in code layer (recommended).
> * `json` is supported in MySQL 5.7+.

## 1) Workflow Definition

```sql
CREATE TABLE wf_definition (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  def_code      VARCHAR(64)  NOT NULL COMMENT 'Workflow code, e.g. USER_ACCESS',
  def_name      VARCHAR(128) NOT NULL,
  def_version   INT          NOT NULL COMMENT 'Template version',
  status        VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
  remark        VARCHAR(255) NULL,
  created_by    VARCHAR(64)  NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by    VARCHAR(64)  NULL,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_def_code_ver (def_code, def_version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow definition';
```

```sql
CREATE TABLE wf_stage_definition (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  def_id        BIGINT       NOT NULL,
  stage_no      INT          NOT NULL COMMENT '1..N',
  stage_code    VARCHAR(64)  NOT NULL,
  stage_name    VARCHAR(128) NOT NULL,
  policy        VARCHAR(16)  NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY (multi-approver)',
  is_terminal   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1 if last stage',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_def_stage_no (def_id, stage_no),
  KEY idx_def_id (def_id),
  CONSTRAINT fk_stage_def_def
    FOREIGN KEY (def_id) REFERENCES wf_definition(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow stage definition';
```

```sql
CREATE TABLE wf_stage_assignee_def (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  stage_def_id    BIGINT      NOT NULL,
  assignee_type   VARCHAR(16) NOT NULL COMMENT 'USER/GROUP/ROLE',
  assignee_id     VARCHAR(64) NOT NULL COMMENT 'UserId/GroupId/RoleCode',
  assignee_name   VARCHAR(128) NULL,
  created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_stage_def_id (stage_def_id),
  CONSTRAINT fk_assignee_stage_def
    FOREIGN KEY (stage_def_id) REFERENCES wf_stage_definition(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Stage assignee definition';
```

---

## 2) Workflow Runtime

```sql
CREATE TABLE wf_instance (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  def_id          BIGINT       NOT NULL COMMENT 'Reference wf_definition',
  business_type   VARCHAR(64)  NOT NULL COMMENT 'Business domain type',
  business_id     VARCHAR(64)  NOT NULL COMMENT 'External id e.g. RITM...',
  title           VARCHAR(255) NOT NULL,

  applicant_id    VARCHAR(64)  NOT NULL,
  applicant_name  VARCHAR(128) NOT NULL,

  status          VARCHAR(32)  NOT NULL DEFAULT 'DRAFT'
    COMMENT 'DRAFT/SUBMITTED/IN_PROGRESS/COMPLETED/REJECTED/RECALLED/DROPPED',
  current_stage_no INT         NOT NULL DEFAULT 0,
  current_stage_id BIGINT      NULL,

  -- For your special case (completed but drop request pending):
  void_state      VARCHAR(32)  NOT NULL DEFAULT 'NONE'
    COMMENT 'NONE/DROP_PENDING/DROPPED/DROP_REJECTED (usually meaningful when status=COMPLETED)',

  submitted_at    DATETIME NULL,
  ended_at        DATETIME NULL,

  payload_json    JSON     NULL COMMENT 'Optional business snapshot',

  version         INT      NOT NULL DEFAULT 0 COMMENT 'Optimistic lock',
  created_by      VARCHAR(64) NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by      VARCHAR(64) NULL,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  UNIQUE KEY uk_business (business_type, business_id),
  KEY idx_status_time (status, created_at),
  KEY idx_def_id (def_id),
  KEY idx_current_stage (current_stage_id),

  CONSTRAINT fk_instance_def
    FOREIGN KEY (def_id) REFERENCES wf_definition(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow instance (one request)';
```

```sql
CREATE TABLE wf_stage_instance (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_id   BIGINT       NOT NULL,
  stage_no      INT          NOT NULL,
  stage_code    VARCHAR(64)  NOT NULL,
  stage_name    VARCHAR(128) NOT NULL,
  policy        VARCHAR(16)  NOT NULL DEFAULT 'ALL' COMMENT 'ALL/ANY',
  status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING/ACTIVE/APPROVED/REJECTED/RECALLED/CANCELLED/SKIPPED',
  started_at    DATETIME NULL,
  ended_at      DATETIME NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  UNIQUE KEY uk_instance_stage (instance_id, stage_no),
  KEY idx_instance_status (instance_id, status),

  CONSTRAINT fk_stage_instance_instance
    FOREIGN KEY (instance_id) REFERENCES wf_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Runtime stage instance';
```

```sql
CREATE TABLE wf_task (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_id    BIGINT       NOT NULL,
  stage_id       BIGINT       NOT NULL,

  assignee_type  VARCHAR(16)  NOT NULL COMMENT 'USER/GROUP/ROLE',
  assignee_id    VARCHAR(64)  NOT NULL,
  assignee_name  VARCHAR(128) NULL,

  status         VARCHAR(32)  NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING/CLAIMED/APPROVED/REJECTED/CANCELLED/SKIPPED',
  decision       VARCHAR(16)  NULL COMMENT 'APPROVE/REJECT',
  comment        VARCHAR(512) NULL,

  claimed_at     DATETIME NULL,
  acted_at       DATETIME NULL,

  cancel_reason  VARCHAR(64) NULL COMMENT 'RECALLED/DROPPED/STAGE_MOVED',
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  KEY idx_assignee_status (assignee_id, status),
  KEY idx_stage_status (stage_id, status),
  KEY idx_instance_status (instance_id, status),

  CONSTRAINT fk_task_instance
    FOREIGN KEY (instance_id) REFERENCES wf_instance(id) ON DELETE CASCADE,
  CONSTRAINT fk_task_stage
    FOREIGN KEY (stage_id) REFERENCES wf_stage_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Runtime task (per approver)';
```

```sql
CREATE TABLE wf_event (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_id   BIGINT       NOT NULL,
  stage_id      BIGINT       NULL,
  task_id       BIGINT       NULL,

  event_type    VARCHAR(32)  NOT NULL
    COMMENT 'SUBMIT/APPROVE/REJECT/COMMENT/RECALL/DROP/DROP_REQUESTED/DROP_APPROVED/DROP_REJECTED/SYSTEM',
  actor_type    VARCHAR(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER/SYSTEM/INTEGRATION',
  actor_id      VARCHAR(64)  NULL,
  actor_name    VARCHAR(128) NOT NULL,

  message       VARCHAR(512) NOT NULL,
  payload_json  JSON         NULL,

  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  KEY idx_instance_time (instance_id, created_at),
  KEY idx_stage_id (stage_id),
  KEY idx_task_id (task_id),

  CONSTRAINT fk_event_instance
    FOREIGN KEY (instance_id) REFERENCES wf_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow event timeline/audit';
```

---

## 3) Post-action request (Drop after completion)

```sql
CREATE TABLE wf_post_action_request (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  instance_id    BIGINT      NOT NULL,

  action_type    VARCHAR(32) NOT NULL COMMENT 'DROP (extendable)',
  status         VARCHAR(32) NOT NULL DEFAULT 'PENDING'
    COMMENT 'PENDING/APPROVED/REJECTED/CANCELLED',

  request_reason VARCHAR(512) NULL,
  requested_by   VARCHAR(64)  NOT NULL,
  requested_name VARCHAR(128) NOT NULL,
  requested_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

  decided_by     VARCHAR(64)  NULL,
  decided_name   VARCHAR(128) NULL,
  decided_at     DATETIME     NULL,
  decision_note  VARCHAR(512) NULL,

  KEY idx_instance_action (instance_id, action_type),
  KEY idx_status (status),

  CONSTRAINT fk_post_action_instance
    FOREIGN KEY (instance_id) REFERENCES wf_instance(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Post-action requests (e.g., drop after completion)';
```

---

# Recommended queries (for your “Completed list” rule)

### Completed list (still includes “drop requested pending”)

```sql
SELECT i.*
FROM wf_instance i
WHERE i.status = 'COMPLETED'
ORDER BY i.ended_at DESC;
```

### Add badge info (void_state)

* Show:

    * `void_state=DROP_PENDING` → “Drop Requested”
    * `void_state=DROPPED` → “Voided/Dropped”
    * `void_state=DROP_REJECTED` → “Drop Rejected”

---

