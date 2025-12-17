package com.api.system.service;

import com.api.common.domain.SysUser;
import com.api.common.domain.SysUserDTO;
import com.api.common.domain.SysUserMapper;
import com.api.common.enums.DelFlagEnum;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.persistence.repository.system.SysUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  public Page<SysUserDTO> selectUserList(
      SysUserDTO user, Map<String, Object> params, Pageable pageable) {

    Date beginTime = (Date) params.get("beginTime");
    Date endTime = (Date) params.get("endTime");

    log.debug("Selecting user list, filter user={}, params={}", user, params);

    Specification<SysUser> spec =
        SpecificationBuilder.<SysUser>builder()
            .eq("delFlag", DelFlagEnum.NORMAL.getCode())
            .eq("deptId", user.getDeptId())
            .eq("userId", user.getUserId())
            .like("userName", user.getUserName())
            .eq("status", user.getStatus())
            .like("phonenumber", user.getPhonenumber())
            .between("createTime", beginTime, endTime);

    if (pageable == null || pageable.isUnpaged()) {
      List<SysUser> entities = userRepository.findAll(spec);
      List<SysUserDTO> dtoList = SysUserDTO.fromEntities(entities);
      return new PageImpl<>(dtoList, Pageable.unpaged(), dtoList.size());
    }

    Page<SysUser> entityPage = userRepository.findAll(spec, pageable);
    List<SysUserDTO> dtoList = SysUserDTO.fromEntities(entityPage.getContent());

    return new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
  }

  public SysUser selectUserById(Long userId) {
    return userRepository.findById(userId).orElse(null);
  }

  public SysUser selectUserByUserName(String userName) {
    return userRepository
        .findByUserNameAndDelFlag(userName, DelFlagEnum.NORMAL.getCode())
        .orElse(null);
  }

  public SysUser insertUser(SysUser user) {
    return userRepository.save(user);
  }

  public void deleteUserById(Long userId) {
    userRepository.deleteById(userId);
  }

  @Transactional
  public void createUser(SysUser user) {
    String userName = generateUniqueUserNameFromNickName(user.getNickName());
    user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
    user.setUserName(userName);

    if (userRepository.existsByUserName(user.getUserName())) {
      throw new ServiceException("Failed to create user username already exists.");
    }

    if (StringUtils.isNotEmpty(user.getPhonenumber())
        && userRepository.existsByPhonenumber(user.getPhonenumber())) {
      throw new ServiceException("Failed to create user phone number is already in use.");
    }

    if (StringUtils.isNotEmpty(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
      throw new ServiceException("Failed to create user email address is already in use.");
    }

    // Save user info
    SysUser savedUser = userRepository.save(user);
    log.info("Inserted user with id={}", savedUser.getUserId());

    // Save user-post associations
    userPostService.insertUserPost(savedUser);

    // Save user-role associations
    userRoleService.insertUserRole(savedUser.getUserId(), savedUser.getRoleIds());
  }

  private final SysUserMapper sysUserMapper; // MapStruct mapper

  @Transactional
  public SysUser updateUser(SysUserDTO req) {
    SysUser existing = loadExisting(req.getUserId());

    validateUniqueness(req, existing);

    // copy non-null DTO fields into entity
    sysUserMapper.updateFromDto(req, existing);

    updateRelations(req, existing);

    log.info("Updating user id={}", existing.getUserId());
    return userRepository.save(existing);
  }

  private SysUser loadExisting(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ServiceException("User not found"));
  }

  private void validateUniqueness(SysUserDTO req, SysUser existing) {
    if (StringUtils.isNotBlank(req.getEmail())
        && !req.getEmail().equals(existing.getEmail())
        && userRepository.existsByEmail(req.getEmail())) {
      throw new ServiceException("Update user failed, email already exists");
    }

    if (StringUtils.isNotBlank(req.getPhonenumber())
        && !req.getPhonenumber().equals(existing.getPhonenumber())
        && userRepository.existsByPhonenumber(req.getPhonenumber())) {
      throw new ServiceException("Update user failed, phone already exists");
    }
  }

  private void updateRelations(SysUserDTO req, SysUser existing) {
    Long userId = existing.getUserId();

    if (req.getRoleIds() != null) {
      userRoleService.deleteByUserId(userId);
      userRoleService.insertUserRole(userId, req.getRoleIds());
    }

    if (req.getPostIds() != null) {
      userPostService.deleteByUserId(userId);
      userPostService.insertUserPost(existing);
    }
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
    return userRepository.softDeleteUsers(ids, DelFlagEnum.DELETED.getCode());
  }

  /**
   * Generate a unique username based on nickname. Examples: nickName "Gavin Li" -> "gavin.li" if
   * gavin. Li exists -> "gavin.li_1" if gavin.li_1 exists -> "gavin.li_2"
   */
  private String generateUniqueUserNameFromNickName(String nickName) {
    String base = buildBaseUserName(nickName);

    // We need space for: "_" + 5 digits
    int maxBaseLength = 30 - 1 - 5;
    if (base.length() > maxBaseLength) {
      base = base.substring(0, maxBaseLength);
    }

    int suffix = 1;
    String candidate;

    // Always use nickname_00001 style
    do {
      candidate = String.format("%s_%05d", base, suffix);
      suffix++;
    } while (userRepository.existsByUserName(candidate));

    log.debug("Generated unique username '{}' for nickname '{}'", candidate, nickName);
    return candidate;
  }

  private String buildBaseUserName(String nickName) {
    if (nickName == null) {
      return "user";
    }
    // Normalize nickname
    String normalized = nickName.trim().toLowerCase();
    // Replace non letter/digit characters with dot
    normalized = normalized.replaceAll("[^a-z0-9]+", ".");
    // Remove leading/trailing dots
    normalized = normalized.replaceAll("^\\.+", "").replaceAll("\\.+$", "");

    if (normalized.isEmpty()) {
      normalized = "user";
    }

    // Do NOT cut to 30 here anymore, we handle exact length in generateUniqueUserNameFromNickName
    log.debug("Built base username '{}' from nickname '{}'", normalized, nickName);
    return normalized;
  }
}
