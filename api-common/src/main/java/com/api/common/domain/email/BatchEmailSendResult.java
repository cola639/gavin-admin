package com.api.common.domain.email;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchEmailSendResult {
    private String requestId;
    private int total;
    private int success;
    private int failed;
    private List<EmailSendItemResult> items;
}
