It depends on whether “approval” and “edit” are **two independent state machines** or just **one business lifecycle**.

## Use **one `status`** when

* There is **one clear lifecycle** and only one state can be true at a time.
* Example: `DRAFT → SUBMITTED → APPROVED → REJECTED → ARCHIVED`
* “editability” is just a **rule derived from status** (e.g., editable only in `DRAFT` and `REJECTED`).

✅ Pros: simple, fewer invalid combinations, easier queries, fewer bugs.

---

## Use **multiple status fields** when

* You have **independent dimensions** that can vary separately.
* Example:

    * `approval_status`: `DRAFT/SUBMITTED/APPROVED/REJECTED`
    * `edit_status`: `LOCKED/UNLOCKED` or `EDITING/READONLY`
* And you *really* need combinations like:

    * Approved **but** temporarily editable (hotfix window)
    * Pending approval **but** locked due to compliance
    * Rejected **and** locked after timeout

✅ Pros: expresses reality when dimensions are independent.
⚠️ Cons: creates invalid combinations unless you enforce rules.

If you choose multi-status, add **constraints/rules** (app validation + DB check constraint if possible) to prevent
nonsense states.

---

## Practical recommendation (most common in enterprise)

1. Keep **one main lifecycle status**: `approval_status` (or `status`).
2. Don’t add `edit_status` unless you truly need it.
3. If “editability” is needed, prefer:

    * a derived boolean: `is_editable = status in (...)` (computed in code), or
    * `locked_at`, `locked_by`, `lock_reason` for operational locking (more meaningful than `edit_status`).

---

## A good compromise pattern

```text
id
approval_status      (business lifecycle)
lock_status          (operational control: LOCKED/UNLOCKED)
lock_reason
lock_by
lock_time
```

This avoids mixing “business workflow” with “system control”.

---

If you tell me your actual workflow states (e.g., Draft/Submit/Approve/Reject) and when editing is allowed, I’ll
recommend the cleanest schema (and rules to prevent invalid combinations).
