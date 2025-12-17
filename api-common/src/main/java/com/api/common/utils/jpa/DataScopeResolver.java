package com.api.common.utils.jpa;

import com.api.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Resolve data scope for current user.
 *
 * <p>Enterprise rule: scope decisions belong to backend, never from frontend params.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataScopeResolver {

  public DataScopeParam resolveCurrentUserScope() {
    Long userId = SecurityUtils.getUserId();
    Long deptId = SecurityUtils.getDeptId();

    if (userId == null) {
      log.warn("No userId in context, applying empty scope for safety.");
      return DataScopeParam.builder()
          .applyDeptScope(true)
          .deptIds(Collections.emptyList())
          .applySelfScope(false)
          .userId(null)
          .build();
    }

    if (SecurityUtils.isAdmin(userId)) {
      return DataScopeParam.builder()
          .applyDeptScope(false)
          .deptIds(Collections.emptyList())
          .applySelfScope(false)
          .userId(userId)
          .build();
    }

    // Example: DEPT_AND_CHILD scope
    List<Long> deptIds = Collections.emptyList();
    // Example: DEPT_AND_CHILD scope
    //  List<Long> deptIds = (deptId == null) ? Collections.emptyList() :
    // deptService.findDeptAndChildrenIds(deptId);

    return DataScopeParam.builder()
        .applyDeptScope(true)
        .deptIds(deptIds)
        .applySelfScope(false)
        .userId(userId)
        .build();
  }
}
