Below is a **practical, production-ready design plan** for an **“Import from Excel → generate derived values → persist
to DB”** feature in a **Java 17 + Spring Boot + JPA** system. (No code yet.)

---

## 1) Goals and constraints

### Functional goals

* User uploads an Excel (`.xlsx`) file.
* System parses rows, validates data, **generates derived fields** (codes, normalized values, relationships, defaults,
  computed amounts, etc.).
* Persist valid records into database.
* Provide **import result**: success count, failure count, downloadable error report.

### Non-functional goals

* Handle large files without OOM (streaming read).
* Idempotent / repeatable imports (avoid duplicates).
* Clear error visibility per row.
* Safe in production: transactional behavior, partial success strategy, audit trail.

---

## 2) User workflow (recommended UX)

1. **Upload**

    * User uploads Excel from UI.
    * Backend stores it temporarily (local disk, NAS, or object storage like S3/MinIO).

2. **Pre-check / Preview (optional but very valuable)**

    * Parse first N rows (e.g., 50) to validate template and show preview.
    * Fail fast if headers/format mismatch.

3. **Start import job**

    * Create an import job record and process async.
    * UI polls job status or uses WebSocket/SSE for progress.

4. **Results**

    * Show summary: total rows / inserted / updated / skipped / failed.
    * Provide error file download (CSV/Excel) listing row number + error reasons.

---

## 3) API design (minimal set)

* `POST /api/imports`
  Upload file → returns `importJobId`

* `GET /api/imports/{jobId}`
  Returns status + progress + summary

* `GET /api/imports/{jobId}/errors`
  Download error report (CSV/Excel)

(Optional)

* `POST /api/imports/{jobId}/confirm` if you want preview-first then confirm.

---

## 4) Domain design (key components)

### 4.1 Import Job tracking tables

**import_job**

* `id`
* `type` (which template/business import this is)
* `status` (UPLOADED, VALIDATING, PROCESSING, COMPLETED, FAILED, PARTIAL)
* `file_path` / `object_key`
* `total_rows`, `success_rows`, `failed_rows`, `updated_rows`, `skipped_rows`
* `started_at`, `finished_at`
* `created_by`
* `checksum` (for idempotency)
* `error_report_path` (optional)

**import_job_error**

* `id`, `job_id`
* `row_number`
* `raw_row_json` (optional for debugging)
* `error_message` (human-readable)
* `error_code` (optional, structured)

This makes the import **auditable and user-friendly**.

---

## 5) Processing pipeline (recommended architecture)

### Step A: File acceptance

* Validate:

    * file extension/type (`.xlsx`)
    * size limit (e.g., 20MB or configurable)
* Save file and create `import_job`.

### Step B: Template detection and header validation

* Read header row, compare with expected columns.
* Support template versions:

    * e.g., “v1: columns A,B,C” vs “v2: columns A,B,C,D”
* If mismatch: mark job FAILED and store errors.

### Step C: Streaming row parsing

Use a streaming approach so large Excel files don’t load fully:

* Prefer **EasyExcel** (streaming & fast) or **Apache POI SAX** (streaming).
* Convert each row into an internal `RowDTO`.

### Step D: Validation layer

Validate each row:

* Required fields, data types, ranges, date formats
* Cross-field rules (“endDate must be after startDate”)
* DB existence checks (“customerId must exist”)
* Duplicate checks (within-file and against DB)

**Output**

* valid `RowDTO`s go to generation step
* invalid rows get stored into `import_job_error`

### Step E: Derived value generation (your “generate value” requirement)

A dedicated “Generator” component that:

* Normalizes: trim, lowercase, format phone/email, map enums
* Computes: totals, taxes, discount, status, timestamps
* Creates identifiers: business code, slug, unique key
* Resolves relationships: look up foreign keys, map to reference data
* Applies defaults: missing optional fields, fallback values

**Important design rule**: generation should be **pure and deterministic** when possible
(so re-importing produces the same result unless DB state changes).

### Step F: Persistence strategy (batch + controlled transactions)

For performance and reliability:

* Process in **chunks** (e.g., 200–1000 rows per chunk)
* Each chunk:

    * Start transaction
    * Insert/update entities
    * Commit
* If chunk fails, you can:

    * mark those rows failed and continue (partial success), OR
    * fail the whole job (strict mode)

**Upsert rules**

* Decide unique business key (e.g., `tenantId + code`, or `email`, or `externalId`)
* If exists → update allowed fields
* If not exists → insert

---

## 6) Idempotency and duplicate control (very important)

Recommended:

* Compute file checksum (MD5/SHA-256) + import type + user/tenant.
* If same checksum imported before:

    * either block (“already imported”), or
    * allow but mark as re-run.
* Row-level idempotency:

    * unique constraints in DB on business key
    * upsert policy in service

---

## 7) Error handling and user feedback

### Row-level errors

* Keep processing other rows.
* Store errors with:

    * row number
    * column name (if possible)
    * message in English (consistent)

### Job-level errors

* File unreadable / wrong template / DB down → fail job quickly.

### Error report output

Generate a downloadable report:

* columns: `rowNumber`, `errorMessage`, plus original columns
* makes it easy for user to fix and re-upload.

---

## 8) Performance and scalability decisions

* Use async job execution:

    * Simple: Spring `@Async` + thread pool
    * Better: message queue (Kafka/RabbitMQ) if import volume grows
* Limit concurrent imports per tenant/user to protect DB.
* Use batch inserts/updates; avoid per-row DB calls:

    * prefetch reference data into maps (e.g., lookup tables)
    * for existence checks, query in bulk per chunk

---

## 9) Security and compliance

* Access control: only authorized roles can import.
* Virus scan if files come from untrusted users (optional but recommended).
* Don’t log raw sensitive values; log row number + error codes.
* Store files with retention policy (e.g., auto-delete after 7 days).

---

## 10) Observability (what to log/measure)

**Logs**

* jobId, importType, userId, totalRows
* per chunk: success/fail counts, elapsed time
* critical failures with stack trace

**Metrics**

* import duration (p50/p95)
* rows/sec throughput
* failure rate
* DB time spent / batch sizes

---

## 11) Testing plan

* Unit tests:

    * header validation
    * row validation rules
    * generator determinism
* Integration tests:

    * import a small Excel and verify DB state
    * duplicate import behavior
    * partial failure scenario
* Load test:

    * 50k–200k rows streaming import, observe memory & time

---

## 12) Recommended module/class responsibilities (clean separation)

* `ImportController`: upload + status endpoints
* `ImportJobService`: create/update job state
* `ExcelReader`: streaming row parsing
* `RowValidator`: business validation rules
* `ValueGenerator`: derived fields generation
* `PersistenceWriter`: chunk-based DB writes (upsert logic)
* `ErrorReportService`: build downloadable error file

This separation keeps the import maintainable as requirements evolve.

---
