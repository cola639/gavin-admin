package com.api.persistence.repository;

import com.api.persistence.domain.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysMenuRepository
    extends JpaRepository<SysMenu, Long>, JpaSpecificationExecutor<SysMenu> {}
