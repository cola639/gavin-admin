package com.api.persistence.repository.system;

import com.api.persistence.domain.system.SysPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** JPA repository for SysPost. */
@Repository
public interface SysPostRepository extends JpaRepository<SysPost, Long> {

  Optional<SysPost> findByPostName(String postName);

  Optional<SysPost> findByPostCode(String postCode);

  /**
   * Returns post IDs for the given user.
   *
   * <p>Equivalent to:
   *
   * <pre>
   *   select p.post_id
   *   from sys_post p
   *   left join sys_user_post up on up.post_id = p.post_id
   *   left join sys_user u on u.user_id = up.user_id
   *   where u.user_id = #{userId}
   * </pre>
   */
  @Query(
      """
              select p.postId
              from SysPost p
                join SysUserPost up on up.post.postId = p.postId
                join SysUser u on u.userId = up.user.userId
              where u.userId = :userId
              """)
  List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
