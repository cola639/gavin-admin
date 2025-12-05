package com.api.common.annotation;

import java.lang.annotation.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Xls {
    String name() default "";
    int order() default Integer.MAX_VALUE;
    int width() default 16;
    int height() default -1;
    String dateFormat() default "";
    String converterExp() default "";
    String dict() default "";
    HorizontalAlignment align() default HorizontalAlignment.CENTER;
    IndexedColors headerBg() default IndexedColors.GREY_50_PERCENT;
    IndexedColors headerFont() default IndexedColors.WHITE;

    /** ===== Data Cell Font Config ===== */
    short fontSize() default 10;
    boolean bold() default false;
    String fontFamily() default "Arial";

    /** ===== Header Cell Font Config ===== */
    short headerFontSize() default 10;
    boolean headerBold() default true;
    String headerFontFamily() default "Arial";

    /** ===== Rich Text Header ===== */
    XlsRichText[] headerRichText() default {};

    /** ===== Extra Config ===== */
    boolean merge() default false;  // 👈 new: whether to merge same value cells vertically
    String type() default "";       // 👈 optional: special type like "url"
}

