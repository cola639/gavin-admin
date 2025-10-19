package com.api.persistence.repository.system;

import com.api.common.domain.SysDept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SysDeptRepository
    extends JpaRepository<SysDept, Long>, JpaSpecificationExecutor<SysDept> {

  boolean existsByDeptNameAndParentId(String deptName, Long parentId);

  long countByParentIdAndDelFlag(Long parentId, String delFlag);

  /** Test method for @TrackSQLDetail — retrieves all departments using JPQL. */
  @Query("SELECT d FROM SysDept d")
  Page<SysDept> getAllDept(Pageable pageable);
}
