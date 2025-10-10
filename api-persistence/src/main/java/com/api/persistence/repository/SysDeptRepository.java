package com.api.persistence.repository;

import com.api.persistence.domain.common.SysDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysDeptRepository
    extends JpaRepository<SysDept, Long>, JpaSpecificationExecutor<SysDept> {

  boolean existsByDeptNameAndParentId(String deptName, Long parentId);

  long countByParentIdAndDelFlag(Long parentId, String delFlag);
}
