package com.api.common.domain;

import com.api.common.enums.UserStatusEnum;
import com.api.common.validation.DictValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

/**
 * User entity mapped to sys_user table.
 *
 * <p>Represents user information within the system, including account details, department
 * association, roles, and audit fields.
 *
 * <p>Features: - Validation annotations for user input - JPA annotations for persistence mapping -
 * Department and role relationships - Utility method for admin detection
 *
 * <p>Lombok annotations reduce boilerplate by auto-generating getters, setters, constructors, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
public class SysUser extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "dept_id")
  private Long deptId;

  /** Login username */
  //  @NotBlank(message = "Username cannot be empty")
  @Size(max = 30, message = "Username length cannot exceed 30 characters")
  @Column(name = "user_name", length = 30, unique = true)
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

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "password")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  @DictValidator(
      value = UserStatusEnum.class,
      allowNull = false,
      message = "User status code is invalid")
  @Column(name = "status")
  private String status;

  @Column(name = "del_flag")
  private String delFlag;

  @Column(name = "login_ip")
  private String loginIp;

  @Column(name = "login_date")
  private Date loginDate;

  @Column(name = "pwd_update_date")
  private Date pwdUpdateDate;

  @Column(name = "user_type", length = 64)
  private String userType;

  @Column(name = "oauth_id", length = 64)
  private String oauthId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "dept_id", insertable = false, updatable = false)
  private SysDept dept;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "sys_user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @org.hibernate.annotations.Immutable
  private List<SysRole> roles;

  /** Array of role IDs (not persisted, helper field) */
  @Transient private Long[] roleIds;

  /** Array of post IDs (not persisted, helper field) */
  @Transient private Long[] postIds;

  /** Single role ID (legacy use, not persisted) */
  @Transient private Long roleId;

  public boolean isAdmin() {
    return userId != null && userId == 1L;
  }
}
