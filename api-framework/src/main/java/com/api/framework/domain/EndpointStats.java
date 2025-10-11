package com.api.framework.domain;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointStats implements Serializable {

  private String uri;

  private long requestCount;
  private double requestTimeAverage;
  private long requestTimeMillisMax;
  private int concurrentMax;

  private long successCount;
  private long failCount;
}
