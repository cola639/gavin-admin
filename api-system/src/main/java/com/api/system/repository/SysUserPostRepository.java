package com.api.system.repository;

import com.api.system.domain.SysUserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SysUserPost entity.
 */
@Repository
public interface SysUserPostRepository extends JpaRepository<SysUserPost, Long> {

    // Correct method names with property navigation
    List<SysUserPost> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
}
