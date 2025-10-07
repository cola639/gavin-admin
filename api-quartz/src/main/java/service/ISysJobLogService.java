package service;

import domain.SysJobLog;

import java.util.List;

/**
 * Service interface for managing Quartz job execution logs.
 *
 * @author Gavin
 */
public interface ISysJobLogService {

  /**
   * Retrieves execution logs matching the given criteria.
   *
   * @param jobLog filter conditions
   * @return list of job execution logs
   */
  List<SysJobLog> selectJobLogList(SysJobLog jobLog);

  /**
   * Retrieves a job log by ID.
   *
   * @param jobLogId log ID
   * @return log details, or {@code null} if not found
   */
  SysJobLog selectJobLogById(Long jobLogId);

  /**
   * Adds a new job execution log.
   *
   * @param jobLog log entry to add
   */
  void addJobLog(SysJobLog jobLog);

  /**
   * Deletes multiple logs by ID array.
   *
   * @param logIds IDs of logs to delete
   * @return number of deleted records
   */
  int deleteJobLogByIds(Long[] logIds);

  /**
   * Deletes a single log by ID.
   *
   * @param jobId log ID
   * @return delete result (1 = success)
   */
  int deleteJobLogById(Long jobId);

  /** Clears all job logs from the system. */
  void cleanJobLog();
}
