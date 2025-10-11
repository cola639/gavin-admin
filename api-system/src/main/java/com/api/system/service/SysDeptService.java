package com.api.system.service;

import com.api.common.domain.SysDept;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SysDeptService {

  Page<SysDept> selectDeptList(SysDept filter, Pageable pageable);

  SysDept selectDeptById(Long deptId);

  SysDept saveDept(SysDept dept);

  void deleteDept(Long deptId);

  boolean checkDeptNameUnique(String deptName, Long parentId);

  List<SysDept> findChildren(Long parentId);

  long countActiveChildren(Long deptId);
}
