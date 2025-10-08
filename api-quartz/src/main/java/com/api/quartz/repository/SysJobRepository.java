package com.api.quartz.repository;

import com.api.quartz.domain.SysJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysJobRepository extends JpaRepository<SysJob, Long>, JpaSpecificationExecutor<SysJob> {}
