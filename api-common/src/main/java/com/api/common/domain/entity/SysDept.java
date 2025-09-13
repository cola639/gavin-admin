package com.api.common.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_dept")
public class SysDept extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "ancestors")
    private String ancestors;

    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "leader")
    private String leader;

    @Size(max = 11, message = "Phone number cannot exceed 11 characters")
    @Column(name = "phone")
    private String phone;

    @Size(max = 50, message = "Email cannot exceed 50 characters")
    @Column(name = "email")
    private String email;

    @Column(name = "status")
    private String status;

    @Column(name = "del_flag")
    private String delFlag;

    @Transient
    private String parentName;

    @Transient
    private List<SysDept> children = new ArrayList<>();
}
