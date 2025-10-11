package com.api.boot.controller.system;

import com.api.common.controller.BaseController;
import com.api.common.domain.AjaxResult;
import com.api.common.domain.SysDept;
import com.api.common.utils.StringUtils;
import com.api.common.utils.pagination.TableDataInfo;
import com.api.framework.annotation.TrackEndpointStats;
import com.api.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Department entities (sys_dept).
 *
 * <p>This controller provides CRUD operations for departments, including filtering, tree-exclusion,
 * and validation logic. It uses JPA-based persistence with dynamic query support through {@link
 * SysDeptService}.
 *
 * <p>Technology stack:
 *
 * <ul>
 *   <li>Spring Boot 3.5
 *   <li>Java 17
 *   <li>Spring Data JPA + Specifications
 * </ul>
 *
 * <p>All endpoints return standardized responses using {@link AjaxResult}.
 *
 * @author Gavin
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {

  private final SysDeptService deptService;

  /**
   * Retrieves a paginated list of departments based on the provided filter criteria.
   *
   * @param filter filter conditions (e.g., department name, status)
   * @param page the current page number (0-based)
   * @param size the page size
   * @return paginated table data containing department information
   */
  @TrackEndpointStats
  @GetMapping("/list")
  public TableDataInfo list(
      SysDept filter,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    var resultPage = deptService.selectDeptList(filter, pageable);

    log.debug(
        "Fetched {} departments (page={}, size={})", resultPage.getTotalElements(), page, size);
    return getDataTable(resultPage);
  }

  @TrackEndpointStats
  @GetMapping("/listAll")
  public TableDataInfo listAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<SysDept> result = deptService.getAllDept(pageable);

    return getDataTable(result);
  }

  /**
   * Retrieves all departments excluding a specific node and its descendants.
   *
   * @param deptId the department ID to exclude
   * @return list of departments excluding the given node
   */
  @GetMapping("/list/exclude/{deptId}")
  public AjaxResult excludeChild(@PathVariable Long deptId) {
    List<SysDept> departments =
        deptService.selectDeptList(new SysDept(), Pageable.unpaged()).getContent();

    departments.removeIf(
        d ->
            d.getDeptId().equals(deptId)
                || ArrayUtils.contains(
                    StringUtils.split(d.getAncestors(), ","), deptId.toString()));

    log.info("Excluded department id={} and its descendants from list", deptId);
    return success(departments);
  }

  /**
   * Retrieves detailed information for a specific department.
   *
   * @param deptId the department ID
   * @return department details
   */
  @GetMapping("/{deptId}")
  public AjaxResult getInfo(@PathVariable Long deptId) {
    log.debug("Fetching department details for id={}", deptId);
    SysDept dept = deptService.selectDeptById(deptId);

    if (dept == null) {
      log.warn("Department with id={} not found", deptId);
      return error("Department not found.");
    }
    return success(dept);
  }

  /**
   * Creates a new department.
   *
   * @param dept the department to create
   * @return success or error result
   */
  @PostMapping
  public AjaxResult add(@Validated @RequestBody SysDept dept) {
    log.info("Creating new department: {}", dept.getDeptName());

    if (!deptService.checkDeptNameUnique(dept.getDeptName(), dept.getParentId())) {
      log.warn("Failed to create department '{}': name already exists", dept.getDeptName());
      return error(
          "Failed to create department '" + dept.getDeptName() + "': name already exists.");
    }

    dept.setCreateBy(getUsername());
    deptService.saveDept(dept);

    log.info(
        "Department '{}' created successfully by user '{}'", dept.getDeptName(), getUsername());
    return success("Department created successfully.");
  }

  /**
   * Updates an existing department.
   *
   * @param dept the updated department details
   * @return success or error result
   */
  @PutMapping
  public AjaxResult edit(@Validated @RequestBody SysDept dept) {
    log.info("Updating department: {}", dept.getDeptName());
    Long deptId = dept.getDeptId();

    // Validation rules
    if (!deptService.checkDeptNameUnique(dept.getDeptName(), dept.getParentId())) {
      log.warn("Failed to update department '{}': name already exists", dept.getDeptName());
      return error(
          "Failed to update department '" + dept.getDeptName() + "': name already exists.");
    }

    if (dept.getParentId() != null && dept.getParentId().equals(deptId)) {
      log.warn("Invalid update: department '{}' cannot be its own parent", dept.getDeptName());
      return error("Failed to update department: a department cannot be its own parent.");
    }

    if ("1".equals(dept.getStatus()) && deptService.countActiveChildren(deptId) > 0) {
      log.warn(
          "Failed to disable department '{}': active child departments exist", dept.getDeptName());
      return error("Failed to disable department: it contains active child departments.");
    }

    dept.setUpdateBy(getUsername());
    deptService.saveDept(dept);

    log.info("Department '{}' updated successfully by '{}'", dept.getDeptName(), getUsername());
    return success("Department updated successfully.");
  }

  /**
   * Deletes a department by ID.
   *
   * @param deptId the department ID
   * @return success or warning message
   */
  @DeleteMapping("/{deptId}")
  public AjaxResult remove(@PathVariable Long deptId) {
    log.warn("Attempting to delete department with id={}", deptId);

    long childCount = deptService.countActiveChildren(deptId);
    if (childCount > 0) {
      log.warn(
          "Cannot delete department id={}: {} active child departments exist", deptId, childCount);
      return warn("Cannot delete department: active child departments exist.");
    }

    // TODO: Add user validation when UserService is integrated
    deptService.deleteDept(deptId);

    log.info("Department with id={} deleted successfully", deptId);
    return success("Department deleted successfully.");
  }
}
