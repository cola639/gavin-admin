package com.api.boot.controller.monitor;

import com.api.common.domain.AjaxResult;
import com.api.common.redis.RedisCache;
import com.api.common.utils.StringUtils;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.framework.domain.EndpointStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.api.common.constant.CacheConstants.MONITOR_URI_KEY;

/**
 * Controller for exposing tracked URI performance metrics.
 *
 * <p>Retrieves data stored in Redis (by {@link com.api.framework.aop.EndpointStatsAspect}) and
 * transforms it into structured API responses for dashboards.
 *
 * <p>Supports search, sorting, and pagination.
 *
 * <p>Example Redis key format:
 *
 * <pre>
 * metrics:endpoint:/system/dept/list
 * </pre>
 *
 * @author Gavin
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/uri")
public class UriController {

  private final RedisCache redisCache;

  /**
   * Retrieve all tracked endpoint metrics.
   *
   * @param keyword Optional keyword to filter URIs.
   * @param page Current page number (0-based).
   * @param size Page size.
   * @param sort Optional field to sort by (e.g. "RequestCount", "RequestTimeAverage").
   * @param order Sort order ("asc" or "desc").
   * @return TableDataInfo containing endpoint metrics.
   */
  @GetMapping("/list")
  public TableDataInfo listMetrics(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "RequestCount") String sort,
      @RequestParam(required = false, defaultValue = "desc") String order) {

    Set<String> keys = redisCache.keys(MONITOR_URI_KEY);
    log.debug("🔍 Found {} metric keys in Redis", keys.size());

    // Map to DTOs
    List<EndpointStats> allStats =
        keys.stream()
            .map(this::mapToEndpointStats)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    // Apply keyword filtering
    if (StringUtils.hasText(keyword)) {
      allStats =
          allStats.stream()
              .filter(s -> s.getUri().toLowerCase().contains(keyword.toLowerCase()))
              .collect(Collectors.toList());
    }

    // Sort dynamically
    Comparator<EndpointStats> comparator = getComparator(sort);
    if ("desc".equalsIgnoreCase(order)) {
      comparator = comparator.reversed();
    }
    allStats.sort(comparator);

    // Pagination
    int start = Math.max(page * size, 0);
    int end = Math.min(start + size, allStats.size());
    List<EndpointStats> paged =
        start > end ? Collections.emptyList() : allStats.subList(start, end);

    log.debug("📊 Returning {} metrics (page={}, size={})", paged.size(), page, size);
    return new TableDataInfo(paged, allStats.size());
  }

  /** Retrieve a specific URI metric by key. */
  @GetMapping("/{uri}")
  public AjaxResult getMetric(@PathVariable String uri) {
    String redisKey = MONITOR_URI_KEY + uri;
    Map<String, Object> map = redisCache.getCacheMap(redisKey);
    if (map.isEmpty()) {
      return AjaxResult.error("No metrics found for URI: " + uri);
    }
    return AjaxResult.success(mapToEndpointStats(redisKey, map));
  }

  // ---------------------- Helper Methods ----------------------

  private EndpointStats mapToEndpointStats(String redisKey) {
    Map<String, Object> map = redisCache.getCacheMap(redisKey);
    if (map == null || map.isEmpty()) return null;
    return mapToEndpointStats(redisKey, map);
  }

  private EndpointStats mapToEndpointStats(String redisKey, Map<String, Object> map) {
    try {
      return EndpointStats.builder()
          .uri(redisKey.replace(MONITOR_URI_KEY, ""))
          .requestCount(parseLong(map.get("RequestCount")))
          .requestTimeAverage(parseDouble(map.get("RequestTimeAverage")))
          .requestTimeMillisMax(parseLong(map.get("RequestTimeMillisMax")))
          .concurrentMax(parseInt(map.get("ConcurrentMax")))
          .successCount(parseLong(map.get("SuccessCount")))
          .failCount(parseLong(map.get("FailCount")))
          .build();
    } catch (Exception e) {
      log.error("❌ Failed to map metrics for {}", redisKey, e);
      return null;
    }
  }

  private Comparator<EndpointStats> getComparator(String field) {
    return switch (field) {
      case "RequestTimeAverage" -> Comparator.comparingDouble(EndpointStats::getRequestTimeAverage);
      case "RequestTimeMillisMax" ->
          Comparator.comparingLong(EndpointStats::getRequestTimeMillisMax);
      case "ConcurrentMax" -> Comparator.comparingInt(EndpointStats::getConcurrentMax);
      case "SuccessCount" -> Comparator.comparingLong(EndpointStats::getSuccessCount);
      case "FailCount" -> Comparator.comparingLong(EndpointStats::getFailCount);
      default -> Comparator.comparingLong(EndpointStats::getRequestCount);
    };
  }

  private long parseLong(Object o) {
    if (o == null) return 0L;
    try {
      return Long.parseLong(o.toString());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private int parseInt(Object o) {
    if (o == null) return 0;
    try {
      return Integer.parseInt(o.toString());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private double parseDouble(Object o) {
    if (o == null) return 0.0;
    try {
      return Double.parseDouble(o.toString());
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }
}
