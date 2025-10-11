package com.api.quartz.domain;

import com.api.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents an execution log entry for a scheduled Quartz job.
 *
 * <p>This entity captures details such as job name, execution result, message, start/stop
 * timestamps, and any exceptions thrown during execution.
 *
 * <p>Design principles:
 *
 * <ul>
 *   <li>Uses Lombok to minimize boilerplate.
 *   <li>Uses Jackson for JSON serialization (no Fastjson).
 *   <li>JPA-ready for optional persistence in {@code sys_job_log} table.
 *   <li>Provides human-friendly Excel export via @Excel annotations.
 * </ul>
 *
 * Compatible with Java 17 and Spring Boot 3.5.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_job_log")
public class SysJobLog extends BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Primary key ID. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "job_log_id")
  private Long jobLogId;

  /** Job name. */
  @Column(name = "job_name", length = 128)
  private String jobName;

  /** Job group name. */
  @Column(name = "job_group", length = 64)
  private String jobGroup;

  /** Method or service invoked. */
  @Column(name = "invoke_target", length = 500)
  private String invokeTarget;

  /** Log message or summary of execution. */
  @Column(name = "job_message", length = 2000)
  private String jobMessage;

  /** Execution status (0 = success, 1 = failure). */
  @Column(name = "status", length = 1)
  private String status;

  /** Exception details, if any. */
  @Column(name = "exception_info", length = 4000)
  private String exceptionInfo;

  /** Job start time. */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "start_time")
  private Date startTime;

  /** Job end time. */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "stop_time")
  private Date stopTime;

  /**
   * Calculates the job's execution duration in milliseconds.
   *
   * @return duration (ms) or 0 if timestamps are null
   */
  @Transient
  public long getDurationMillis() {
    if (startTime == null || stopTime == null) {
      return 0L;
    }
    long duration = stopTime.getTime() - startTime.getTime();
    log.debug("Job '{}' execution duration: {} ms", jobName, duration);
    return duration;
  }

  /** Provides a concise log-friendly string representation. */
  @Override
  public String toString() {
    return "SysJobLog(id=%d, jobName='%s', status='%s', start=%s, stop=%s)"
        .formatted(jobLogId, jobName, status, startTime, stopTime);
  }
}
