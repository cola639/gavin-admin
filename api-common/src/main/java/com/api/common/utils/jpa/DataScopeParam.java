package com.api.common.utils.jpa;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Data scope parameters passed into JPQL queries.
 *
 * <p>Important: IN () is invalid in SQL, so always provide a non-empty list for IN params.
 */
@Getter
@Builder
public class DataScopeParam {

  /** Whether to apply dept scope filter. */
  private final boolean applyDeptScope;

  /** Allowed dept ids (maybe empty). */
  private final List<Long> deptIds;

  /** Whether to apply "self only" scope. */
  private final boolean applySelfScope;

  /** Current user id (required if applySelfScope=true). */
  private final Long userId;

  /** Safe dept ids for JPQL "IN" usage. */
  public List<Long> getDeptIdsForIn() {
    if (!applyDeptScope) {
      return Collections.singletonList(-1L);
    }
    if (deptIds == null || deptIds.isEmpty()) {
      return Collections.singletonList(-1L);
    }
    return deptIds;
  }

  /** Safe user id for JPQL "equal" usage. */
  public Long getUserIdForEq() {
    return userId == null ? -1L : userId;
  }
}
