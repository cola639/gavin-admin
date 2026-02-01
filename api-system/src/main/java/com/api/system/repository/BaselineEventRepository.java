package com.api.system.repository;

import com.api.system.domain.baseline.BaselineEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaselineEventRepository extends JpaRepository<BaselineEvent, Long> {

  List<BaselineEvent> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
