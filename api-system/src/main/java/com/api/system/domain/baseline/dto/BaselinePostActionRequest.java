package com.api.system.domain.baseline.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaselinePostActionRequest {

  @NotBlank(message = "Action type is required")
  private String actionType;

  private String reason;

  private String reviewerId;

  private String reviewerName;

  private String actorId;

  private String actorName;

  private String actorRole;
}
