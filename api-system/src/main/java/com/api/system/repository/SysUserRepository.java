package com.api.system.repository;

import com.api.common.domain.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository
    extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {
  Optional<SysUser> findByUserId(Long userId);

  Optional<SysUser> findByUserNameAndDelFlag(String userName, String delFlag);

  Optional<SysUser> findByPhonenumberAndDelFlag(String phonenumber, String delFlag);

  Optional<SysUser> findByEmailAndDelFlag(String email, String delFlag);

  @Modifying
  @Query("update SysUser u set u.delFlag = '2' where u.userId in :userIds")
  int softDeleteUsers(@Param("userIds") List<Long> userIds);

  boolean existsByUserName(String userName);

  boolean existsByPhonenumber(String phonenumber);

  boolean existsByEmail(String email);
}
