package com.api.system.service;

import com.api.common.domain.SysUser;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.persistence.repository.SysUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

  private final SysUserPostService userPostService;
  private final SysUserRoleService userRoleService;
  private final SysUserRepository userRepository;

  public Page<SysUser> selectUserList(SysUser user, Map<String, Object> params, Pageable pageable) {
    Specification<SysUser> spec =
        SpecificationBuilder.<SysUser>builder()
            .eq("delFlag", "0")
            .eq("userId", user.getUserId())
            .like("userName", user.getUserName())
            .eq("status", user.getStatus())
            .like("phonenumber", user.getPhonenumber())
            .between("createTime", (Date) params.get("beginTime"), (Date) params.get("endTime"));

    return userRepository.findAll(spec, pageable);
  }

  public SysUser selectUserById(Long userId) {
    return userRepository.findById(userId).orElse(null);
  }

  public SysUser selectUserByUserName(String userName) {
    return userRepository.findByUserNameAndDelFlag(userName, "0").orElse(null);
  }

  public SysUser insertUser(SysUser user) {
    return userRepository.save(user);
  }

  public void deleteUserById(Long userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  public void createUser(SysUser user) {
    if (userRepository.existsByUserName(user.getUserName())) {
      throw new ServiceException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
    }
    if (StringUtils.isNotEmpty(user.getPhonenumber())
        && userRepository.existsByPhonenumber(user.getPhonenumber())) {
      throw new ServiceException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
    }
    if (StringUtils.isNotEmpty(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
      throw new ServiceException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
    }

    // Save user info
    SysUser savedUser = userRepository.save(user);
    log.info("Inserted user with id={}", savedUser.getUserId());

    // Save user-post associations
    userPostService.insertUserPost(savedUser);

    // Save user-role associations
    userRoleService.insertUserRole(savedUser.getUserId(), savedUser.getRoleIds());
  }

  @Transactional
  public SysUser updateUser(SysUser user) {
    Optional<SysUser> info = userRepository.findById(user.getUserId());

    if (info.isPresent()) {
      SysUser existingUser = info.get(); // unwrap to real SysUser
      if (!user.getUserId().equals(existingUser.getUserId())
          && userRepository.existsByUserName(user.getUserName())) {
        throw new ServiceException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
      }
      if (!user.getUserId().equals(existingUser.getUserId())
          && StringUtils.isNotEmpty(existingUser.getPhonenumber())
          && userRepository.existsByPhonenumber(user.getPhonenumber())) {
        throw new ServiceException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
      }
      if (!user.getUserId().equals(existingUser.getUserId())
          && StringUtils.isNotEmpty(user.getEmail())
          && userRepository.existsByEmail(user.getEmail())) {
        throw new ServiceException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
      }
    }

    Long userId = user.getUserId();
    userRoleService.deleteByUserId(userId);
    userPostService.deleteByUserId(userId);
    userPostService.insertUserPost(user);
    userRoleService.insertUserRole(user.getUserId(), user.getRoleIds());

    // ensure JPA does not touch immutable roles
    user.setRoles(null);
    return userRepository.save(user);
  }

  @Transactional
  public int deleteUserByIds(Long[] userIds) {
    List<Long> ids = Arrays.asList(userIds);

    // Pre-checks
    //        for (Long userId : ids) {
    //            checkUserAllowed(new SysUser(userId));
    //            checkUserDataScope(userId);
    //        }

    // Delete associations
    userRoleService.deleteByUserIds(ids);
    userPostService.deleteByUser_UserIdIn(ids);

    // Soft delete users
    return userRepository.softDeleteUsers(ids);
  }
}
