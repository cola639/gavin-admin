package com.api.framework.aspectj;

import com.api.common.constant.UserConstants;
import com.api.common.domain.SysRole;
import com.api.common.domain.SysUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.StringUtils;
import com.api.framework.annotation.DataScope;
import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/** Aspect for handling data permission filters with JPA Specification. */
@Aspect
@Component
@Slf4j
public class DataScopeAspect {

  public static final String DATA_SCOPE_ALL = "1";
  public static final String DATA_SCOPE_CUSTOM = "2";
  public static final String DATA_SCOPE_DEPT = "3";
  public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
  public static final String DATA_SCOPE_SELF = "5";

  @Before("@annotation(controllerDataScope)")
  public void doBefore(JoinPoint point, DataScope controllerDataScope) {
    handleDataScope(controllerDataScope);
  }

  @After("@annotation(controllerDataScope)")
  public void doAfter(DataScope controllerDataScope) {
    DataScopeContextHolder.clear(); // prevent memory leaks
  }

  private void handleDataScope(DataScope controllerDataScope) {
    SysUser currentUser = SecurityUtils.getLoginUser().getUser();

    if (currentUser == null || currentUser.isAdmin()) {
      DataScopeContextHolder.clear();
      return;
    }

    // 保存权限字符串，而不是 Specification
    String permission =
        StringUtils.defaultIfEmpty(
            controllerDataScope.permission(), DataScopeContextHolder.getContext());

    // 这里你生成 spec 直接用，不要存到 PermissionContextHolder
    Specification<Object> spec = buildSpecification(currentUser, permission);

    log.debug(
        "DataScope applied for userId={}, deptId={}, permission={}",
        currentUser.getUserId(),
        currentUser.getDeptId(),
        permission);
  }

  private Specification<Object> buildSpecification(SysUser user, String permission) {
    return (root, query, cb) -> {
      var predicate = cb.disjunction(); // OR conditions

      List<SysRole> roles = user.getRoles();
      for (SysRole role : roles) {
        if (!StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL)) {
          continue;
        }

        switch (role.getDataScope()) {
          case DATA_SCOPE_ALL -> {
            return cb.conjunction(); // no restrictions
          }
          case DATA_SCOPE_DEPT -> {
            predicate =
                cb.or(
                    predicate,
                    cb.equal(root.join("dept", JoinType.LEFT).get("deptId"), user.getDeptId()));
          }
          case DATA_SCOPE_SELF -> {
            predicate = cb.or(predicate, cb.equal(root.get("userId"), user.getUserId()));
          }
          case DATA_SCOPE_DEPT_AND_CHILD -> {
            // Simplified: only filter by deptId, in real case you'd query child deptIds
            predicate =
                cb.or(
                    predicate,
                    cb.equal(root.join("dept", JoinType.LEFT).get("deptId"), user.getDeptId()));
          }
          case DATA_SCOPE_CUSTOM -> {
            predicate =
                cb.or(
                    predicate,
                    root.join("dept", JoinType.LEFT)
                        .get("deptId")
                        .in(role.getDeptIds())); // assume role.deptIds is available
          }
          default -> {
            predicate = cb.or(predicate, cb.equal(root.get("deptId"), -1)); // block all
          }
        }
      }

      return predicate;
    };
  }
}
