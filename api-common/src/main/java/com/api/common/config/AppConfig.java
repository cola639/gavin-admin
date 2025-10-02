package com.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

  /** Project name */
  private String name;

  /** Project version */
  private String version;

  /** Copyright year */
  private String copyrightYear;

  /** Base directory for file uploads (profile path) */
  private String profile;

  /** Whether IP address lookup is enabled */
  private static boolean addressEnabled;

  /** Captcha type (e.g. "math", "char") */
  private String captchaType;

  // ----------------- Derived Paths -----------------

  /** Import file path */
  public String getImportPath() {
    return profile + "/import";
  }

  /** Avatar upload path */
  public String getAvatarPath() {
    return profile + "/avatar";
  }

  /** File download path */
  public String getDownloadPath() {
    return profile + "/download";
  }

  /** General upload path */
  public String getUploadPath() {
    return profile + "/upload";
  }

  public static boolean getAddressEnabled() {
    return addressEnabled;
  }
}
