package com.api.common.domain.entity;

import java.util.Date;
import java.util.List;

//import javax.validation.constraints.*;

import com.api.common.domain.entity.BaseEntity;
import com.api.common.domain.entity.SysDept;
import com.api.common.domain.entity.SysRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

/**
 * User entity mapped to sys_user table.
 * <p>
 * Represents user information within the system, including account details,
 * department association, roles, and audit fields.
 * </p>
 * <p>
 * Features:
 * - Validation annotations for user input
 * - Excel annotations for batch import/export
 * - Department and role relationships
 * - Utility method for admin detection
 * <p>
 * Lombok annotations reduce boilerplate by auto-generating getters, setters, constructors, etc.
 * <p>
 * Author: ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Unique User ID */
    private Long userId;

    /** Department ID */
    private Long deptId;

    /** Login username */
    // @Xss(message = "Username cannot contain script characters")
    @NotBlank(message = "Username cannot be empty")
    @Size(max = 30, message = "Username length cannot exceed 30 characters")
    private String userName;

    /** Display nickname */
    //  @Xss(message = "Nickname cannot contain script characters")
    @Size(max = 30, message = "Nickname length cannot exceed 30 characters")
    private String nickName;

    /** Email address */
    // @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email length cannot exceed 50 characters")
    //  @Excel(name = "Email")
    private String email;

    /** Phone number */
    @Size(max = 11, message = "Phone number cannot exceed 11 digits")
    // @Excel(name = "Phone Number", cellType = ColumnType.TEXT)
    private String phonenumber;

    /** Gender (0=Male, 1=Female, 2=Unknown) */
    // @Excel(name = "Gender", readConverterExp = "0=Male,1=Female,2=Unknown")
    private String sex;

    /** Avatar URL */
    private String avatar;

    /** Encrypted password */
    private String password;

    /** Account status (0=Active, 1=Disabled) */
    // @Excel(name = "Account Status", readConverterExp = "0=Active,1=Disabled")
    private String status;

    /** Deletion flag (0=Exists, 2=Deleted) */
    private String delFlag;

    /** Last login IP */
    // @Excel(name = "Last Login IP", type = Type.EXPORT)
    private String loginIp;

    /** Last login time */
    // @Excel(name = "Last Login Time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /** Last password update timestamp */
    private Date pwdUpdateDate;

    /** Associated department */
    private SysDept dept;

    /** Associated roles */
    private List<SysRole> roles;

    /** Array of role IDs */
    private Long[] roleIds;

    /** Array of post IDs */
    private Long[] postIds;

    /** Single role ID (legacy use) */
    private Long roleId;

    /**
     * Check if this user is an administrator.
     *
     * @return true if userId equals 1
     */
    public boolean isAdmin() {
        return isAdmin(this.userId);
    }

    /**
     * Static helper to check admin status by userId.
     *
     * @param userId User ID
     * @return true if userId equals 1
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && userId == 1L;
    }
}
