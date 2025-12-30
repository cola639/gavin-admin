package com.api.system.domain.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an operation log record.
 *
 * <p>Maps directly to the sys_oper_log database table.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "sys_oper_log")
public class SysOperLog {

  /** Primary key */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "oper_id")
  private Long operId;

  /** Operation module title */
  @Column(name = "title", length = 255)
  private String title;

  /** Business type (0=Other, 1=Insert, 2=Update, 3=Delete, etc.) */
  @Column(name = "business_type")
  private Integer businessType;

  /** Request method name */
  @Column(name = "method", length = 500)
  private String method;

  /** HTTP request method (GET, POST, etc.) */
  @Column(name = "request_method", length = 10)
  private String requestMethod;

  /** Operator category (0=Other, 1=Admin, 2=Mobile) */
  @Column(name = "operator_type")
  private Integer operatorType;

  /** Operator name */
  @Column(name = "oper_name", length = 100)
  private String operName;

  /** Department name */
  @Column(name = "dept_name", length = 100)
  private String deptName;

  /** Request URL */
  @Column(name = "oper_url", length = 255)
  private String operUrl;

  /** Operation IP address */
  @Column(name = "oper_ip", length = 50)
  private String operIp;

  /** Operation location */
  @Column(name = "oper_location", length = 255)
  private String operLocation;

  /** Request parameters */
  @Lob
  @Column(name = "oper_param", columnDefinition = "MEDIUMTEXT")
  private String operParam;

  /** Response JSON */
  @Lob
  @Column(name = "json_result")
  private String jsonResult;

  /** Operation status (0=Success, 1=Error) */
  @Column(name = "status")
  private Integer status;

  /** Error message */
  @Column(name = "error_msg", length = 2000)
  private String errorMsg;

  /** Operation timestamp */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(name = "oper_time")
  private LocalDateTime operTime;

  /** Execution time cost (milliseconds) */
  @Column(name = "cost_time")
  private Long costTime;
}
