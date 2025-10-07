package util;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/** Utility class for validating and parsing Cron expressions. */
@Slf4j
public final class CronUtils {

  private CronUtils() {}

  /** Checks if the given cron expression is valid. */
  public static boolean isValid(String cronExpression) {
    return CronExpression.isValidExpression(cronExpression);
  }

  /** Returns an error message if the cron expression is invalid. */
  public static String getInvalidMessage(String cronExpression) {
    try {
      new CronExpression(cronExpression);
      return null;
    } catch (ParseException e) {
      log.warn("Invalid cron expression '{}': {}", cronExpression, e.getMessage());
      return e.getMessage();
    }
  }

  /** Returns the next valid execution time for the given cron expression. */
  public static Date getNextExecution(String cronExpression) {
    try {
      CronExpression cron = new CronExpression(cronExpression);
      return cron.getNextValidTimeAfter(new Date());
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid cron expression: " + e.getMessage(), e);
    }
  }
}
