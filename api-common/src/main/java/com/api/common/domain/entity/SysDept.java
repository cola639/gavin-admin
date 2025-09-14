package com.api.common.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Department entity mapped to table sys_dept.
 * Represents organizational structure including hierarchy, contact info, and status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_dept")
public class SysDept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Department ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    /** Parent department ID */
    @Column(name = "parent_id")
    private Long parentId;

    /** Ancestor hierarchy (e.g. "1,2,3") */
    @Column(name = "ancestors")
    private String ancestors;

    /** Department name */
    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 30, message = "Department name cannot exceed 30 characters")
    @Column(name = "dept_name")
    private String deptName;

    /** Display order */
    @NotNull(message = "Display order cannot be null")
    @Column(name = "order_num")
    private Integer orderNum;

    /** Person in charge */
    @Column(name = "leader")
    private String leader;

    /** Contact phone number */
    @Size(max = 11, message = "Phone number cannot exceed 11 characters")
    @Column(name = "phone")
    private String phone;

    /** Contact email */
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Column(name = "email")
    private String email;

    /** Department status (0 = Active, 1 = Disabled) */
    @Column(name = "status")
    private String status;

    /** Delete flag (0 = Exists, 2 = Deleted) */
    @Column(name = "del_flag")
    private String delFlag;

    /** Parent department name (helper, not persisted) */
    @Transient
    private String parentName;

    /** Child departments (helper, not persisted) */
    @Transient
    @Builder.Default
    private List<SysDept> children = new ArrayList<>();
}
