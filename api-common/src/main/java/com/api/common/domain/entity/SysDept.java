package com.api.common.domain.entity;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Department entity (corresponds to table sys_dept).
 * <p>
 * Represents organizational structure such as departments, sub-departments,
 * leaders, and contact information.
 * <p>
 * Inherits common audit fields (createdBy, createdTime, etc.) from BaseEntity.
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SysDept extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** Unique ID of the department */
    private Long deptId;

    /** Parent department ID */
    private Long parentId;

    /** Ancestor hierarchy (e.g. "1,2,3") */
    private String ancestors;

    /** Department name */
    //  @NotBlank(message = "Department name cannot be blank")
    //  @Size(max = 30, message = "Department name cannot exceed 30 characters")
    private String deptName;

    /** Display order */
    //  @NotNull(message = "Display order cannot be null")
    private Integer orderNum;

    /** Person in charge */
    private String leader;

    /** Contact phone number */
    @Size(max = 11, message = "Phone number cannot exceed 11 characters")
    private String phone;

    /** Contact email */
//    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    /** Department status (0 = Active, 1 = Disabled) */
    private String status;

    /** Delete flag (0 = Exists, 2 = Deleted) */
    private String delFlag;

    /** Parent department name (for display only, not stored in DB) */
    private String parentName;

    /** Child departments (used for building department tree structures) */
    private List<SysDept> children = new ArrayList<>();

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("deptId", deptId)
                .append("parentId", parentId)
                .append("ancestors", ancestors)
                .append("deptName", deptName)
                .append("orderNum", orderNum)
                .append("leader", leader)
                .append("phone", phone)
                .append("email", email)
                .append("status", status)
                .append("delFlag", delFlag)
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
