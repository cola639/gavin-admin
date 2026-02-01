package com.api.boot.controller.baseline;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.system.domain.baseline.BaselineEvent;
import com.api.system.domain.baseline.BaselineRequest;
import com.api.system.domain.baseline.dto.BaselineCreateRequest;
import com.api.system.domain.baseline.dto.BaselinePostActionRequest;
import com.api.system.domain.baseline.dto.BaselineSubmitRequest;
import com.api.system.domain.baseline.dto.BaselineSummary;
import com.api.system.service.BaselineWorkflowService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/baselines")
@RequiredArgsConstructor
public class BaselineController extends BaseController {

  private final BaselineWorkflowService baselineWorkflowService;

  @GetMapping
  public TableDataInfo<BaselineSummary> list(
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "approvalStatus", required = false) String approvalStatus,
      @RequestParam(value = "ownerId", required = false) String ownerId,
      @RequestParam(value = "reviewerId", required = false) String reviewerId,
      @RequestParam(value = "pendingActionType", required = false) String pendingActionType,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
      @RequestParam(value = "sort", required = false) String sort) {

    Pageable pageable = buildPageable(page, size, sort);
    Page<BaselineSummary> result =
        baselineWorkflowService.listBaselines(
            status, approvalStatus, ownerId, reviewerId, pendingActionType, pageable);
    return TableDataInfo.success(result);
  }

  @GetMapping("/{id}")
  public AjaxResult<BaselineRequest> getById(@PathVariable Long id) {
    return AjaxResult.success(baselineWorkflowService.getBaseline(id));
  }

  @GetMapping("/{id}/timeline")
  public AjaxResult<List<BaselineEvent>> timeline(@PathVariable Long id) {
    return AjaxResult.success(baselineWorkflowService.getTimeline(id));
  }

  @PostMapping
  public AjaxResult<BaselineRequest> create(@Valid @RequestBody BaselineCreateRequest request) {
    return AjaxResult.success(baselineWorkflowService.createDraft(request));
  }

  @PostMapping("/{id}/submit")
  public AjaxResult<BaselineRequest> submit(
      @PathVariable Long id, @RequestBody BaselineSubmitRequest request) {
    return AjaxResult.success(baselineWorkflowService.submitBaseline(id, request));
  }

  @PostMapping("/{id}/post-action/request")
  public AjaxResult<BaselineRequest> requestPostAction(
      @PathVariable Long id, @Valid @RequestBody BaselinePostActionRequest request) {
    return AjaxResult.success(baselineWorkflowService.requestPostAction(id, request));
  }

  private Pageable buildPageable(int page, int size, String sort) {
    int pageIndex = Math.max(page - 1, 0);
    int pageSize = size <= 0 ? 20 : size;
    Sort sortOrder = parseSort(sort);
    return PageRequest.of(pageIndex, pageSize, sortOrder);
  }

  private Sort parseSort(String sort) {
    if (!StringUtils.hasText(sort)) {
      return Sort.by(Sort.Direction.DESC, "updatedAt");
    }
    String[] parts = sort.split(",");
    String property = parts[0];
    if (parts.length > 1) {
      Sort.Direction direction = Sort.Direction.fromString(parts[1]);
      return Sort.by(direction, property);
    }
    return Sort.by(property).ascending();
  }
}
