package com.api.framework.domain.server;

import com.api.common.utils.Arith;
import com.api.common.utils.DateEnhancedUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.management.ManagementFactory;

/**
 * Represents JVM runtime information.
 *
 * <p>This class provides real-time insights into the Java Virtual Machine, including memory usage,
 * runtime duration, startup time, JDK version, and JVM arguments.
 *
 * <p>Design principles: - Uses Lombok for simplicity. - Uses Jackson for JSON serialization. -
 * Uses @Slf4j for logging and diagnostics. - Does not persist to any database. - Compatible with
 * JDK 8.
 */
@Slf4j
@Data
public class Jvm implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Current total JVM memory usage (in bytes, internal use only)). */
  @JsonIgnore private double total;

  /** Maximum available JVM memory (in bytes, internal use only)). */
  @JsonIgnore private double max;

  /** Current free JVM memory (in bytes, internal use only)). */
  @JsonIgnore private double free;

  /** JDK version. */
  private String version;

  /** JDK installation path. */
  private String home;

  /** JVM name (e.g., Java HotSpot(TM) 64-Bit Server VM). */
  @JsonProperty("jvmName")
  public String getName() {
    return ManagementFactory.getRuntimeMXBean().getVmName();
  }

  /** Total JVM memory (in MB). */
  @JsonProperty("totalMemoryMB")
  public double getTotalMB() {
    double value = Arith.div(total, (1024 * 1024), 2);
    log.debug("Calculated total JVM memory: {} MB", value);
    return value;
  }

  /** Maximum JVM memory (in MB). */
  @JsonProperty("maxMemoryMB")
  public double getMaxMB() {
    double value = Arith.div(max, (1024 * 1024), 2);
    log.debug("Calculated max JVM memory: {} MB", value);
    return value;
  }

  /** Free JVM memory (in MB). */
  @JsonProperty("freeMemoryMB")
  public double getFreeMB() {
    double value = Arith.div(free, (1024 * 1024), 2);
    log.debug("Calculated free JVM memory: {} MB", value);
    return value;
  }

  /** Used JVM memory (in MB). */
  @JsonProperty("usedMemoryMB")
  public double getUsedMB() {
    double value = Arith.div(total - free, (1024 * 1024), 2);
    log.debug("Calculated used JVM memory: {} MB", value);
    return value;
  }

  /** JVM memory usage percentage. */
  @JsonProperty("usagePercent")
  public double getUsagePercent() {
    if (total == 0) {
      log.warn("JVM total memory is zero, returning 0 usage.");
      return 0D;
    }
    double value = Arith.mul(Arith.div(total - free, total, 4), 100);
    log.debug("Calculated JVM memory usage: {}%", value);
    return value;
  }

  /** JVM startup time (formatted). */
  @JsonProperty("startTime")
  public String getStartTime() {
    return DateEnhancedUtil.parseDateToStr(
        DateEnhancedUtil.YYYY_MM_DD_HH_MM_SS, DateEnhancedUtil.getServerStartDate());
  }

  /** JVM running time duration. */
  @JsonProperty("runTime")
  public String getRunTime() {
    return DateEnhancedUtil.timeDistance(
        DateEnhancedUtil.getNowDate(), DateEnhancedUtil.getServerStartDate());
  }

  /** JVM input arguments (e.g., startup options). */
  @JsonProperty("inputArgs")
  public String getInputArgs() {
    return ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
  }
}
