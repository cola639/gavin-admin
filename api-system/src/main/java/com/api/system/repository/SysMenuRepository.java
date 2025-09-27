package com.api.system.repository;

import com.api.system.domain.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysMenuRepository
    extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {}
