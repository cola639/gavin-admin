To understand your project structure well enough to **write code that matches your style, patterns, and constraints**,
you don’t need to give me everything—just the “shape” + the key conventions.

Here’s the best set of materials to provide (in priority order). If you provide items 1–6, I can usually follow your
structure very accurately.

---

## 1) Project package tree (most important)

Give a quick tree of your `src/main/java` (2–4 levels deep), for example:

* `com.api.boot.controller...`
* `com.api.system.service...`
* `com.api.system.repository...`
* `com.api.common.domain...`
* etc.

Even a screenshot is OK, but text tree is best.

---

## 2) Key base classes / common conventions

Upload or paste these files (or their key parts):

* `BaseEntity`
* `AjaxResult`
* `LoginUser`
* `SecurityUtils`
* `Constants`
* your exception / error handler (global exception advice)

These define how responses, auditing fields, and auth context work.

---

## 3) One complete “feature” as a reference

Pick one module that you think is “well-written” and provide:

* Entity (e.g., `SysUser`)
* Repository
* Service interface + impl (if separated)
* Controller
* DTO + Mapper (if used)

This lets me copy your exact layering and naming style.

---

## 4) JPA relationship rules (your project-specific)

You already said:

> Only `@OneToMany` / `@ManyToOne` with bridge entity, associations for querying only, save/update manually.

To follow this perfectly, I need **one existing example** of a bridge entity pattern from your codebase (e.g.,
`SysUserRole` style).

If you don’t have one yet, tell me:

* How you want your join tables named
* Whether you prefer `*_id` columns or `camelCase`
* Whether you use soft delete on bridge tables

---

## 5) Configuration & dependencies

Provide:

* `pom.xml` (or the relevant dependency sections)
* `application.yml` (or the relevant parts: datasource, jpa, security oauth2, redis, jwt)

This avoids me suggesting code that conflicts with your libs (Redis? MyBatis? MapStruct?).

---

## 6) Database schema (DDL) for the tables involved

For the feature you want to build next, paste:

* `CREATE TABLE ...` or at least column list + indexes + constraints

This helps me generate correct entities and queries.

---

## 7) Coding standards (optional but helpful)

If you have:

* a “Java Development Handbook” rules summary you follow
* naming conventions (DTO suffix? Request/Response naming? logging format?)

Share the key bullets.

---

### Minimal set if you want the fastest onboarding

If you want the shortest path, send just these 3:

1. package tree
2. one complete feature (entity/repo/service/controller)
3. `pom.xml` + relevant `application.yml` sections

Then I can write new code that blends into your project immediately.

If you tell me what feature you want to implement next (e.g., “user binds GitHub account” / “menu tree API”), I can tell
you exactly which tables/files are needed and keep the materials minimal.
