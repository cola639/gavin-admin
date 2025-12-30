package com.api.system.service;

import com.api.common.domain.SysDept;
import com.api.common.domain.SysDeptMapper;
import com.api.common.domain.TreeSelect;
import com.api.common.enums.StatusEnum;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.framework.exception.ServiceException;
import com.api.system.repository.SysDeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDeptService {

  private final SysDeptRepository deptRepository;
  private final SysDeptMapper deptMapper;

  public List<TreeSelect> selectDeptList(SysDept filter) {
    SysDept criteria = (filter != null) ? filter : new SysDept();

    // Build dynamic spec + sort using your builder
    SpecificationBuilder<SysDept> b =
        SpecificationBuilder.<SysDept>builder()
            .eq("delFlag", criteria.getDelFlag()) // or DelFlagEnum.NORMAL.getCode()
            .like("deptName", criteria.getDeptName())
            .eq("parentId", criteria.getParentId())
            .eq("status", criteria.getStatus())
            .orderByAsc("parentId")
            .orderByAsc("orderNum")
            .orderByAsc("deptId");

    // Apply spec + sort
    List<SysDept> depts = deptRepository.findAll(b, b.buildSort());

    // Build tree + map to TreeSelect
    List<SysDept> tree = buildDeptTreeFast(depts);
    return tree.stream().map(TreeSelect::new).toList();
  }

  public Page<SysDept> getAllDept(Pageable pageable) {
    return deptRepository.getAllDept(pageable);
  }

  public SysDept selectDeptById(Long deptId) {
    return deptRepository.findById(deptId).orElse(null);
  }

  @Transactional
  public SysDept saveDept(SysDept dept) {

    Long parentId = dept.getParentId();
    if (parentId == null || parentId == 0L) {
      // root node
      dept.setParentId(0L);
      dept.setAncestors("0");
      return deptRepository.save(dept);
    }

    // load parent (Optional -> entity)
    SysDept parent =
        deptRepository
            .findById(parentId)
            .orElseThrow(() -> new ServiceException("Parent dept not found: " + parentId));

    if (StatusEnum.DISABLED.getCode().equals(parent.getStatus())) {
      throw new ServiceException("Department is disabled; cannot create a sub-department.");
    }

    // build ancestors: parent.ancestors + "," + parentId
    String parentAncestors = parent.getAncestors();
    String ancestors =
        (parentAncestors == null || parentAncestors.isBlank())
            ? String.valueOf(parentId)
            : parentAncestors + "," + parentId;

    dept.setAncestors(ancestors);

    return deptRepository.save(dept);
  }

  @Transactional
  public SysDept updateDept(SysDept req) {
    if (req == null || req.getDeptId() == null) {
      throw new ServiceException("deptId cannot be null");
    }

    SysDept existing =
        deptRepository
            .findById(req.getDeptId())
            .orElseThrow(() -> new ServiceException("Department not found: " + req.getDeptId()));

    // parentId: if not provided, keep existing
    Long newParentId = (req.getParentId() != null) ? req.getParentId() : existing.getParentId();
    if (newParentId == null) newParentId = 0L;

    // cannot set parent to itself
    if (Objects.equals(existing.getDeptId(), newParentId)) {
      throw new ServiceException("parentId cannot be the same as deptId");
    }

    // if parent changed, validate + rebuild ancestors
    if (!Objects.equals(existing.getParentId(), newParentId)) {
      if (newParentId == 0L) {
        existing.setParentId(0L);
        existing.setAncestors("0");
      } else {
        Long finalNewParentId = newParentId;
        SysDept parent =
            deptRepository
                .findById(newParentId)
                .orElseThrow(
                    () -> new ServiceException("Parent dept not found: " + finalNewParentId));

        if (StatusEnum.DISABLED.getCode().equals(parent.getStatus())) {
          throw new ServiceException("Parent department is disabled; cannot move under it.");
        }

        // prevent cycle: parent cannot be a descendant of current dept
        if (isDescendant(parent, existing.getDeptId())) {
          throw new ServiceException("Invalid parentId: would create a cycle.");
        }

        existing.setParentId(newParentId);
        existing.setAncestors(buildAncestors(parent));
      }
    }

    // copy allowed fields (deptName/orderNum/leader/phone/email/status/...)
    deptMapper.updateFromReq(req, existing);

    return deptRepository.save(existing);
  }

  @Transactional
  public void deleteDept(Long deptId) {
    deptRepository.deleteById(deptId);
  }

  @Transactional
  public void deleteDeptByIds(List<Long> deptIds) {
    deptRepository.deleteAllByIdInBatch(deptIds);
  }

  public boolean checkDeptNameUnique(String deptName, Long parentId) {
    return !deptRepository.existsByDeptNameAndParentId(deptName, parentId);
  }

  /** Direct children dept IDs (only one level). */
  public List<Long> selectChildDeptIds(Long parentId) {
    if (parentId == null) {
      return List.of();
    }
    return deptRepository.findChildDeptIds(parentId);
  }

  /** Self + all descendants dept IDs (tree scope). */
  public List<Long> findDeptAndChildrenIds(Long parentId) {
    if (parentId == null) {
      return List.of();
    }
    return deptRepository.findDeptAndChildrenIds(parentId);
  }

  public long countActiveChildren(Long deptId) {
    return deptRepository.countByParentIdAndDelFlag(deptId, "0");
  }

  private List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts) {
    List<SysDept> deptTrees = buildDeptTree(depts);
    return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
  }

  private List<SysDept> buildDeptTree(List<SysDept> depts) {
    List<SysDept> returnList = new ArrayList<SysDept>();
    List<Long> tempList = depts.stream().map(SysDept::getDeptId).collect(Collectors.toList());
    for (SysDept dept : depts) {
      // 如果是顶级节点, 遍历该父节点的所有子节点
      if (!tempList.contains(dept.getParentId())) {
        recursionFn(depts, dept);
        returnList.add(dept);
      }
    }
    if (returnList.isEmpty()) {
      returnList = depts;
    }
    return returnList;
  }

  private void recursionFn(List<SysDept> list, SysDept t) {
    // 得到子节点列表
    List<SysDept> childList = getChildList(list, t);
    t.setChildren(childList);
    for (SysDept tChild : childList) {
      if (hasChild(list, tChild)) {
        recursionFn(list, tChild);
      }
    }
  }

  private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
    List<SysDept> tlist = new ArrayList<SysDept>();
    Iterator<SysDept> it = list.iterator();
    while (it.hasNext()) {
      SysDept n = (SysDept) it.next();
      if (StringUtils.isNotNull(n.getParentId())
          && n.getParentId().longValue() == t.getDeptId().longValue()) {
        tlist.add(n);
      }
    }
    return tlist;
  }

  private boolean hasChild(List<SysDept> list, SysDept t) {
    return getChildList(list, t).size() > 0;
  }

  private List<SysDept> buildDeptTreeFast(List<SysDept> depts) {
    if (depts == null || depts.isEmpty()) {
      return List.of();
    }

    // parentId -> children list
    Map<Long, List<SysDept>> childrenMap =
        depts.stream()
            .collect(Collectors.groupingBy(d -> Optional.ofNullable(d.getParentId()).orElse(0L)));

    // attach children
    depts.forEach(
        d -> d.setChildren(new ArrayList<>(childrenMap.getOrDefault(d.getDeptId(), List.of()))));

    // root detection: parentId == 0 OR parent not included (important when filtering)
    Set<Long> ids = depts.stream().map(SysDept::getDeptId).collect(Collectors.toSet());

    List<SysDept> roots =
        depts.stream()
            .filter(
                d -> {
                  Long pid = d.getParentId();
                  return pid == null || pid == 0L || !ids.contains(pid);
                })
            .toList();

    return roots.isEmpty() ? depts : roots;
  }

  private String buildAncestors(SysDept parent) {
    String parentAnc = parent.getAncestors();
    Long parentId = parent.getDeptId();

    if (parentAnc == null || parentAnc.isBlank()) {
      return String.valueOf(parentId);
    }
    return parentAnc + "," + parentId;
  }

  /** true if parent is under current dept (cycle) */
  private boolean isDescendant(SysDept parent, Long currentDeptId) {
    if (parent == null || currentDeptId == null) return false;
    String anc = parent.getAncestors();
    if (anc == null || anc.isBlank()) return false;

    // ancestors like "0,100,101"
    for (String part : anc.split(",")) {
      if (part.trim().equals(String.valueOf(currentDeptId))) {
        return true;
      }
    }
    return false;
  }
}
