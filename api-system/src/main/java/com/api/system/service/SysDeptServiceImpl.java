package com.api.system.service;

import com.api.common.domain.SysDept;
import com.api.common.utils.jpa.SpecificationBuilder;
import com.api.persistence.repository.SysDeptRepository;
import com.api.system.service.SysDeptService;
import com.api.common.utils.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysDeptServiceImpl implements SysDeptService {

  private final SysDeptRepository deptRepository;

  @Override
  public Page<SysDept> selectDeptList(SysDept filter, Pageable pageable) {
    Specification<SysDept> spec =
        SpecificationBuilder.<SysDept>builder()
            .like("deptName", filter.getDeptName())
            .eq("status", filter.getStatus())
            .eq("parentId", filter.getParentId());

    return deptRepository.findAll(spec, pageable);
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
}
