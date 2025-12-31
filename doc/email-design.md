# Async Email Service (AWS SES SDK) — Enterprise Design Plan (Markdown)

## 0. Scope and Decisions

### Chosen options

* **Async**: API returns **Accepted + taskId**, sending happens in background.
* **Provider**: **AWS SDK (SES API)** (not SMTP).

### Key goals

1. Support **batch notification** (N recipients per request).
2. Use **Amazon SES SDK** as the sending adapter.
3. Support **custom email style** using **template files** (HTML/text).
4. Provide a **test controller** to validate email features and behaviors.

### Non-goals (Phase-1)

* Full marketing system (unsubscribe, click tracking, campaigns).
* Multi-channel (SMS/push) — we design extension points.

---

## 1. Current Project Structure Fit

Your repo layout:

```
gavin-admin
 - api-boot
 - api-common
 - api-framework
 - api-quartz
 - api-system
```

This maps cleanly to a modular-monolith enterprise design:

* **api-boot**: REST controllers (public APIs + test endpoints).
* **api-common**: shared primitives (AjaxResult, enums, constants, base exceptions).
* **api-framework**: infra adapters (AWS SES client, template renderer, configs).
* **api-system**: application services + domain entities + repositories (workflow + persistence).
* **api-quartz**: scheduled jobs for async dispatch + retry + cleanup.

---

## 2. Architecture Overview

### 2.1 High-level components

**(A) Application layer (api-system)**

* `EmailAppService`: validates requests, creates tasks, controls workflow state, reads configs, writes logs.
* `EmailPolicyService`: batch size limits, allowed domains, per-user rate, etc.
* `EmailPermissionService`: who can send what (optional in Phase-1).
* Repositories for tasks/logs.

**(B) Infrastructure layer (api-framework)**

* `EmailClient` (interface): `send(EmailSendCommand)` → provider-independent.
* `SesEmailClient` (implementation): uses AWS SES SDK to send.
* `EmailTemplateRenderer`: renders HTML/text from template files + variables.
* `EmailProperties`: unified configuration.

**(C) Async layer (api-quartz)**

* `EmailDispatchJob`: pulls PENDING tasks (or items) and sends in batches.
* `EmailRetryJob`: retries FAILED items based on backoff schedule.
* `EmailCleanupJob`: archives/cleans old logs and expired tasks.

---

## 3. Domain Model and Persistence Design

Because you want enterprise-grade async + batch + retry, design around **Task Header + Task Items**.

### 3.1 Tables (suggested)

#### `sys_email_task` (header)

Represents one API submission (batch or single).

| Field                   | Type         | Notes                                         |
|-------------------------|--------------|-----------------------------------------------|
| task_id                 | bigint PK    | returned to client                            |
| biz_type                | varchar(64)  | trace business scenario                       |
| biz_id                  | varchar(64)  | business reference                            |
| template_name           | varchar(128) | optional                                      |
| subject                 | varchar(255) | resolved subject                              |
| content_type            | varchar(16)  | HTML / TEXT                                   |
| sender                  | varchar(255) | from address                                  |
| status                  | varchar(16)  | PENDING/SENDING/PARTIAL/SENT/FAILED/CANCELLED |
| total_count             | int          | number of recipients                          |
| success_count           | int          | updated by worker                             |
| fail_count              | int          | updated by worker                             |
| idempotency_key         | varchar(128) | dedup key (optional but recommended)          |
| created_by              | bigint       | userId                                        |
| created_at / updated_at | datetime     | auditing                                      |

#### `sys_email_task_item` (line items)

One row per recipient (supports per-recipient status/retry).

| Field                   | Type          | Notes                                    |
|-------------------------|---------------|------------------------------------------|
| item_id                 | bigint PK     |                                          |
| task_id                 | bigint        | FK to header                             |
| to_email                | varchar(320)  | recipient                                |
| cc_emails               | text          | optional                                 |
| bcc_emails              | text          | optional                                 |
| variables_json          | text          | per-recipient template vars (optional)   |
| rendered_snapshot       | longtext      | optional, for audit/debug (configurable) |
| provider_message_id     | varchar(128)  | returned by SES if available             |
| status                  | varchar(16)   | PENDING/SENDING/SENT/FAILED              |
| retry_count             | int           |                                          |
| next_retry_at           | datetime      | backoff schedule                         |
| last_error              | varchar(1000) | last failure reason                      |
| created_at / updated_at | datetime      |                                          |

#### `sys_email_send_log` (optional, if you want separate log)

You can log in `task_item` directly. If you want more detail (attempt history), add a log table.

| Field        | Type         | Notes          |
|--------------|--------------|----------------|
| log_id       | bigint PK    |                |
| item_id      | bigint       |                |
| attempt_no   | int          | 1..N           |
| provider     | varchar(32)  | SES            |
| request_id   | varchar(128) | AWS request id |
| result       | varchar(16)  | SUCCESS/FAIL   |
| error_detail | text         |                |
| created_at   | datetime     |                |

### 3.2 Status definitions

**Item status (most important)**

* `PENDING` → ready to send
* `SENDING` → picked by worker
* `SENT`
* `FAILED` → waiting retry or terminal failure

**Header status (derived)**

* `PENDING` (all items pending)
* `SENDING` (some items in sending)
* `PARTIAL` (some sent, some failed/pending)
* `SENT` (all sent)
* `FAILED` (all failed, or terminal)
* `CANCELLED`

---

## 4. Workflow Design

### 4.1 Submit (API → create tasks)

1. Validate request (batch limit, email format, template existence).
2. Create `sys_email_task` header (status=PENDING).
3. Create N `sys_email_task_item` rows (status=PENDING).
4. Return response:

    * `code=200`
    * `msg="Accepted"`
    * `data={ taskId, totalCount }`

**Idempotency (recommended)**

* If client provides `idempotencyKey`, enforce uniqueness per `(bizType, bizId, idempotencyKey)` to avoid duplicates.

### 4.2 Dispatch worker (Quartz job)

1. Pick items: `status=PENDING` and (`next_retry_at` is null or <= now)
2. Mark as `SENDING` (optimistic locking or “update where status=PENDING limit N”)
3. Render template for each item (using renderer)
4. Call `EmailClient.send(...)`
5. Update each item:

    * success → `SENT`, store `provider_message_id`
    * failure → `FAILED`, set `retry_count+1`, `next_retry_at`, `last_error`
6. Update header counts + status

### 4.3 Retry policy

* Only retry errors that are **transient**:

    * throttling, timeouts, network issues
* Do not retry permanent failures:

    * invalid address, template missing, policy denied

Suggested backoff (configurable):

* attempt 1: +1 min
* attempt 2: +5 min
* attempt 3: +30 min
* attempt 4: +2 hours
* maxRetries: 4–6 depending on requirements

### 4.4 Cleanup

* Archive logs older than X days (config)
* Mark tasks as terminal after too long
* Optional: remove rendered snapshots if storage cost matters

---

## 5. Template and Styling Design

### 5.1 Template file structure

In `api-framework`:

```
api-framework/src/main/resources/templates/email/
  layout/
    base.html
  notice/
    notice.html
  auth/
    register.html
    reset_password.html
  text/
    reset_password.txt
```

### 5.2 Template engine

Use one engine consistently:

* **FreeMarker** (recommended for email) or Thymeleaf.

### 5.3 Styling strategy (email-client safe)

* Prefer **inline-friendly CSS** and simple HTML structure.
* Use a base layout `base.html` and inject body content via include/macro.
* Variables are provided as a `Map<String, Object>`.

### 5.4 Rendering mode

Support:

* HTML template → HTML output
* Text template → TEXT output (fallback)

Config option:

* `email.render.snapshot.enabled` → whether to store rendered content in DB for debugging/audit.

---

## 6. AWS SES SDK Integration Plan

### 6.1 SES adapter placement

In `api-framework`:

* `EmailClient` (interface)
* `SesEmailClient` (implementation)

### 6.2 AWS credentials

Enterprise recommendation:

* Use AWS default provider chain (EC2/ECS role, env vars, profiles) **in production**
* Allow static accessKey/secretKey for local dev

### 6.3 SES API mode

* Use SES v2 (`SesV2Client`) for modern features.
* For attachments later, plan for `RawEmail` support (more complex).

### 6.4 Send options (future-ready)

* Configuration Set
* Tags (bizType/bizId/taskId)
* Reply-To
* Optional: bounce/complaint handling (SNS) later

---

## 7. API Design (Controllers in api-boot)

### 7.1 Production APIs (recommended)

Base path suggestion: `/notify/email`

1. **Submit single** (async)

* `POST /notify/email/submit`
* Returns: `{ taskId }`

2. **Submit batch** (async)

* `POST /notify/email/batch-submit`
* Returns: `{ taskId, totalCount }`

3. **Query task**

* `GET /notify/email/task/{taskId}`
* Returns: header + aggregated counts

4. **Query items** (optional for admin)

* `GET /notify/email/task/{taskId}/items?page=&size=`

### 7.2 Test Controller (your requirement)

Under `/test/email` (or `/email/test`), guarded by:

* `@Profile("dev")` OR `@PreAuthorize("hasRole('ADMIN')")`

Endpoints:

* `POST /test/email/send-simple` (no template)
* `POST /test/email/send-template`
* `POST /test/email/send-batch-template`
* `GET /test/email/task/{taskId}` (check behavior)
* `GET /test/email/recent?limit=50`

### 7.3 Response style

All JSON APIs return `AjaxResult`:

* success: `code=200`, `msg`, `data`
* accepted: you can still use `code=200` with `msg="Accepted"` (consistent with your current style)

---

## 8. Async Execution Strategy (Quartz + DB polling)

### 8.1 Why DB polling is enterprise-stable

* No message broker required (simpler ops)
* Works well for moderate volume
* Supports replay, auditing, and manual intervention

### 8.2 Job cadence (configurable)

* Dispatch job every 10–30 seconds (or 1 minute)
* Retry job every 1–5 minutes
* Cleanup daily

### 8.3 Concurrency control

To prevent double sending:

* Use atomic DB state transitions:

    * `UPDATE sys_email_task_item SET status='SENDING' WHERE status='PENDING' AND ... LIMIT N`
* Consider optimistic locking with `version` field (optional).
* Ensure worker instances can run in parallel safely.

---

## 9. Config Design (Properties)

In `api-framework` add `EmailProperties`:

Suggested YAML:

```yaml
app:
  email:
    provider: ses
    from: "no-reply@yourdomain.com"
    maxBatchSize: 100
    maxRetries: 4
    templateRoot: "templates/email"
    snapshotEnabled: false

    ses:
      region: "us-east-1"
      accessKey: ""
      secretKey: ""
      configurationSet: ""
```

And Quartz:

```yaml
app:
  quartz:
    emailDispatchCron: "*/15 * * * * ?"
    emailRetryCron: "0 */1 * * * ?"
    emailCleanupCron: "0 30 3 * * ?"
```

---

## 10. Observability and Operations

### Logging (follow your style)

* Use `@Slf4j`
* Log key identifiers:

    * `taskId`, `itemId`, `bizType`, `bizId`, `toEmail`, `providerMessageId`
* Log at:

    * INFO: accepted, dispatched, sent, retry scheduled
    * WARN: transient failure, size/template warnings
    * ERROR: unexpected exceptions

### Metrics (optional, recommended)

* sent_count, failed_count, retry_count
* average render time, send latency

### Admin operations (future)

* Manual resend failed items
* Cancel a task
* Export logs

---

## 11. Testing Strategy

### Unit tests

* `EmailTemplateRenderer` renders expected HTML with variables.
* `EmailAppService`:

    * creates header/items correctly
    * status transitions correct
    * retry scheduling correct

### Integration tests

* Use a mocked `EmailClient` in test profile
* Optional: LocalStack for SES (if stable in your environment)

### Test controller validation

* Expose test endpoints only in `dev` profile or admin role.

---

## 12. Delivery Plan (Phases)

### Phase-1 (MVP, enterprise-correct)

* Async submit + DB persistence
* Template file rendering
* SES send adapter
* Quartz dispatch + retry
* Test controller + query task

### Phase-2

* Attachments (integrate with your MinIO module)
* SNS bounce/complaint handling → update email status
* Rate limiting per user/bizType
* Admin resend/cancel endpoints

---

## 13. Recommended Naming and Package Layout

### api-framework

* `com.api.framework.email.config`
* `com.api.framework.email.client`
* `com.api.framework.email.template`

### api-system

* `com.api.system.service.notify` (EmailAppService)
* `com.api.system.domain.email` (entities/enums)
* `com.api.system.repository.email`

### api-quartz

* `com.api.quartz.job.email`

### api-boot

* `com.api.boot.controller.notify.EmailController`
* `com.api.boot.controller.test.EmailTestController`

---
