package com.api.system.domain.baseline.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BaselineCreateRequest {

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Owner ID is required")
  private String ownerId;

  @NotBlank(message = "Owner name is required")
  private String ownerName;

  private String reviewerId;

  private String reviewerName;
}
