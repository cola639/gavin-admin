## MinIO File Center High-level Module Plan

### Goals

* Upload different file categories to different MinIO “directories” (prefixes): avatar / excel / doc / file / attachment
* YAML-driven configuration (prefix, size, allowed types, visibility, default expiry)
* Single + multiple upload
* Time-sensitive access (presigned URL) for private files
* Delete single + batch

---

## 1) Package Structure (Package-by-feature, aligned with your current modules)

```
com.api
 ├─ boot.controller
 │   └─ common
 │       └─ FileController                  // HTTP endpoints (upload, urls, delete)
 │
 ├─ common
 │   ├─ domain
 │   │   ├─ SysFileObject                   // DB metadata entity
 │   │   ├─ dto
 │   │   │   ├─ FileUploadRequest           // category, bizType, bizId (optional)
 │   │   │   ├─ FileUploadResult            // fileId, objectKey, originalName, size, contentType
 │   │   │   ├─ FileUrlResponse             // url, expireAt
 │   │   │   ├─ BatchDeleteRequest          // fileIds[]
 │   │   │   └─ BatchResult<T>              // per-item success/failure
 │   │   └─ enums
 │   │       ├─ FileCategoryEnum            // AVATAR, EXCEL, DOC, FILE, ATTACHMENT
 │   │       └─ FileVisibilityEnum          // PUBLIC, PRIVATE
 │   │
 │   ├─ constant
 │   │   └─ FileConstants                   // default expiry, max limits (fallback)
 │   │
 │   └─ utils
 │       ├─ FileUploadUtils                 // reuse/extend your current validation helpers
 │       ├─ FileTypeUtils
 │       ├─ MimeTypeUtils
 │       └─ ImageUtils
 │
 ├─ config
 │   ├─ MinioProperties                     // @ConfigurationProperties app.minio.*
 │   ├─ FileStorageProperties               // @ConfigurationProperties app.file.*
 │   ├─ MinioConfig                         // MinioClient bean
 │   └─ FileStorageConfig                   // wiring strategy, bean selection
 │
 ├─ framework.storage
 │   ├─ StorageClient                       // abstraction: put/delete/presign/stat
 │   ├─ MinioStorageClient                  // MinIO implementation
 │   └─ model
 │       ├─ StoredObjectMeta                // etag, size, contentType, bucket, objectKey
 │       └─ PresignedUrl                    // url, expireAt
 │
 └─ system
     ├─ repository
     │   └─ SysFileObjectRepository          // JPA repository
     │
     └─ service
         ├─ FileAppService                  // application service (business flow)
         ├─ FilePolicyService               // category rules (prefix, allowed types, visibility)
         ├─ FilePermissionService           // access control: owner/admin/biz binding
         └─ FileCleanupService              // optional: orphan cleanup / scheduled jobs
```

---

## 2) Configuration Plan (YAML-driven)

### `app.minio` (connection + bucket)

* endpoint
* accessKey / secretKey
* bucket
* defaultPresignExpirySeconds

### `app.file` (category rules)

For each category:

* prefix (directory)
* maxSizeMb
* allowedExtensions (e.g. png/jpg/pdf/xlsx)
* visibility (PUBLIC/PRIVATE)
* presignExpirySeconds (default TTL per category)

**Design principle:** new file category should require only YAML + enum entry (or fully YAML-driven without enum if you
prefer).

---

## 3) Core Responsibilities (Who does what)

### `FileController` (HTTP only)

* Validate request shape (required params)
* Call `FileAppService`
* Return `AjaxResult` payloads consistent with your project

Endpoints (suggested):

* `POST /common/upload` (single)
* `POST /common/uploads` (multi)
* `GET /common/file-url?fileId=...` (presigned GET)
* `DELETE /common/files/{fileId}` (single delete)
* `DELETE /common/files` (batch delete)

---

### `FileAppService` (transaction + orchestration)

Orchestrates:

* read current userId from `SecurityUtils`
* apply policy from `FilePolicyService`
* validate file using your existing `FileUploadUtils`/`MimeTypeUtils`
* upload to MinIO via `StorageClient`
* persist metadata via `SysFileObjectRepository`
* generate presigned URL if requested (or for preview)
* delete: permission check → delete object → update DB

---

### `FilePolicyService` (rules & mapping)

* Translate category → directory prefix + constraints
* Provide default TTL and visibility per category
* Central place to enforce:

    * avatar must be image
    * excel must be xls/xlsx
    * docs must be pdf/doc/docx
    * etc.

---

### `FilePermissionService` (security)

* `canRead(fileId, userId)`
* `canDelete(fileId, userId)`
* Default rule:

    * owner can read/delete
    * admin can read/delete
    * optionally: biz binding permissions (phase 2)

---

### `StorageClient` (pluggable storage layer)

Interface methods:

* `putObject(bucket, objectKey, stream, size, contentType) -> StoredObjectMeta`
* `presignGet(bucket, objectKey, expiry) -> PresignedUrl`
* `presignPut(bucket, objectKey, expiry) -> PresignedUrl` (optional for large files)
* `removeObject(bucket, objectKey)`
* `statObject(bucket, objectKey)`

Implementation:

* `MinioStorageClient` uses MinIO SDK only (no business logic)

---

## 4) Data Model Plan (SysFileObject)

Store **metadata** and **ownership**:

* fileId
* bucket
* objectKey
* originalName
* contentType
* sizeBytes
* etag
* ownerUserId
* category
* visibility
* bizType / bizId (optional)
* status / delFlag + audit fields

This enables:

* batch delete
* permission control
* audit/history

---

## 5) Workflows (High-level)

### Upload (single/multiple)

1. Controller receives multipart
2. AppService resolves category policy (prefix + rules)
3. Validate file (size/ext/mime)
4. Generate objectKey (prefix + date + userId + uuid)
5. Upload to MinIO
6. Save metadata to DB
7. Return fileId (+ optional preview presigned GET)

### Access (time-sensitive token)

1. Controller requests URL for fileId
2. AppService loads metadata + checks permission
3. Generate presigned GET with expiry from policy (or request override within max)
4. Return `{url, expireAt}`

### Delete (single/batch)

1. AppService checks permission for each fileId
2. Delete from MinIO (best-effort per item)
3. Update DB (soft delete recommended)
4. Return per-item results

---

## 6) Phase Plan (Implementation Roadmap)

### Phase 1 (MVP)

* Single upload + multiple upload
* Metadata table + repository
* Presigned GET for private files
* Single delete + batch delete
* YAML policies per category

### Phase 2 (Hardening)

* Presigned PUT for large file direct-to-MinIO uploads
* Antivirus / file scanning hook (optional)
* Orphan cleanup job (MinIO object exists but DB record missing, or vice versa)
* Business binding permission checks (bizType/bizId)

### Phase 3 (Ops & Scaling)

* Nginx domain for MinIO public resources (optional)
* CDN support
* Rate limits / quotas per user/category
* Audit logs for downloads/deletes

---

## 7) Compatibility Notes with Your Current Code

* Keep `AjaxResult` response format
* Use `SecurityUtils.getUserId()` for ownership
* Keep your existing file validation utilities (extend where needed)
* Keep service style: `@Slf4j`, `@RequiredArgsConstructor`, transactional boundaries in AppService
* Follow your “bridge entity only” rule for future relations (e.g., file-to-biz mappings can be a bridge table)

---
