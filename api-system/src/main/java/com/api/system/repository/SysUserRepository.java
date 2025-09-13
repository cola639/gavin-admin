package com.api.system.repository;

import com.api.common.domain.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SysUserRepository
        extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUserNameAndDelFlag(String userName, String delFlag);

    boolean existsByUserNameAndDelFlag(String userName, String delFlag);

    boolean existsByPhonenumberAndDelFlag(String phonenumber, String delFlag);

    boolean existsByEmailAndDelFlag(String email, String delFlag);
}
