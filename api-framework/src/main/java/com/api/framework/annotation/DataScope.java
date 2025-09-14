package com.api.framework.annotation;

import java.lang.annotation.*;

/**
 * Data permission filter annotation for JPA
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /** Department alias (compatibility only) */
    String deptAlias() default "";

    /** User alias (compatibility only) */
    String userAlias() default "";

    /** Permission string (used to match roles, optional) */
    String permission() default "";
}
