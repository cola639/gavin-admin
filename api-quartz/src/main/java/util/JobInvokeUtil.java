package util;

import com.api.common.utils.StringUtils;
import com.api.common.utils.springUtils.SpringUtils;
import domain.SysJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/** Utility for dynamically invoking scheduled job methods via reflection. */
@Slf4j
@Component
public final class JobInvokeUtil {

  private JobInvokeUtil() {}

  /** Invokes the target method specified in SysJob configuration. */
  public static void invokeMethod(SysJob sysJob) throws Exception {
    String invokeTarget = sysJob.getInvokeTarget();
    String beanName = getBeanName(invokeTarget);
    String methodName = getMethodName(invokeTarget);
    List<Object[]> params = getMethodParams(invokeTarget);

    Object bean =
        isValidClassName(beanName)
            ? Class.forName(beanName).getDeclaredConstructor().newInstance()
            : SpringUtils.getBean(beanName);

    invoke(bean, methodName, params);
  }

  /** Reflectively invokes the target method. */
  private static void invoke(Object bean, String methodName, List<Object[]> params)
      throws Exception {
    if (params != null && !params.isEmpty()) {
      Method method = bean.getClass().getMethod(methodName, getParamTypes(params));
      method.invoke(bean, getParamValues(params));
    } else {
      Method method = bean.getClass().getMethod(methodName);
      method.invoke(bean);
    }
  }

  /** Determines whether the name represents a class path or a Spring bean. */
  private static boolean isValidClassName(String invokeTarget) {
    return StringUtils.countMatches(invokeTarget, ".") > 1;
  }

  private static String getBeanName(String invokeTarget) {
    return StringUtils.substringBeforeLast(StringUtils.substringBefore(invokeTarget, "("), ".");
  }

  private static String getMethodName(String invokeTarget) {
    return StringUtils.substringAfterLast(StringUtils.substringBefore(invokeTarget, "("), ".");
  }

  private static List<Object[]> getMethodParams(String invokeTarget) {
    String paramStr = StringUtils.substringBetween(invokeTarget, "(", ")");
    if (StringUtils.isEmpty(paramStr)) return null;

    String[] paramArray = paramStr.split(",(?=([^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
    List<Object[]> params = new LinkedList<>();

    for (String raw : paramArray) {
      String str = StringUtils.trimToEmpty(raw);
      if (StringUtils.startsWithAny(str, "'", "\"")) {
        params.add(new Object[] {StringUtils.substring(str, 1, str.length() - 1), String.class});
      } else if ("true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str)) {
        params.add(new Object[] {Boolean.valueOf(str), Boolean.class});
      } else if (StringUtils.endsWith(str, "L")) {
        params.add(new Object[] {Long.valueOf(str.substring(0, str.length() - 1)), Long.class});
      } else if (StringUtils.endsWith(str, "D")) {
        params.add(new Object[] {Double.valueOf(str.substring(0, str.length() - 1)), Double.class});
      } else {
        params.add(new Object[] {Integer.valueOf(str), Integer.class});
      }
    }
    return params;
  }

  private static Class<?>[] getParamTypes(List<Object[]> params) {
    return params.stream().map(p -> (Class<?>) p[1]).toArray(Class[]::new);
  }

  private static Object[] getParamValues(List<Object[]> params) {
    return params.stream().map(p -> p[0]).toArray();
  }
}
