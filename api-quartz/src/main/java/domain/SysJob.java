package domain;

import com.api.common.utils.StringUtils;
import com.api.persistence.domain.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import constant.ScheduleConstants;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import util.CronUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a scheduled Quartz job definition.
 *
 * <p>This entity stores configuration details for scheduled tasks, including job name, group, cron
 * expression, misfire policy, and concurrency settings.
 *
 * <p>Design principles:
 *
 * <ul>
 *   <li>Uses Lombok for concise boilerplate-free design.
 *   <li>Uses Jackson for JSON serialization.
 *   <li>Supports JPA entity mapping (but can also operate as a plain POJO).
 *   <li>Provides dynamic computation for next valid execution time.
 * </ul>
 *
 * Compatible with Java 17 and Spring Boot 3.5.
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_job")
public class SysJob extends BaseEntity implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** Primary key ID. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "job_id")
  private Long jobId;

  /** Job name. */
  @NotBlank(message = "Job name cannot be empty")
  @Size(max = 64, message = "Job name must not exceed 64 characters")
  @Column(name = "job_name", length = 64, nullable = false)
  private String jobName;

  /** Job group. */
  @Column(name = "job_group", length = 64)
  private String jobGroup;

  /** Method or service to invoke. */
  @NotBlank(message = "Invoke target cannot be empty")
  @Size(max = 500, message = "Invoke target must not exceed 500 characters")
  @Column(name = "invoke_target", length = 500, nullable = false)
  private String invokeTarget;

  /** Cron expression for execution schedule. */
  @NotBlank(message = "Cron expression cannot be empty")
  @Size(max = 255, message = "Cron expression must not exceed 255 characters")
  @Column(name = "cron_expression", length = 255, nullable = false)
  private String cronExpression;

  /** Policy when cron misfires (default = 0). */
  @Column(name = "misfire_policy", length = 10)
  private String misfirePolicy = ScheduleConstants.MISFIRE_DEFAULT;

  /** Whether concurrent execution is allowed (0=allowed, 1=forbidden). */
  @Column(name = "concurrent", length = 1)
  private String concurrent;

  /** Job status (0=normal, 1=paused). */
  @Column(name = "status", length = 1)
  private String status;

  /**
   * Dynamically compute the next valid execution time from the cron expression.
   *
   * @return the next execution {@link Date}, or null if invalid or missing cron expression.
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Transient
  public Date getNextValidTime() {
    if (StringUtils.isEmpty(cronExpression)) {
      log.warn("Cron expression is empty for job: {}", jobName);
      return null;
    }
    try {
      return CronUtils.getNextExecution(cronExpression);
    } catch (Exception ex) {
      log.error("Failed to parse cron expression '{}' for job: {}", cronExpression, jobName, ex);
      return null;
    }
  }

  /** Ignores system-level internal attributes when serializing to JSON. */
  @Override
  @JsonIgnore
  public String toString() {
    return "SysJob(jobId=%d, jobName='%s', group='%s', cron='%s', status='%s')"
        .formatted(jobId, jobName, jobGroup, cronExpression, status);
  }
}
