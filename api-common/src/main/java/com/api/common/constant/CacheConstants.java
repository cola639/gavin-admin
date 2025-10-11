package com.api.common.constant;

/** Cache key constants */
public class CacheConstants {
  /** user redis key */
  public static final String LOGIN_TOKEN_KEY = "login_user_tokens:";

  /** captcha redis key */
  public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

  /** parameters cache key */
  public static final String SYS_CONFIG_KEY = "sys_config:";

  /** dict cache key */
  public static final String SYS_DICT_KEY = "sys_dict:";

  /** repeat redis key */
  public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

  /** limit redis key */
  public static final String RATE_LIMIT_KEY = "rate_limit:";

  /** max try counts redis key */
  public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

  /** monitor uri redis key */
  public static final String MONITOR_URI_KEY = "metrics:endpoint:*";
}
