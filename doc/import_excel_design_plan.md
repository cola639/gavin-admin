# Excel Import Design Plan (Project Standard)

This document captures the current Excel import design used in this repo and serves as a reusable template for new import types.

## 1) Scope and goals

- Provide a consistent, testable pipeline for Excel imports.
- Separate responsibilities: read, map, validate, generate, persist.
- Support update vs create, dry-run, and row-level error reporting.

## 2) High-level data flow

1. `ExcelReader` loads an `ExcelSheet` using `ExcelReadSpec`.
2. `ImportRowMapper` converts each `ExcelRow` to a typed row DTO.
3. `ImportRowValidator` validates and returns `ImportValidationResult` with row context.
4. `ImportRowGenerator` normalizes values and creates a persistent entity.
5. `ImportRowWriter` persists the entity.
6. `ImportService` aggregates results and errors.

## 3) Base contracts (shared across import types)

Located in `api-system/src/main/java/com/api/system/imports/base`:

- `ImportRowMapper<R>`: map raw `ExcelRow` to typed row DTO. No business rules.
- `ImportRowValidator<R, C>`: validate and return `ImportValidationResult<C>`.
- `ImportRowGenerator<R, C, E>`: create or update entity from row + context.
- `ImportRowWriter<E>`: persist entity.
- `ImportBatchContext`: per-import state (updateSupport, duplicate detection).
- `ImportValidationResult<C>`: validation errors + context; `isValid()` helper.

## 4) Excel read rules (current behavior)

Using `api-common` Excel utilities:

- `ExcelReadSpec(sheetName, headerRowIndex, expectedHeaders, allowExtraHeaders)`
- Headers are normalized with trim + whitespace collapse + case-insensitive compare.
- Empty rows are skipped (row with no cell values).
- `ExcelRow.rowIndex` is 1-based and matches user-facing row numbers.
- Header mismatches throw `ExcelHeaderMismatchException` with expected vs actual.

## 5) Import contract (per import type)

Example: `UserImportContract`

Responsibilities:

- Define sheet name, column headers, required headers, allowed values, defaults.
- Provide `readSpec()` for `ExcelReader`.
- Normalize user-provided strings into canonical values:
  - `normalizeStatus`, `normalizeSex`, `normalizeUserType`.
  - Map aliases to stored codes via constant maps.

Guideline:

- Keep column definitions and header lists in one place to avoid drift.
- Use constants for shared strings across mapper/validator/generator.

## 6) Row DTO and mapping

Example: `UserImportRow`

- Plain DTO with fields matching columns.
- `UserImportRowMapper` trims values from `ExcelRow.getValues()`.
- Mapping should be deterministic and side-effect free.

## 7) Validation layer

Example: `UserImportValidator`

Core responsibilities:

- Required fields and length constraints.
- Allowed values using contract normalization.
- Cross-field and repository checks.
- Duplicate detection within the file via `ImportBatchContext.getSeenKeys()`.

Validation output:

- `ImportValidationResult` contains:
  - `errors`: list of messages.
  - `context`: resolved data for generation (e.g., existing entity, foreign keys).

## 8) Generation layer

Example: `UserImportGenerator`

Responsibilities:

- Normalize data and apply defaults.
- Respect update vs create:
  - On update, only set optional fields when provided.
  - On create, set defaults for missing optional fields.
- Use `ImportValidationResult.context` for resolved references.
- Set audit fields (`createBy`, `updateBy`) and default flags.

## 9) Persistence layer

Example: `UserImportWriter`

- Single responsibility: save entity via repository.
- No validation or transformation here.

## 10) Orchestration (service layer)

Example: `UserImportService`

Workflow:

- Read Excel with `UserImportContract.readSpec()`.
- Loop rows and apply: map -> validate -> generate -> save.
- Support:
  - `updateSupport` for upsert behavior.
  - `dryRun` to skip writes.
- Aggregate:
  - total rows, created, updated, errors.
  - per-row errors with row index + identifier.

## 11) Result + error reporting

Example: `UserImportResult` and `UserImportRowError`

- `UserImportRowError` includes row index, row identifier, and messages.
- `UserImportResult` captures totals and error list for UI display.

## 12) Checklist for adding a new import

Use this as a repeatable template:

1. Create `[Entity]ImportContract` with:
   - sheet name, column constants, headers, required headers.
   - allowed values, defaults, alias normalization.
   - `readSpec()` config.
2. Create `[Entity]ImportRow` DTO.
3. Implement `[Entity]ImportRowMapper`.
4. Implement `[Entity]ImportValidator`:
   - required fields and length rules.
   - allowed values and DB checks.
   - file-level duplicate detection via `ImportBatchContext`.
   - context for generator (existing entity, resolved IDs).
5. Implement `[Entity]ImportGenerator`:
   - normalization, defaults, update logic.
   - audit fields.
6. Implement `[Entity]ImportWriter`.
7. Implement `[Entity]ImportService`:
   - use `ExcelReader`.
   - map -> validate -> generate -> write.
   - return `[Entity]ImportResult`.
8. Add a sample Excel file in `doc/import-test/`.
9. Add tests for validator and generator (at minimum).

## 13) Practical notes (from current design)

- Keep validation and generation separate to make dry-run meaningful.
- Use consistent error message strings for predictable UI display.
- Make normalization functions in the contract so validator and generator stay in sync.

