package com.api.system.repository;

import com.api.system.domain.SysUserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SysUserPost entity.
 * Provides data access methods for managing user-post associations.
 */
@Repository
public interface SysUserPostRepository extends JpaRepository<SysUserPost, Long>, JpaSpecificationExecutor<SysUserPost> {

    /**
     * Delete user-post associations by userId.
     * <p>
     * Equivalent to:
     * delete from sys_user_post where user_id = ?
     *
     * @param userId user id
     */
    void deleteByUser_UserId(Long userId);

    /**
     * Count user-post associations by postId.
     * <p>
     * Equivalent to:
     * select count(1) from sys_user_post where post_id = ?
     *
     * @param postId post id
     * @return count
     */
    long countByPost_PostId(Long postId);

    /**
     * Delete user-post associations by a list of userIds.
     * <p>
     * Equivalent to:
     * delete from sys_user_post where user_id in (...)
     *
     * @param userIds list of user ids
     */
    void deleteByUser_UserIdIn(List<Long> userIds);

    /**
     * Save multiple user-post associations.
     * (Replaces MyBatis batchUserPost).
     *
     * @param userPosts list of associations
     * @return saved associations
     */
    @SuppressWarnings("unchecked")
    <S extends SysUserPost> List<S> saveAll(Iterable<S> userPosts);

}
