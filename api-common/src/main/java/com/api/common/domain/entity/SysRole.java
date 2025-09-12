package com.api.common.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Role entity (corresponds to table sys_role).
 * <p>
 * Represents user roles in the system, including their permissions,
 * data access scope, and status. Roles are central to RBAC (Role-Based Access Control),
 * where users are assigned roles and roles define permissions.
 * <p>
 * Inherits common audit fields (createdBy, createdTime, etc.) from BaseEntity.
 * <p>
 * Example:
 * - Admin role with full access
 * - Manager role with department-level access
 * - Employee role with self-only access
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Unique ID of the role */
    private Long roleId;

    /** Role name */
    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 30, message = "Role name cannot exceed 30 characters")
    private String roleName;

    /** Role key (permission string, e.g. "system:user:view") */
    @NotBlank(message = "Role key cannot be blank")
    @Size(max = 100, message = "Role key cannot exceed 100 characters")
    private String roleKey;

    /** Display order */
    @NotNull(message = "Display order cannot be null")
    private Integer roleSort;

    /**
     * Data scope:
     * 1 = All data
     * 2 = Custom data
     * 3 = Department data
     * 4 = Department and sub-departments
     * 5 = Self only
     */
    private String dataScope;

    /** Whether menu selection should enforce parent-child linkage (true/false) */
    private boolean menuCheckStrictly;

    /** Whether department selection should enforce parent-child linkage (true/false) */
    private boolean deptCheckStrictly;

    /** Role status (0 = Active, 1 = Disabled) */
    private String status;

    /** Delete flag (0 = Exists, 2 = Deleted) */
    private String delFlag;

    /** Whether the current user already has this role (used for UI display, default = false) */
    private boolean flag = false;

    /** Menu IDs associated with the role */
    private Long[] menuIds;

    /** Department IDs associated with the role (used for data scope) */
    private Long[] deptIds;

    /** Permission strings associated with the role */
    private Set<String> permissions;

    public SysRole(Long roleId) {
        this.roleId = roleId;
    }

    /**
     * Checks if the given roleId represents the super admin role.
     * By convention, roleId = 1 is considered as admin.
     */
    public static boolean isAdmin(Long roleId) {
        return roleId != null && roleId == 1L;
    }

    /** Convenience method for current role object */
    public boolean isAdmin() {
        return isAdmin(this.roleId);
    }
}
