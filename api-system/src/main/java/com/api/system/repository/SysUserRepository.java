package com.api.system.repository;

import com.api.common.domain.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {
    Optional<SysUser> findByUserId(Long userId);

    Optional<SysUser> findByUserNameAndDelFlag(String userName, String delFlag);

    Optional<SysUser> findByPhonenumberAndDelFlag(String phonenumber, String delFlag);

    Optional<SysUser> findByEmailAndDelFlag(String email, String delFlag);

    boolean existsByUserName(String userName);

    boolean existsByPhonenumber(String phonenumber);

    boolean existsByEmail(String email);
}
