package com.api.common.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * Global application configuration.
 *
 * <p>Binds properties prefixed with {@code app.*} from application.yml/application.properties.
 * Provides easy access to project settings such as name, version, file storage paths, captcha type,
 * and whether IP address lookup is enabled.
 *
 * <p>Example:
 *
 * <pre>
 * app:
 *   name: MyApp
 *   version: 1.0.0
 *   copyrightYear: 2025
 *   profile: /data/app
 *   addressEnabled: true
 *   captchaType: math
 * </pre>
 *
 * @author
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

  /** Application display name */
  private String name;

  /** Application version number */
  private String version;

  /** Copyright year */
  private String copyrightYear;

  /** Base path for file storage (uploads, downloads, avatars, etc.) */
  private String profile;

  /** Enable IP/geo-address lookup */
  private boolean addressEnabled;

  /** Captcha type (e.g., math, char) */
  private String captchaType;

  // ------------------------------------------------------------------------
  // 📁 File Path Helpers
  // ------------------------------------------------------------------------

  /** Import file directory */
  public String getImportPath() {
    return profile + "/import";
  }

  /** Avatar storage directory */
  public String getAvatarPath() {
    return profile + "/avatar";
  }

  /** File download directory */
  public String getDownloadPath() {
    return profile + "/download";
  }

  /** General upload directory */
  public String getUploadPath() {
    return profile + "/upload";
  }

  // ------------------------------------------------------------------------
  // 🧩 Utility
  // ------------------------------------------------------------------------

  /** Logs all loaded configuration values at startup. */
  public void logConfiguration() {
    log.info(
        """
            ⚙️ Application Configuration Loaded:
              • Name: {}
              • Version: {}
              • Copyright Year: {}
              • Profile Path: {}
              • Address Lookup Enabled: {}
              • Captcha Type: {}
            """,
        name,
        version,
        copyrightYear,
        profile,
        addressEnabled,
        captchaType);
  }
}
