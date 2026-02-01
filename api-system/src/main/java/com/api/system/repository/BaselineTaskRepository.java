package com.api.system.repository;

import com.api.system.domain.baseline.BaselineTask;
import com.api.system.domain.baseline.dto.BaselineTaskSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BaselineTaskRepository
    extends JpaRepository<BaselineTask, Long>, JpaSpecificationExecutor<BaselineTask> {

  @Query(
      """
          select new com.api.system.domain.baseline.dto.BaselineTaskSummary(
            t.id,
            t.requestId,
            t.stepCode,
            t.assigneeRole,
            t.assigneeId,
            t.assigneeName,
            t.status,
            t.decision,
            t.comment,
            t.createdAt,
            t.actedAt,
            r.requestNo,
            r.title,
            r.status,
            r.approvalStatus,
            r.pendingActionType,
            r.currentStep
          )
          from BaselineTask t
          join BaselineRequest r on r.id = t.requestId
          where (:assigneeId is null or t.assigneeId = :assigneeId)
            and (:assigneeRole is null or t.assigneeRole = :assigneeRole)
            and (:status is null or t.status = :status)
            and (:stepCode is null or t.stepCode = :stepCode)
          """)
  Page<BaselineTaskSummary> findTaskSummaries(
      @Param("assigneeId") String assigneeId,
      @Param("assigneeRole") String assigneeRole,
      @Param("status") String status,
      @Param("stepCode") String stepCode,
      Pageable pageable);
}
