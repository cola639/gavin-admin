Project already has a **DB-driven
Quartz scheduling framework**: jobs are stored in `sys_job`, executed via **reflection** (`invokeTarget`), and execution
logs are persisted to `sys_job_log`.

---

## Quartz Plan (PM → AI Developer)

### 1) Goal

Implement a new scheduled task that:

* Runs on a **cron** schedule
* Can be **created/updated/paused/resumed/run-once** via existing APIs
* Uses the existing `invokeTarget` reflection mechanism (no hardcoded Quartz job classes per feature)

---

## 2) Existing Mechanism (Baseline You Must Follow)

### Job creation / management APIs

Jobs are managed through `/monitor/job` endpoints (list/get/add/edit/changeStatus/run/remove).

### Validation rules (very important)

When creating/updating a job, the system validates:

* Cron must be valid (`CronUtils.isValid`)
* `invokeTarget` must **NOT** contain RMI/LDAP/HTTP(S) patterns and must pass a **whitelist** check

### How the job executes

* Quartz runs either a **concurrent** or **non-concurrent** wrapper job depending on `job.concurrent`:

    * `"0"` → `QuartzJobExecution` (concurrent allowed)
    * otherwise → `QuartzDisallowConcurrentExecution` (annotated with `@DisallowConcurrentExecution`)
* Both wrappers call `JobInvokeUtil.invokeMethod(sysJob)`

### `invokeTarget` format + parameter rules

`JobInvokeUtil` parses `invokeTarget` like:

* `beanName.methodName(...)` or `full.class.Name.methodName(...)`
* parameters supported:

    * String: must be quoted `'text'` or `"text"`
    * Boolean: `true/false`
    * Long: ends with `L` (e.g., `10L`)
    * Double: ends with `D` (e.g., `3.14D`)
    * otherwise Integer

There is an existing example bean `@Component("ryTask")` showing supported method signatures.

### Scheduling behavior

* Creating a job schedules it via `ScheduleUtils.createScheduleJob(...)` using the cron expression; if job status is
  PAUSE, it pauses immediately.
* New jobs are inserted with status **PAUSE by default**, then scheduled (but paused). You must resume it to start
  running.

### Logging

Every execution is recorded into `sys_job_log` with duration + status; exceptions are captured and truncated.

### Startup behavior

On application startup, scheduler is cleared and all DB jobs are re-created from `sys_job`.

---

## 3) Implementation Instructions (How to Add a New Scheduled Task)

### Step A — Create a new Task Bean (preferred approach)

1. Create a new class under:

* `api-quartz/src/main/java/com/api/quartz/task/...`

2. Make it a Spring bean with a stable bean name:

* `@Component("yourTaskName")`

3. Use:

* `@Slf4j`
* `@RequiredArgsConstructor`
* **No `@Autowired`**

4. Keep task method(s):

* `public void methodName(...)`
* Only use supported param types (String/Boolean/Long/Double/Integer) to match `JobInvokeUtil` parsing rules

**Example template (adjust names + injected services):**

```java
package com.api.quartz.task.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("orderTask")
@RequiredArgsConstructor
public class OrderTask {

    // private final OrderService orderService;

    public void syncPendingOrders(String region) {
        log.info("Order sync started. region={}", region);
        // orderService.syncPending(region);
        log.info("Order sync finished. region={}", region);
    }
}
```

**PM quality requirements for the AI developer**

* Task must be **idempotent** (safe to retry).
* If it updates DB state, do it in a **service layer** with clear transactions.
* Add meaningful logs (English only), no sensitive data.

---

### Step B — Define the job configuration (SysJob)

When creating a job, set:

* `jobName`: human-readable
* `jobGroup`: categorize (e.g., `DEFAULT`, `ORDER`, `REPORT`)
* `invokeTarget`: must match the reflection format
* `cronExpression`: Quartz cron
* `misfirePolicy`: one of `0/1/2/3` (see constants)
* `concurrent`: `"0"` allow concurrent, `"1"` disallow concurrent
* `status`: system will insert as PAUSE; you’ll resume via API after creation

**invokeTarget examples**

* No params: `orderTask.syncAll()`
* With String: `orderTask.syncPendingOrders('CN')`
* Multiple params: `ryTask.ryMultipleParams('a', true, 10L, 1.2D, 3)`

---

### Step C — Register the job via API (recommended operational flow)

1. **Create job**

* `POST /monitor/job` with SysJob JSON

2. **Enable job**

* `PUT /monitor/job/changeStatus` to set status to `"0"` (NORMAL)

3. (Optional) **Run once now**

* `PUT /monitor/job/run`

**Important:** if validation fails, it’s often because `invokeTarget` is not whitelisted or contains forbidden
patterns (RMI/LDAP/HTTP).

---

## 4) Testing & Acceptance Criteria

### Functional checks

* ✅ Job can be created via `/monitor/job` (no validation errors).
* ✅ Job can be resumed and triggers on schedule (or via “run once”).
* ✅ Each run creates a `sys_job_log` record with status and duration.

### Observability checks

* Verify job logs using `/monitor/jobLog/list`
* Failures should show status FAIL and exception text (truncated).

---

