package com.api.persistence.repository;

import com.api.persistence.domain.system.SysRoleDept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleDeptRepository
    extends JpaRepository<SysRoleDept, SysRoleDept.SysRoleDeptId> {

  List<SysRoleDept> findByRoleId(Long roleId);
}
