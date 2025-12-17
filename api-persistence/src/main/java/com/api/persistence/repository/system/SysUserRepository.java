package com.api.persistence.repository.system;

import com.api.common.domain.SysUser;
import com.api.framework.annotation.TrackSQLDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository
    extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {
  Optional<SysUser> findByUserId(Long userId);

  Optional<SysUser> findByUserNameAndDelFlag(String userName, String delFlag);

  Optional<SysUser> findByEmail(String email);

  Optional<SysUser> findByPhonenumberAndDelFlag(String phonenumber, String delFlag);

  Optional<SysUser> findByEmailAndDelFlag(String email, String delFlag);

  @TrackSQLDetail
  @Modifying
  @Query("update SysUser u set u.delFlag = :delFlag where u.userId in :userIds")
  int softDeleteUsers(@Param("userIds") List<Long> userIds, @Param("delFlag") String delFlag);

  boolean existsByUserName(String userName);

  boolean existsByPhonenumber(String phonenumber);

  boolean existsByEmail(String email);

  @Query(
      """
          select distinct u
          from SysUser u
          left join u.dept d
          left join SysUserRole ur on ur.userId = u.userId
          left join SysRole r on r.roleId = ur.roleId
          where u.delFlag = :delFlag
            and r.roleId = :roleId
            and (:userName is null or :userName = '' or lower(u.userName) like lower(concat('%', :userName, '%')))
            and (:phonenumber is null or :phonenumber = '' or u.phonenumber like concat('%', :phonenumber, '%'))
            and (:applyScope = false or u.deptId in :deptIds)
        """)
  Page<SysUser> findAllocatedUsers(
      @Param("roleId") Long roleId,
      @Param("delFlag") String delFlag,
      @Param("userName") String userName,
      @Param("phonenumber") String phonenumber,
      @Param("applyScope") boolean applyScope,
      @Param("deptIds") List<Long> deptIds,
      Pageable pageable);

  @Query(
      """
                select distinct u
                from SysUser u
                left join u.dept d
                where u.delFlag = :delFlag
                  and (:userName is null or :userName = '' or lower(u.userName) like lower(concat('%', :userName, '%')))
                  and (:phonenumber is null or :phonenumber = '' or u.phonenumber like concat('%', :phonenumber, '%'))
                  and (:applyScope = false or u.deptId in :deptIds)
                  and not exists (
                    select 1
                    from SysUserRole ur
                    where ur.userId = u.userId
                      and ur.roleId = :roleId
                  )
              """)
  Page<SysUser> findUnallocatedUsers(
      @Param("roleId") Long roleId,
      @Param("delFlag") String delFlag,
      @Param("userName") String userName,
      @Param("phonenumber") String phonenumber,
      @Param("applyScope") boolean applyScope,
      @Param("deptIds") List<Long> deptIds,
      Pageable pageable);
}
