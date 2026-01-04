package com.api.framework.client.email.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSendResponse {
    private String messageId;
    private int acceptedCount;
}
