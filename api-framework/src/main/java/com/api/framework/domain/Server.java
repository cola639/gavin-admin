package com.api.framework.domain;

import com.api.common.utils.Arith;
import com.api.common.utils.ip.IpUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Represents the server monitoring information.
 *
 * <p>This class aggregates system-level metrics including CPU, memory, JVM, operating system, and
 * disk information using the OSHI library.
 *
 * <p>Design principles: - Uses Lombok for clean structure. - Uses Jackson for JSON serialization. -
 * Uses @Slf4j for monitoring and debugging. - No persistence (transient runtime data). - Compatible
 * with JDK 8.
 */
@Slf4j
@Data
public class Server implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final int OSHI_WAIT_SECOND = 1000;

  /** CPU information. */
  private Cpu cpu = new Cpu();

  /** Memory information. */
  private Mem mem = new Mem();

  /** JVM information. */
  private Jvm jvm = new Jvm();

  /** System information. */
  private Sys sys = new Sys();

  /** Disk information. */
  @JsonProperty("disks")
  private List<SysFile> sysFiles = new LinkedList<>();

  /** Collects all system information into this object. */
  public void collectSystemInfo() {
    try {
      SystemInfo si = new SystemInfo();
      HardwareAbstractionLayer hal = si.getHardware();

      setCpuInfo(hal.getProcessor());
      setMemInfo(hal.getMemory());
      setSysInfo();
      setJvmInfo();
      setSysFiles(si.getOperatingSystem());

      log.info("Server monitoring data collected successfully.");
    } catch (Exception e) {
      log.error("Error while collecting server monitoring information", e);
    }
  }

  /** Populate CPU information. */
  private void setCpuInfo(CentralProcessor processor) {
    long[] prevTicks = processor.getSystemCpuLoadTicks();
    Util.sleep(OSHI_WAIT_SECOND);
    long[] ticks = processor.getSystemCpuLoadTicks();

    long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
    long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
    long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
    long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
    long sysTime = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
    long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
    long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
    long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];

    long totalCpu = user + nice + sysTime + idle + iowait + irq + softirq + steal;

    cpu.setCpuNum(processor.getLogicalProcessorCount());
    cpu.setTotal((double) totalCpu);
    cpu.setSys((double) sysTime);
    cpu.setUsed((double) user);
    cpu.setWait((double) iowait);
    cpu.setFree((double) idle);

    log.debug("CPU data collected: {}", cpu);
  }

  /** Populate memory information. */
  private void setMemInfo(GlobalMemory memory) {
    mem.setTotal(memory.getTotal());
    mem.setUsed(memory.getTotal() - memory.getAvailable());
    mem.setFree(memory.getAvailable());
    log.debug("Memory data collected: {}", mem);
  }

  /** Populate system information. */
  private void setSysInfo() {
    Properties props = System.getProperties();
    sys.setComputerName(IpUtils.getHostName());
    sys.setComputerIp(IpUtils.getHostIp());
    sys.setOsName(props.getProperty("os.name"));
    sys.setOsArch(props.getProperty("os.arch"));
    sys.setUserDir(props.getProperty("user.dir"));
    log.debug("System data collected: {}", sys);
  }

  /** Populate JVM information. */
  private void setJvmInfo() throws UnknownHostException {
    Properties props = System.getProperties();
    jvm.setTotal(Runtime.getRuntime().totalMemory());
    jvm.setMax(Runtime.getRuntime().maxMemory());
    jvm.setFree(Runtime.getRuntime().freeMemory());
    jvm.setVersion(props.getProperty("java.version"));
    jvm.setHome(props.getProperty("java.home"));
    log.debug("JVM data collected: {}", jvm);
  }

  /** Populate disk information. */
  private void setSysFiles(OperatingSystem os) {
    FileSystem fileSystem = os.getFileSystem();
    List<OSFileStore> fsArray = fileSystem.getFileStores();

    sysFiles.clear();
    for (OSFileStore fs : fsArray) {
      long free = fs.getUsableSpace();
      long total = fs.getTotalSpace();
      long used = total - free;

      SysFile sysFile = new SysFile();
      sysFile.setDirName(fs.getMount());
      sysFile.setSysTypeName(fs.getType());
      sysFile.setTypeName(fs.getName());
      sysFile.setTotal(formatFileSize(total));
      sysFile.setFree(formatFileSize(free));
      sysFile.setUsed(formatFileSize(used));
      sysFile.setUsage(Arith.mul(Arith.div(used, total, 4), 100));

      sysFiles.add(sysFile);
    }

    log.debug("Disk data collected: {} entries", sysFiles.size());
  }

  /** Converts a file size in bytes to a readable format (GB, MB, KB, B). */
  private String formatFileSize(long size) {
    long kb = 1024;
    long mb = kb * 1024;
    long gb = mb * 1024;

    if (size >= gb) {
      return String.format("%.1f GB", (float) size / gb);
    } else if (size >= mb) {
      float f = (float) size / mb;
      return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
    } else if (size >= kb) {
      float f = (float) size / kb;
      return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
    } else {
      return String.format("%d B", size);
    }
  }
}
