package com.api.framework.client.email.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSendCommand {
    private String from;
    private List<String> toAddresses;
    private String subject;
    private String htmlBody;
    private String textBody;
    private String configurationSetName;
}
