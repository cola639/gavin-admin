package com.api.system.repository;

import com.api.common.domain.SysDept;
import com.api.framework.annotation.TrackSQLDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysDeptRepository
    extends JpaRepository<SysDept, Long>, JpaSpecificationExecutor<SysDept> {

  boolean existsByDeptNameAndParentId(String deptName, Long parentId);

  long countByParentIdAndDelFlag(Long parentId, String delFlag);

  /** Direct children IDs: where parent_id = :parentId */
  @Query(
      value =
          """
          select d.dept_id
          from sys_dept d
          where d.parent_id = :parentId
        """,
      nativeQuery = true)
  List<Long> findChildDeptIds(@Param("parentId") Long parentId);

  /**
   * Self + all descendants IDs using ancestors path.
   *
   * <p>Example: if parentId=101, it matches rows where ancestors contains "...,101,..."
   */
  @Query(
      value =
          """
          select d.dept_id
          from sys_dept d
          where d.dept_id = :parentId
             or find_in_set(cast(:parentId as char), d.ancestors) > 0
        """,
      nativeQuery = true)
  List<Long> findDeptAndChildrenIds(@Param("parentId") Long parentId);

  /** Test method for @TrackSQLDetail — retrieves all departments using JPQL. */
  @TrackSQLDetail
  @Query("SELECT d FROM SysDept d")
  Page<SysDept> getAllDept(Pageable pageable);
}
