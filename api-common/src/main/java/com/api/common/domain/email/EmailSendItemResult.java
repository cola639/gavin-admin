package com.api.common.domain.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSendItemResult {
    private String email;
    private boolean success;
    private String messageId;
    private String error;
}
