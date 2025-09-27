package com.api.common.utils.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.*;

/** Generic Specification builder for dynamic JPA queries. */
public class SpecificationBuilder<T> implements Specification<T> {

  private final List<Specification<T>> specs = new ArrayList<>();
  private final List<Sort.Order> orders = new ArrayList<>();
  private int page = -1; // disabled by default
  private int size = 20; // default size

  private SpecificationBuilder() {}

  public static <T> SpecificationBuilder<T> builder() {
    return new SpecificationBuilder<>();
  }

  /** Equals condition */
  public SpecificationBuilder<T> eq(String field, Object value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.equal(root.get(field), value));
    }
    return this;
  }

  /** Not Equals condition */
  public SpecificationBuilder<T> ne(String field, Object value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.notEqual(root.get(field), value));
    }
    return this;
  }

  /** Like */
  public SpecificationBuilder<T> like(String field, String value) {
    if (StringUtils.hasText(value)) {
      specs.add((root, query, cb) -> cb.like(root.get(field), "%" + value + "%"));
    }
    return this;
  }

  /** Greater Than */
  public <Y extends Comparable<? super Y>> SpecificationBuilder<T> gt(String field, Y value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.greaterThan(root.get(field), value));
    }
    return this;
  }

  /** Less Than */
  public <Y extends Comparable<? super Y>> SpecificationBuilder<T> lt(String field, Y value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.lessThan(root.get(field), value));
    }
    return this;
  }

  /** Between */
  public <Y extends Comparable<? super Y>> SpecificationBuilder<T> between(
      String field, Y start, Y end) {
    if (start != null && end != null) {
      specs.add((root, query, cb) -> cb.between(root.get(field), start, end));
    }
    return this;
  }

  /** In */
  public SpecificationBuilder<T> in(String field, List<?> values) {
    if (values != null && !values.isEmpty()) {
      specs.add((root, query, cb) -> root.get(field).in(values));
    }
    return this;
  }

  /** Dynamic scope filter */
  public SpecificationBuilder<T> scope(String field, Object value) {
    return eq(field, value);
  }

  /** Add conditions from params map dynamically */
  public SpecificationBuilder<T> params(Map<String, Object> params) {
    if (params != null && !params.isEmpty()) {
      specs.add(
          (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (var entry : params.entrySet()) {
              String field = entry.getKey();
              Object value = entry.getValue();
              if (value != null) {
                predicates.add(cb.equal(root.get(field), value));
              }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
          });
    }
    return this;
  }

  // --- sorting ---
  public SpecificationBuilder<T> orderByAsc(String field) {
    if (field != null) orders.add(Sort.Order.asc(field));
    return this;
  }

  public SpecificationBuilder<T> orderByDesc(String field) {
    if (field != null) orders.add(Sort.Order.desc(field));
    return this;
  }

  // --- pagination ---
  public SpecificationBuilder<T> page(int page, int size) {
    this.page = page;
    this.size = size;
    return this;
  }

  public Pageable buildPageable() {
    if (page < 0) {
      return Pageable.unpaged();
    }
    return PageRequest.of(page, size, buildSort());
  }

  public Sort buildSort() {
    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
  }

  // --- build specification ---
  @Override
  public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    List<Predicate> predicates = new ArrayList<>();
    for (Specification<T> spec : specs) {
      predicates.add(spec.toPredicate(root, query, cb));
    }

    if (!orders.isEmpty()) {
      query.orderBy(
          orders.stream()
              .map(
                  o ->
                      o.isAscending()
                          ? cb.asc(root.get(o.getProperty()))
                          : cb.desc(root.get(o.getProperty())))
              .toList());
    }

    return cb.and(predicates.toArray(new Predicate[0]));
  }
}

// Example usage:
// 1. Simple query with conditions and sorting
// SpecificationBuilder<SysUser> spec = SpecificationBuilder.<SysUser>builder()
//        .like("username", "jack")
//        .eq("status", "ACTIVE")
//        .orderByAsc("username")
//        .orderByDesc("createTime");
//
// List<SysUser> users = sysUserRepository.findAll(spec);
//
// 2. Paginated query
// SpecificationBuilder<SysUser> builder = SpecificationBuilder.<SysUser>builder()
//        .like("username", "jack")
//        .eq("status", "ACTIVE")
//        .orderByAsc("username")
//        .orderByDesc("createTime")
//        .page(0, 10); // page 0, size 10
//
// Page<SysUser> pageResult = sysUserRepository.findAll(builder, builder.buildPageable());
