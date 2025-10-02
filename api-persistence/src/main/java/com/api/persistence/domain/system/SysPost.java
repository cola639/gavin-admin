package com.api.persistence.domain.system;

import com.api.persistence.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Post entity mapped to sys_post table.
 *
 * <p>Represents a job position in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sys_post")
public class SysPost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "post_id")
  private Long postId;

  @Column(name = "post_code")
  private String postCode;

  @Column(name = "post_name")
  private String postName;

  @Column(name = "post_sort")
  private Integer postSort;

  @Column(name = "status")
  private String status;

  /** Reverse relation: Users holding this post (query only, not persisted here). */
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
  private List<SysUserPost> userPosts;
}
