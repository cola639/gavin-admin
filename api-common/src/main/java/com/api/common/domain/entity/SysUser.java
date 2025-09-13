package com.api.common.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

/**
 * User entity mapped to sys_user table.
 * <p>
 * Represents user information within the system, including account details,
 * department association, roles, and audit fields.
 * </p>
 * <p>
 * Features:
 * - Validation annotations for user input
 * - JPA annotations for persistence mapping
 * - Department and role relationships
 * - Utility method for admin detection
 * <p>
 * Lombok annotations reduce boilerplate by auto-generating getters, setters, constructors, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Unique User ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /** Department ID */
    @Column(name = "dept_id")
    private Long deptId;

    /** Login username */
    @NotBlank(message = "Username cannot be empty")
    @Size(max = 30, message = "Username length cannot exceed 30 characters")
    @Column(name = "user_name")
    private String userName;

    /** Display nickname */
    @Size(max = 30, message = "Nickname length cannot exceed 30 characters")
    @Column(name = "nick_name")
    private String nickName;

    /** Email address */
    @Size(max = 50, message = "Email length cannot exceed 50 characters")
    @Column(name = "email")
    private String email;

    /** Phone number */
    @Size(max = 11, message = "Phone number cannot exceed 11 digits")
    @Column(name = "phonenumber")
    private String phonenumber;

    /** Gender (0=Male, 1=Female, 2=Unknown) */
    @Column(name = "sex")
    private String sex;

    /** Avatar URL */
    @Column(name = "avatar")
    private String avatar;

    /** Encrypted password */
    @Column(name = "password")
    private String password;

    /** Account status (0=Active, 1=Disabled) */
    @Column(name = "status")
    private String status;

    /** Deletion flag (0=Exists, 2=Deleted) */
    @Column(name = "del_flag")
    private String delFlag;

    /** Last login IP */
    @Column(name = "login_ip")
    private String loginIp;

    /** Last login time */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "login_date")
    private Date loginDate;

    /** Last password update timestamp */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "pwd_update_date")
    private Date pwdUpdateDate;

    /** Associated department */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dept_id", insertable = false, updatable = false)
    private SysDept dept;

    /** Associated roles */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<SysRole> roles;

    /** Array of role IDs (not persisted, helper field) */
    @Transient
    private Long[] roleIds;

    /** Array of post IDs (not persisted, helper field) */
    @Transient
    private Long[] postIds;

    /** Single role ID (legacy use, not persisted) */
    @Transient
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
