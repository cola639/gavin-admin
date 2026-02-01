package com.api.boot.controller.baseline;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.system.domain.baseline.BaselineRequest;
import com.api.system.domain.baseline.dto.BaselineTaskSummary;
import com.api.system.domain.baseline.dto.TaskDecisionRequest;
import com.api.system.service.BaselineWorkflowService;
import jakarta.validation.Valid;
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
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class BaselineTaskController extends BaseController {

  private final BaselineWorkflowService baselineWorkflowService;

  @GetMapping("/my")
  public TableDataInfo<BaselineTaskSummary> myTasks(
      @RequestParam("assigneeId") String assigneeId,
      @RequestParam(value = "assigneeRole", required = false, defaultValue = "CYBER_SME")
          String assigneeRole,
      @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status,
      @RequestParam(value = "stepCode", required = false) String stepCode,
      @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
      @RequestParam(value = "sort", required = false) String sort) {

    Pageable pageable = buildPageable(page, size, sort);
    Page<BaselineTaskSummary> result =
        baselineWorkflowService.listMyTasks(assigneeId, assigneeRole, status, stepCode, pageable);
    return TableDataInfo.success(result);
  }

  @PostMapping("/{taskId}/decision")
  public AjaxResult<BaselineRequest> decide(
      @PathVariable Long taskId, @Valid @RequestBody TaskDecisionRequest request) {
    return AjaxResult.success(baselineWorkflowService.decideTask(taskId, request));
  }

  private Pageable buildPageable(int page, int size, String sort) {
    int pageIndex = Math.max(page - 1, 0);
    int pageSize = size <= 0 ? 20 : size;
    Sort sortOrder = parseSort(sort);
    return PageRequest.of(pageIndex, pageSize, sortOrder);
  }

  private Sort parseSort(String sort) {
    if (!StringUtils.hasText(sort)) {
      return Sort.by(Sort.Direction.DESC, "createdAt");
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
