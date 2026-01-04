package com.api.system.service.email;

import com.api.common.domain.email.BatchEmailSendRequest;
import com.api.common.domain.email.BatchEmailSendResult;
import com.api.common.domain.email.EmailPreviewRequest;
import com.api.common.domain.email.EmailPreviewResult;

public interface EmailAppService {

    EmailPreviewResult preview(EmailPreviewRequest request);

    BatchEmailSendResult sendBatch(BatchEmailSendRequest request);
}
