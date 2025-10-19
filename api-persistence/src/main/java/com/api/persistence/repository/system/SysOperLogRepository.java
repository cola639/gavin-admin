package com.api.persistence.repository.system;

import com.api.persistence.domain.system.SysOperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for SysOperLog.
 *
 * <p>Provides CRUD and query capabilities via Spring Data JPA.
 */
@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {

  /** Deletes all operation logs. */
  void deleteAll();
}
