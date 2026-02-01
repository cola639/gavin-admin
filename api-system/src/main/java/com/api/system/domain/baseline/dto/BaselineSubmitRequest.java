package com.api.system.domain.baseline.dto;

import lombok.Data;

@Data
public class BaselineSubmitRequest {

  private String reviewerId;

  private String reviewerName;

  private String actorId;

  private String actorName;

  private String actorRole;
}
