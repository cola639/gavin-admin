package com.api.common.constant;

import io.jsonwebtoken.Claims;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

// import io.jsonwebtoken.Claims;

/**
 * Common application constants. Provides definitions for charset, system flags, authentication, JWT
 * claims, resource mapping, security configurations, and job whitelists.
 *
 * @author
 */
public class Constants {

  // ---------- Charset ----------
  /** UTF-8 charset */
  public static final String UTF8 = StandardCharsets.UTF_8.name();

  /** GBK charset */
  public static final String GBK = "GBK";

  /** Default system locale */
  public static final Locale DEFAULT_LOCALE = Locale.SIMPLIFIED_CHINESE;

  // ---------- Protocols & URLs ----------
  /** WWW prefix */
  public static final String WWW = "www.";

  /** HTTP request prefix */
  public static final String HTTP = "http://";

  /** HTTPS request prefix */
  public static final String HTTPS = "https://";

  // ---------- Generic Result Flags ----------
  /** Common success flag */
  public static final String SUCCESS = "0";

  /** Common failure flag */
  public static final String FAIL = "1";

  // ---------- Authentication & Login ----------
  /** Login success message */
  public static final String LOGIN_SUCCESS = "Success";

  /** Logout message */
  public static final String LOGOUT = "Logout";

  /** Register message */
  public static final String REGISTER = "Register";

  /** Login failure message */
  public static final String LOGIN_FAIL = "Error";

  // ---------- Roles & Permissions ----------
  /** Permission for all resources */
  public static final String ALL_PERMISSION = "*:*:*";

  /** Super admin role identifier */
  public static final String SUPER_ADMIN = "admin";

  /** Role delimiter */
  public static final String ROLE_DELIMITER = ",";

  /** Permission delimiter */
  public static final String PERMISSION_DELIMITER = ",";

  // ---------- Captcha ----------
  /** Captcha expiration in minutes */
  public static final int CAPTCHA_EXPIRATION = 2;

  // ---------- Tokens & JWT ----------
  /** Token key */
  public static final String TOKEN = "token";

  /** Token prefix */
  public static final String TOKEN_PREFIX = "Bearer ";

  /** Login user key */
  public static final String LOGIN_USER_KEY = "login_user_key";

  /** JWT user ID claim */
  public static final String JWT_USERID = "userid";

  /** JWT username claim */
  public static final String JWT_USERNAME = Claims.SUBJECT;

  /** JWT avatar claim */
  public static final String JWT_AVATAR = "avatar";

  /** JWT creation time claim */
  public static final String JWT_CREATED = "created";

  /** JWT authorities claim */
  public static final String JWT_AUTHORITIES = "authorities";

  // ---------- Resources ----------
  /** Resource mapping prefix */
  public static final String RESOURCE_PREFIX = "/profile";

  // ---------- Remote Method Invocation ----------
  /** RMI lookup prefix */
  public static final String LOOKUP_RMI = "rmi:";

  /** LDAP lookup prefix */
  public static final String LOOKUP_LDAP = "ldap:";

  /** LDAPS lookup prefix */
  public static final String LOOKUP_LDAPS = "ldaps:";

  // ---------- Security Whitelists ----------
  /** Allowed packages for JSON auto-detection (narrower scope is safer) */
  public static final String[] JSON_WHITELIST = {"com.ruoyi"};

  /** Allowed packages for scheduled jobs (extend if needed) */
  public static final String[] JOB_WHITELIST = {"com.ruoyi.quartz.task"};

  /** Disallowed classes or packages in scheduled jobs (security restriction) */
  public static final String[] JOB_ERROR_LIST = {
    "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
    "org.springframework", "org.apache", "com.ruoyi.common.utils.file",
    "com.ruoyi.common.config", "com.ruoyi.generator"
  };
}
