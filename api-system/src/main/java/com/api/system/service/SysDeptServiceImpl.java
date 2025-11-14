package com.api.system.service;

import com.api.common.annotation.DataSource;
import com.api.common.domain.SysDept;
import com.api.common.domain.TreeSelect;
import com.api.common.enums.DataSourceType;
import com.api.common.utils.StringUtils;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.persistence.repository.system.SysDeptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysDeptServiceImpl implements SysDeptService {

  private final SysDeptRepository deptRepository;

  @Override
  public List<TreeSelect> selectDeptList(SysDept filter) {
    List<SysDept> depts = deptRepository.findAll();
    return buildDeptTreeSelect(depts);
  }

  @Override
  public Page<SysDept> getAllDept(Pageable pageable) {
    return deptRepository.getAllDept(pageable);
  }

  @Override
  public SysDept selectDeptById(Long deptId) {
    return deptRepository.findById(deptId).orElse(null);
  }

  @Override
  @Transactional
  public SysDept saveDept(SysDept dept) {
    return deptRepository.save(dept);
  }

  @Override
  @Transactional
  public void deleteDept(Long deptId) {
    deptRepository.deleteById(deptId);
  }

  @Override
  public boolean checkDeptNameUnique(String deptName, Long parentId) {
    return !deptRepository.existsByDeptNameAndParentId(deptName, parentId);
  }

  @Override
  public List<SysDept> findChildren(Long parentId) {
    Specification<SysDept> spec = (root, query, cb) -> cb.equal(root.get("parentId"), parentId);
    return deptRepository.findAll(spec);
  }

  @Override
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
}
