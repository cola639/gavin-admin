package com.api.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Department entity (sys_dept) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sys_dept")
@SQLDelete(sql = "UPDATE sys_dept SET del_flag = '2' WHERE dept_id = ?")
@Where(clause = "del_flag = '0'")
public class SysDept extends BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dept_id")
  private Long deptId;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(name = "ancestors", length = 50)
  private String ancestors;

  @NotBlank
  @Size(max = 30)
  @Column(name = "dept_name", length = 30, nullable = false)
  private String deptName;

  @NotNull
  @Column(name = "order_num")
  private Integer orderNum;

  @Column(name = "leader", length = 20)
  private String leader;

  @Size(max = 11)
  @Column(name = "phone", length = 11)
  private String phone;

  @Email
  @Size(max = 50)
  @Column(name = "email", length = 50)
  private String email;

  @Column(name = "status", length = 1)
  private String status = "0";

  @Column(name = "del_flag", length = 1)
  private String delFlag = "0";

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  @JsonIgnore
  private SysDept parent;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @JsonIgnore
  private List<SysDept> children = new ArrayList<>();
}
