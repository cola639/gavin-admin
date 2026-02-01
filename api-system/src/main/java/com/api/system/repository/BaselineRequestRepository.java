package com.api.system.repository;

import com.api.system.domain.baseline.BaselineRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BaselineRequestRepository
    extends JpaRepository<BaselineRequest, Long>, JpaSpecificationExecutor<BaselineRequest> {

  Optional<BaselineRequest> findByRequestNo(String requestNo);
}
