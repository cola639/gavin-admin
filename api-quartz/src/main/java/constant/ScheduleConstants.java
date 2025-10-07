package constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common constants for Quartz job scheduling.
 *
 * <p>This class defines keys and policies used across the scheduling subsystem. It includes
 * identifiers for task properties and standardized misfire policies.
 */
public final class ScheduleConstants {

  /** Key used for storing task class name in Quartz JobDataMap. */
  public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

  /** Key used for storing task properties in Quartz JobDataMap. */
  public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

  /** Default misfire policy. */
  public static final String MISFIRE_DEFAULT = "0";

  /** Immediately trigger execution when a misfire occurs. */
  public static final String MISFIRE_IGNORE_MISFIRES = "1";

  /** Trigger the job once when a misfire occurs. */
  public static final String MISFIRE_FIRE_AND_PROCEED = "2";

  /** Do not trigger the job immediately after a misfire. */
  public static final String MISFIRE_DO_NOTHING = "3";

  /** Enum representing the current status of a scheduled task. */
  @Getter
  @RequiredArgsConstructor
  public enum Status {
    /** The job is active and running as scheduled. */
    NORMAL("0"),

    /** The job is paused and not currently executing. */
    PAUSE("1");

    private final String value;
  }
}
