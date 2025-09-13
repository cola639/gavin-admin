package com.api.common.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Role entity mapped to sys_role table.
 * <p>
 * Represents roles within the system, defining access permissions and scope.
 * Roles are used in RBAC (Role-Based Access Control).
 * <p>
 * Features:
 * - JPA annotations for persistence mapping
 * - Validation annotations for role attributes
 * - Relationships with users, menus, and departments
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Unique ID of the role */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    /** Role name */
    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 30, message = "Role name cannot exceed 30 characters")
    @Column(name = "role_name")
    private String roleName;

    /** Role key (permission string, e.g. "system:user:view") */
    @NotBlank(message = "Role key cannot be blank")
    @Size(max = 100, message = "Role key cannot exceed 100 characters")
    @Column(name = "role_key")
    private String roleKey;

    /** Display order */
    @NotNull(message = "Display order cannot be null")
    @Column(name = "role_sort")
    private Integer roleSort;

    /**
     * Data scope:
     * 1 = All data
     * 2 = Custom data
     * 3 = Department data
     * 4 = Department and sub-departments
     * 5 = Self only
     */
    @Column(name = "data_scope")
    private String dataScope;

    /** Whether menu selection enforces parent-child linkage */
    @Column(name = "menu_check_strictly")
    private boolean menuCheckStrictly;

    /** Whether department selection enforces parent-child linkage */
    @Column(name = "dept_check_strictly")
    private boolean deptCheckStrictly;

    /** Role status (0 = Active, 1 = Disabled) */
    @Column(name = "status")
    private String status;

    /** Delete flag (0 = Exists, 2 = Deleted) */
    @Column(name = "del_flag")
    private String delFlag;

    /** Whether the current user already has this role (UI helper field, not persisted) */
    @Transient
    private boolean flag = false;

    /** Menu IDs associated with the role (helper, not persisted) */
    @Transient
    private Long[] menuIds;

    /** Department IDs associated with the role (helper, not persisted) */
    @Transient
    private Long[] deptIds;

    /** Permission strings associated with the role (helper, not persisted) */
    @Transient
    private Set<String> permissions;

    /** Constructor with roleId */
    public SysRole(Long roleId) {
        this.roleId = roleId;
    }

    /**
     * Check if the given roleId represents the super admin role.
     * By convention, roleId = 1 is considered admin.
     */
    public static boolean isAdmin(Long roleId) {
        return roleId != null && roleId == 1L;
    }

    /** Convenience method for current role object */
    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }
}
