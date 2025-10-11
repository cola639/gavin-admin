package com.api.boot.controller.monitor;

import com.api.framework.aspectj.SQLMetricsInspectorAspect;
import com.api.common.constant.CacheConstants;
import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.redis.RedisCache;
import com.api.common.utils.StringUtils;
import com.api.common.utils.pagination.TableDataInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST Controller for monitoring SQL performance and statistics.
 *
 * <p>Retrieves SQL metrics collected via {@link
 * SQLMetricsInspectorAspect} and {@link
 * com.api.framework.interceptor.SQLDetailInspector}.
 *
 * <p>Data source: Redis keys with pattern "metrics:sqlDetail:*"
 */
@Slf4j
@RestController
@RequestMapping("/monitor/sql")
@RequiredArgsConstructor
public class SysSqlController extends BaseController {

  private final RedisCache redisCache;

  /**
   * List all tracked SQL metrics.
   *
   * @param keyword Optional filter for SQL method or text
   * @param page Page number (default = 1)
   * @param size Page size (default = 10)
   * @return Paginated SQL metrics
   */
  @GetMapping("/list")
  public TableDataInfo list(
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {

    // 1️⃣ Get all keys
    Set<String> keys = redisCache.keys(CacheConstants.MONITOR_SQL_PREFIX + "*");

    if (keys.isEmpty()) {
      return getDataTable(Collections.emptyList());
    }

    // 2️⃣ Fetch metrics from Redis
    List<Map<String, Object>> allMetrics =
        keys.stream()
            .map(redisCache::getCacheMap)
            .filter(Objects::nonNull)
            .map(HashMap::new)
            .peek(m -> m.put("key", m.getOrDefault("SQLMethod", "<unknown>")))
            .collect(Collectors.toList());

    // 3️⃣ Filter by keyword (method or SQL)
    if (StringUtils.isNotEmpty(keyword)) {
      allMetrics =
          allMetrics.stream()
              .filter(
                  m ->
                      m.values().stream()
                          .anyMatch(
                              v ->
                                  v != null
                                      && v.toString()
                                          .toLowerCase()
                                          .contains(keyword.toLowerCase())))
              .collect(Collectors.toList());
    }

    // 4️⃣ Sort by execution count (descending)
    allMetrics.sort(
        Comparator.comparingLong(
                (Map<String, Object> m) ->
                    Long.parseLong(m.getOrDefault("ExecuteCount", "0").toString()))
            .reversed());

    // 5️⃣ Apply pagination
    int total = allMetrics.size();
    int fromIndex = Math.max((page - 1) * size, 0);
    int toIndex = Math.min(fromIndex + size, total);
    List<Map<String, Object>> pageData =
        total == 0 ? Collections.emptyList() : allMetrics.subList(fromIndex, toIndex);

    log.debug("Retrieved {} SQL metrics (page={}, size={})", total, page, size);

    return getDataTable(pageData, total);
  }

  /**
   * Get a specific SQL metric detail by method name.
   *
   * @param method Repository method name, e.g. SysDeptRepository.countByParentIdAndDelFlag
   * @return Detailed metrics for that method
   */
  @GetMapping("/{method}")
  public AjaxResult getMetricDetail(@PathVariable String method) {
    String redisKey = CacheConstants.MONITOR_SQL_PREFIX + method;
    Map<String, Object> metrics = redisCache.getCacheMap(redisKey);
    if (metrics == null || metrics.isEmpty()) {
      return AjaxResult.error("No metrics found for method: " + method);
    }
    return AjaxResult.success(metrics);
  }

  /**
   * Delete SQL metrics by method.
   *
   * @param method Repository method name
   * @return success message
   */
  @DeleteMapping("/{method}")
  public AjaxResult deleteMetric(@PathVariable String method) {
    String redisKey = CacheConstants.MONITOR_SQL_PREFIX + method;
    boolean deleted = redisCache.deleteObject(redisKey);
    if (deleted) {
      return AjaxResult.success("Deleted metrics for " + method);
    }
    return AjaxResult.error("No metrics found for method: " + method);
  }

  /**
   * Clear all SQL metrics from Redis.
   *
   * @return success message
   */
  @DeleteMapping("/clear")
  public AjaxResult clearAll() {
    Set<String> keys = redisCache.keys(CacheConstants.MONITOR_SQL_PREFIX + "*");
    if (keys.isEmpty()) {
      return AjaxResult.success("No SQL metrics to clear.");
    }
    redisCache.deleteObject(keys);
    return AjaxResult.success("Cleared " + keys.size() + " SQL metrics.");
  }
}
