package com.api.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

/**
 * Utility class for common date and time operations.
 *
 * <p>Provides helpers for: - Formatting and parsing dates - Calculating time differences -
 * Converting between {@link Date}, {@link LocalDate}, and {@link LocalDateTime} - Retrieving
 * system/server start time
 */
@Slf4j
public final class DateEnhancedUtil extends org.apache.commons.lang3.time.DateUtils {

  public static final String YYYY = "yyyy";
  public static final String YYYY_MM = "yyyy-MM";
  public static final String YYYY_MM_DD = "yyyy-MM-dd";
  public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
  public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

  private static final String[] PARSE_PATTERNS = {
    "yyyy-MM-dd",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm",
    "yyyy-MM",
    "yyyy/MM/dd",
    "yyyy/MM/dd HH:mm:ss",
    "yyyy/MM/dd HH:mm",
    "yyyy/MM",
    "yyyy.MM.dd",
    "yyyy.MM.dd HH:mm:ss",
    "yyyy.MM.dd HH:mm",
    "yyyy.MM"
  };

  /**
   * @return current date
   */
  public static Date getNowDate() {
    return new Date();
  }

  /**
   * @return current date as string in yyyy-MM-dd format
   */
  public static String getDate() {
    return dateTimeNow(YYYY_MM_DD);
  }

  /**
   * @return current date-time as string in yyyy-MM-dd HH:mm:ss format
   */
  public static String getTime() {
    return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
  }

  /**
   * @return current date-time as string in yyyyMMddHHmmss format
   */
  public static String dateTimeNow() {
    return dateTimeNow(YYYYMMDDHHMMSS);
  }

  /** Format current date-time with custom format */
  public static String dateTimeNow(final String format) {
    return parseDateToStr(format, new Date());
  }

  /** Format a given date as yyyy-MM-dd */
  public static String dateTime(final Date date) {
    return parseDateToStr(YYYY_MM_DD, date);
  }

  /** Format a given date into a string using the specified format */
  public static String parseDateToStr(final String format, final Date date) {
    return new SimpleDateFormat(format).format(date);
  }

  /** Parse a date string into a {@link Date} using the specified format */
  public static Date dateTime(final String format, final String ts) {
    try {
      return new SimpleDateFormat(format).parse(ts);
    } catch (ParseException e) {
      log.error("Failed to parse date: {} with format: {}", ts, format, e);
      throw new RuntimeException(e);
    }
  }

  /**
   * @return date path in format yyyy/MM/dd
   */
  public static String datePath() {
    return DateFormatUtils.format(new Date(), "yyyy/MM/dd");
  }

  /**
   * @return date string in format yyyyMMdd
   */
  public static String dateTime() {
    return DateFormatUtils.format(new Date(), "yyyyMMdd");
  }

  /** Convert a string to a {@link Date}, supporting multiple formats */
  public static Date parseDate(Object str) {
    if (str == null) {
      return null;
    }
    try {
      return parseDate(str.toString(), PARSE_PATTERNS);
    } catch (ParseException e) {
      log.warn("Failed to parse date: {}", str, e);
      return null;
    }
  }

  /**
   * @return server start time
   */
  public static Date getServerStartDate() {
    long time = ManagementFactory.getRuntimeMXBean().getStartTime();
    return new Date(time);
  }

  /** Calculate difference in days between two dates */
  public static int differentDaysByMillisecond(Date date1, Date date2) {
    return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
  }

  /**
   * Calculate time difference in days, hours, and minutes.
   *
   * @param endDate end time
   * @param startTime start time
   * @return human-readable difference, e.g., "1 days 2 hours 30 minutes"
   */
  public static String timeDistance(Date endDate, Date startTime) {
    long nd = 1000L * 60 * 60 * 24;
    long nh = 1000L * 60 * 60;
    long nm = 1000L * 60;

    long diff = endDate.getTime() - startTime.getTime();

    long day = diff / nd;
    long hour = diff % nd / nh;
    long min = diff % nd % nh / nm;

    return String.format("%d days %d hours %d minutes", day, hour, min);
  }

  /** Convert {@link LocalDateTime} to {@link Date} */
  public static Date toDate(LocalDateTime temporalAccessor) {
    ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
    return Date.from(zdt.toInstant());
  }

  /** Convert {@link LocalDate} to {@link Date} */
  public static Date toDate(LocalDate temporalAccessor) {
    LocalDateTime localDateTime = temporalAccessor.atStartOfDay();
    ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
    return Date.from(zdt.toInstant());
  }
}
