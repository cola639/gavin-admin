Below is a **product-style design plan** (no code) for the 3 features:

* **1) Timeline view for baselines**
* **2) Baseline list view by status**

    * **Published baselines must still show as Published even if RETIRE/DELETE is pending**
* **3) Approval workflow**
* **No email for MVP** → replace with **event log + application log**

---

## 1) Scope and UX Goals

### MVP Goals

1. Users can **create + submit** a baseline request and see its **current status**.
2. Cyber SME can **approve / reject**.
3. If approved → baseline becomes **Published**.
4. Owner can request **Retire/Delete**, but while it’s pending the baseline stays in **Published list** and is visibly
   marked.

### Roles

* **Platform Owner**

    * Create draft, submit, view status, request retire/delete
* **Cyber SME**

    * Review tasks, approve/reject, decide retire/delete requests (same role for MVP)

---

## 2) Data Model Usage (Your Tables)

### baseline_request (current state)

Use it as the **single source of truth** for:

* User-visible status: `status` (DRAFT/PUBLISHED/RETIRED)
* Approval state: `approval_status` (PENDING/APPROVED/REJECTED/NOT_REQUIRED)
* Step: `current_step` (OWNER_SUBMIT/SME_REVIEW/END)
* Pending retire/delete marker: `pending_action_type` (RETIRE/DELETE/NULL)

**Key rule for your requirement**

* **Published list is driven only by `status='PUBLISHED'`**
* `pending_action_type` only affects a **badge/indicator**, not the list membership.

### baseline_task (work queue)

Represents “who needs to act”.

* For initial publish approval: `step_code='SME_REVIEW'`
* For retire/delete decision (MVP): create another task with `step_code='POST_ACTION_REVIEW'` (or reuse SME_REVIEW if
  you want fewer types, but it’s cleaner to separate)

### baseline_event (timeline + audit log)

Everything important writes an event:

* `SUBMIT`, `REVIEW_REQUESTED`, `APPROVE`, `REJECT`, `PUBLISH`
* plus **post-action events** (even if you keep the `event_type` column simple):

    * `POST_ACTION_REQUEST` (message includes RETIRE/DELETE + reason)
    * `POST_ACTION_APPROVE` / `POST_ACTION_REJECT`
    * `POST_ACTION_CANCEL` (optional)

### Notifications for MVP

* No outbox/email.
* Instead:

    * Write `baseline_event` records for traceability
    * Log in service layer (application logs) with `request_no`, `request_id`, `actor_id`, `action`

---

## 3) State & Workflow Rules (simple and predictable)

### Lifecycle status (`baseline_request.status`)

* `DRAFT` → created/being edited
* `PUBLISHED` → visible to end users
* `RETIRED` → no longer active

### Approval status (`baseline_request.approval_status`)

* `PENDING` → waiting SME decision
* `APPROVED` / `REJECTED` → decision outcome

### Pending action rule

* If owner requests retire/delete:

    * set `pending_action_type = RETIRE|DELETE`
    * **do not change** `status` immediately
    * create a `POST_ACTION_REVIEW` task for SME
* If SME approves:

    * RETIRE → set `status='RETIRED'`, `retired_at=now`, clear `pending_action_type`
    * DELETE → for MVP you can treat delete as RETIRE (recommended) to keep history; or allow hard delete later
* If SME rejects:

    * clear `pending_action_type` (baseline remains PUBLISHED)

---

## 4) API Design (REST, minimal set)

### 4.1 Baseline list + filters

**GET `/baselines`**

* Query params:

    * `status` (DRAFT/PUBLISHED/RETIRED)
    * `approvalStatus` (optional)
    * `ownerId` (optional)
    * `reviewerId` (optional)
    * `pendingActionType` (optional: RETIRE/DELETE) — *for admin/reviewer filtering*
    * paging: `page`, `size`, `sort`
* Response includes:

    * `id`, `requestNo`, `title`
    * `status`, `approvalStatus`, `currentStep`
    * `pendingActionType` (so UI can show badge)
    * timestamps: `createdAt`, `updatedAt`, `publishedAt`

**Important behavior**

* Published tab uses: `status=PUBLISHED` (ignores pendingActionType for membership)

---

### 4.2 Baseline detail (current status + key fields)

**GET `/baselines/{id}`**

* Returns full baseline_request fields needed for detail page
* Includes:

    * current state + pending action marker
    * reviewer info
    * timestamps

---

### 4.3 Timeline feed

**GET `/baselines/{id}/timeline`**

* Returns ordered list from `baseline_event`:

    * `eventType`, `message`, `actorRole`, `actorName`, `createdAt`
    * optionally: link `taskId` if present

This supports the Timeline UI directly.

---

### 4.4 Create draft + submit

**POST `/baselines`**

* Creates draft baseline (status DRAFT, approval PENDING or NOT_REQUIRED depending on product rule)

**POST `/baselines/{id}/submit`**

* Sets `current_step='SME_REVIEW'`, `approval_status='PENDING'`, `submitted_at=now`
* Creates `baseline_task` for SME
* Adds `baseline_event` for submit + review requested

---

### 4.5 Approver “My tasks” inbox

**GET `/tasks/my`**

* Query params: `status=PENDING` (default), `stepCode` (optional)
* Response:

    * task info + baseline summary (requestNo/title/status)

---

### 4.6 Approve / Reject

**POST `/tasks/{taskId}/decision`**

* Body: `decision=APPROVE|REJECT`, `comment` (optional)
* Effects:

    * Update task (`status`, `decision`, `acted_at`)
    * Update baseline_request:

        * Approve → `approval_status=APPROVED`, `status=PUBLISHED`, `current_step=END`, `published_at`
        * Reject → `approval_status=REJECTED`, `current_step=END` (status stays DRAFT unless you define another
          lifecycle)
    * Write baseline_event for audit

---

### 4.7 Retire/Delete request + decision (MVP)

**POST `/baselines/{id}/post-action/request`**

* Body: `actionType=RETIRE|DELETE`, `reason`
* Effects:

    * Set `pending_action_type`
    * Create approver task `POST_ACTION_REVIEW`
    * Add event `POST_ACTION_REQUEST`

**POST `/tasks/{taskId}/decision`** (same endpoint as above)

* If step is POST_ACTION_REVIEW:

    * Approve RETIRE → set `status=RETIRED`, clear pending action
    * Reject → clear pending action
    * Add event `POST_ACTION_APPROVE/REJECT`

---

## 5) UI Design (React)

### 5.1 Baseline list (table view)

**Page: “Baselines”**

* Tabs:

    * **Published**
    * Draft
    * Retired
    * My Approvals (optional shortcut)

**Table columns**

* Request No, Title
* Status (big label)
* Approval Status (small badge)
* Pending Action badge (if `pendingActionType != null` → “Retire Pending” / “Delete Pending”)
* Owner, Reviewer
* Updated time

**Behavior**

* Published tab shows `status=PUBLISHED` only
* If pending action exists → still in published tab, with badge

---

### 5.2 Baseline detail page

**Top “Status Card”**

* Display:

    * Status + Approval status + Current step
    * Pending action badge if exists
    * Key timestamps

**Actions (role-based)**

* Owner:

    * Submit (if draft)
    * Request retire/delete (if published and no pending action)
* SME:

    * Approve/Reject (if has pending task)
    * Approve/Reject retire/delete request (if post-action task)

---

### 5.3 Timeline view

**Component: Timeline**

* Shows `baseline_event` stream (newest first or oldest first)
* Each item:

    * Icon by event type
    * message + actor + time
* Optional filter:

    * “Workflow only” / “Post-action only”

---

### 5.4 Approval workflow view (simple stepper)

**Component: Workflow Stepper**

* Steps:

    1. Owner submit
    2. SME review
    3. Published
* Each step shows:

    * completion status
    * assigned SME (from request/task)
    * decision/comment when done

---

## 6) Logging Strategy (replace notifications)

For every state transition:

* Write a `baseline_event`
* Write an application log line (structured):

    * requestId, requestNo, action, actorId, result, timestamps

This gives you:

* UI timeline (from DB)
* Operational trace (from logs)

---

## 7) Acceptance Criteria (what “done” means)

1. Published list shows all `status=PUBLISHED` baselines, **even if `pending_action_type` is set**.
2. Baseline detail page always shows:

    * status + approval status + current step + pending badge (if any)
3. Timeline contains:

    * submit, review requested, approve/reject, publish, post-action request/decision
4. Approver can complete tasks from “My tasks”.

---
