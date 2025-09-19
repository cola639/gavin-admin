package com.api.common.utils.jpa;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic Specification builder for dynamic JPA queries.
 */
public class SpecificationBuilder<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    private SpecificationBuilder() {
    }

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

    /** Like  */
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
    public <Y extends Comparable<? super Y>> SpecificationBuilder<T> between(String field, Y start, Y end) {
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

    /** Build final Specification */
    public Specification<T> build() {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Specification<T> spec : specs) {
                predicates.add(spec.toPredicate(root, query, cb));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
