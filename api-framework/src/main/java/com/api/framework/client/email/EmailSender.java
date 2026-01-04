package com.api.framework.client.email;

import com.api.framework.client.email.model.EmailSendCommand;
import com.api.framework.client.email.model.EmailSendResponse;

public interface EmailSender {
    EmailSendResponse send(EmailSendCommand command);
}
