Here’s a **battle-tested enterprise approach** for “data scope” (数据权限) based on:

* **different dept**
* **same dept but different role**
* **multiple roles per user**
* **deny-by-default**
* enforced **only in backend** (frontend never decides)

---

## Table design (MySQL)

### 1) Dept table

```sql
create table sys_dept
(
    dept_id   BIGINT primary key AUTO_INCREMENT,
    parent_id BIGINT null,
    dept_name VARCHAR(50) not null,
    status    VARCHAR(20) not null default 'Enabled',
    del_flag  VARCHAR(20) not null default 'Normal'
);
```

### 2) Dept closure table (best practice for “dept + children”)

This avoids recursive queries and is fast.

```sql
create table sys_dept_relation
(
    ancestor_id   BIGINT not null,
    descendant_id BIGINT not null,
    depth         INT    not null,
    primary key (ancestor_id, descendant_id),
    INDEX         idx_descendant (descendant_id),
    constraint fk_rel_ancestor foreign key (ancestor_id) references sys_dept (dept_id),
    constraint fk_rel_descendant foreign key (descendant_id) references sys_dept (dept_id)
);
```

### 3) Role data scope

```sql
create table sys_role
(
    role_id    BIGINT primary key AUTO_INCREMENT,
    role_name  VARCHAR(50) not null,
    data_scope VARCHAR(30) not null
    -- values: ALL, DEPT, DEPT_AND_CHILD, SELF, CUSTOM
);
```

### 4) Custom role depts

```sql
create table sys_role_dept
(
    role_id BIGINT not null,
    dept_id BIGINT not null,
    primary key (role_id, dept_id)
);
```

### 5) User + user-role

```sql
create table sys_user_role
(
    user_id BIGINT not null,
    role_id BIGINT not null,
    primary key (user_id, role_id)
);
```

---

## Data scope model

### Enum (Java)

```java
public enum DataScope {
    ALL,
    DEPT,
    DEPT_AND_CHILD,
    SELF,
    CUSTOM
}
```

> Enterprise rule: **a user can have multiple roles; final scope is OR/UNION**
> If any role is `ALL` → no restriction.

---

## JPA entities (minimal)

### SysRole (only relevant fields shown)

```java

@Entity
@Table(name = "sys_role")
@Data
public class SysRole {
    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "data_scope")
    private String dataScope; // store "ALL/DEPT/..."
}
```

### Dept relation entity

```java

@Entity
@Table(name = "sys_dept_relation")
@IdClass(SysDeptRelationId.class)
@Data
public class SysDeptRelation {

    @Id
    @Column(name = "ancestor_id")
    private Long ancestorId;

    @Id
    @Column(name = "descendant_id")
    private Long descendantId;

    @Column(name = "depth")
    private Integer depth;
}

@Data
public class SysDeptRelationId implements java.io.Serializable {
    private Long ancestorId;
    private Long descendantId;
}
```

---

## Repositories

```java
public interface SysDeptRelationRepository extends JpaRepository<SysDeptRelation, SysDeptRelationId> {

    @Query("select r.descendantId from SysDeptRelation r where r.ancestorId = :ancestorId")
    List<Long> findDescendantIds(@Param("ancestorId") Long ancestorId);
}

public interface SysRoleDeptRepository extends JpaRepository<SysRoleDept, SysRoleDeptId> {

    @Query("select rd.deptId from SysRoleDept rd where rd.roleId in :roleIds")
    List<Long> findDeptIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
```

(Your `SysRoleDept` is just a simple “middle table entity”, same pattern as above.)

---

## Best practice JPA query: Specification-based “data scope”

### DataScopeSpecificationFactory (core idea)

Apply this to any query that has `deptId` (and optionally `userId`).

```java

@Slf4j
@RequiredArgsConstructor
@Component
public class DataScopeSpecificationFactory {

    private final SysDeptRelationRepository deptRelationRepository;
    private final SysRoleDeptRepository roleDeptRepository;

    public <T> Specification<T> build(Long currentUserId,
                                      Long currentDeptId,
                                      List<SysRole> roles,
                                      String deptField,
                                      String userField) {

        return (root, query, cb) -> {

            if (roles == null || roles.isEmpty()) {
                log.warn("No roles found for userId={}, deny all by default.", currentUserId);
                return cb.disjunction(); // always false
            }

            // If any role is ALL -> no restriction
            boolean hasAll = roles.stream().anyMatch(r -> "ALL".equalsIgnoreCase(r.getDataScope()));
            if (hasAll) {
                log.debug("Data scope ALL applied for userId={}", currentUserId);
                return cb.conjunction(); // always true
            }

            List<jakarta.persistence.criteria.Predicate> orPredicates = new ArrayList<>();

            for (SysRole role : roles) {
                String scope = role.getDataScope();

                if ("DEPT".equalsIgnoreCase(scope)) {
                    orPredicates.add(cb.equal(root.get(deptField), currentDeptId));
                }

                if ("DEPT_AND_CHILD".equalsIgnoreCase(scope)) {
                    List<Long> deptIds = deptRelationRepository.findDescendantIds(currentDeptId);
                    orPredicates.add(root.get(deptField).in(deptIds));
                }

                if ("SELF".equalsIgnoreCase(scope)) {
                    orPredicates.add(cb.equal(root.get(userField), currentUserId));
                }

                if ("CUSTOM".equalsIgnoreCase(scope)) {
                    List<Long> customDeptIds = roleDeptRepository.findDeptIdsByRoleIds(List.of(role.getRoleId()));
                    if (!customDeptIds.isEmpty()) {
                        orPredicates.add(root.get(deptField).in(customDeptIds));
                    }
                }
            }

            if (orPredicates.isEmpty()) {
                log.warn("No data scope predicate matched. Deny all. userId={}", currentUserId);
                return cb.disjunction();
            }

            return cb.or(orPredicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
```

### How to use in a service query

Example: listing users (`SysUser` has `deptId`, `userId`)

```java
public Page<SysUser> listUsers(SysUser criteria, Pageable pageable, LoginUser loginUser) {

    Specification<SysUser> baseSpec =
            SpecificationBuilder.<SysUser>builder()
                    .eq("delFlag", "Normal")
                    .like("userName", criteria.getUserName())
                    .eq("status", criteria.getStatus());

    Specification<SysUser> dataScopeSpec =
            dataScopeSpecificationFactory.build(
                    loginUser.getUserId(),
                    loginUser.getDeptId(),
                    loginUser.getRoles(),
                    "deptId",
                    "userId"
            );

    return userRepository.findAll(baseSpec.and(dataScopeSpec), pageable);
}
```

---

## Why this is “best enterprise practice”

* ✅ Data scope is enforced **server-side** (frontend cannot bypass)
* ✅ Supports “same dept but different role” by **union across roles**
* ✅ Uses **closure table** for dept subtree (fast and scalable)
* ✅ Reusable across entities (just pass `deptField`, `userField`)
* ✅ Deny-by-default (security first)

---

If you tell me your exact roles you want (like RuoYi’s `1=ALL,2=CUSTOM,3=DEPT,4=DEPT_AND_CHILD,5=SELF`), I can align
this to your existing enum/string style and show the **full DDL + entity for sys_role_dept** too.
