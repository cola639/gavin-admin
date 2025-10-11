package com.api.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Department entity (sys_dept) Represents hierarchical department structure with soft delete
 * support.
 */
@Getter
@Setter
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sys_dept")
@SQLDelete(sql = "UPDATE sys_dept SET del_flag = '2' WHERE dept_id = ?")
@Where(clause = "del_flag = '0'")
public class SysDept extends BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Primary key */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "dept_id")
  private Long deptId;

  /** Parent department ID */
  @Column(name = "parent_id")
  private Long parentId;

  /** Ancestors path (comma-separated parent IDs) */
  @Column(name = "ancestors", length = 50)
  private String ancestors;

  /** Department name */
  @NotBlank
  @Size(max = 30)
  @Column(name = "dept_name", length = 30, nullable = false)
  private String deptName;

  /** Order number */
  @NotNull
  @Column(name = "order_num")
  private Integer orderNum;

  /** Leader name */
  @Column(name = "leader", length = 20)
  private String leader;

  /** Phone number */
  @Size(max = 11)
  @Column(name = "phone", length = 11)
  private String phone;

  /** Email */
  @Email
  @Size(max = 50)
  @Column(name = "email", length = 50)
  private String email;

  /** Department status (0=active, 1=disabled) */
  @Column(name = "status", length = 1)
  private String status = "0";

  /** Soft delete flag (0=exists, 2=deleted) */
  @Column(name = "del_flag", length = 1)
  private String delFlag = "0";

  /** Parent department reference (optional) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", insertable = false, updatable = false)
  @JsonIgnore // Prevent JSON recursion
  @ToString.Exclude
  private SysDept parent;

  /** Child departments */
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  @ToString.Exclude
  private List<SysDept> children = new ArrayList<>();
}
