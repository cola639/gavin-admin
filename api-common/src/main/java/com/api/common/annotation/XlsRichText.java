package com.api.common.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface XlsRichText {
    String text();
    String color() default "#000000";
    short fontSize() default 10;
    boolean bold() default false;
    boolean italic() default false;

    /** margin right, simulate space after text (unit: spaces ≈ ~px) */
    int marginRight() default 0;
}

