package com.api.system.service;

import com.api.persistence.domain.system.SysOperLog;
import com.api.persistence.repository.SysOperLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Implementation of operation log service using JPA. */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogService {

  private final SysOperLogRepository repository;

  /** Insert a new operation log record. */
  @Transactional
  public void insertOperLog(SysOperLog operLog) {
    repository.save(operLog);
    log.info("Inserted operation log: {}", operLog.getTitle());
  }

  /**
   * Retrieve all operation logs matching given filter. (Note: extend later with Specification for
   * dynamic filtering)
   */
  public List<SysOperLog> list(SysOperLog filter) {
    log.debug("Fetching operation logs with filter: {}", filter);
    return repository.findAll();
  }

  /** Delete multiple logs by their IDs. */
  @Transactional
  public int deleteByIds(Long[] operIds) {
    for (Long id : operIds) {
      repository.deleteById(id);
    }
    log.info("Deleted operation logs with IDs: {}", (Object) operIds);
    return operIds.length;
  }

  /** Find a log record by ID. */
  public SysOperLog getById(Long operId) {
    return repository.findById(operId).orElse(null);
  }

  /** Clear all operation logs. */
  @Transactional
  public void clean() {
    repository.deleteAll();
    log.warn("Cleared all operation logs.");
  }
}
