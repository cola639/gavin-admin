package com.api.persistence.domain;

import com.api.persistence.domain.common.BaseEntity;
import com.api.persistence.domain.common.SysUser;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "sys_user_post")
@IdClass(SysUserPost.SysUserPostId.class)
public class SysUserPost extends BaseEntity {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private SysUser user;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private SysPost post;

  /** Composite primary key for SysUserPost */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SysUserPostId implements java.io.Serializable {
    private Long user;
    private Long post;
  }
}
