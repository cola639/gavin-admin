package com.api.framework.config.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    /**
     * Only "ses" is supported in this module.
     */
    private String provider = "ses";

    /**
     * Verified sender address in SES.
     */
    private String from;

    /**
     * Your business batch size; real SES per-call recipients limit is 50.
     */
    private int maxBatchSize = 100;

    /**
     * Retry count for transient errors (429/5xx).
     */
    private int maxRetries = 4;

    /**
     * Future extension point (store rendered snapshots).
     */
    private boolean snapshotEnabled = false;

    /**
     * Classpath root relative path, e.g. "templates/email"
     */
    private String templateRoot = "templates/email";

    private SesProperties ses = new SesProperties();

    @Data
    public static class SesProperties {
        private String region;
        private String accessKey;
        private String secretKey;
        private String configurationSet;
    }
}
