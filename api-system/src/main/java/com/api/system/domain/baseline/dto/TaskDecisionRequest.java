package com.api.system.domain.baseline.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskDecisionRequest {

  @NotBlank(message = "Decision is required")
  private String decision;

  private String comment;

  private String actorId;

  private String actorName;

  private String actorRole;
}
