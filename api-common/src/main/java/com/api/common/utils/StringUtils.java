package com.api.common.utils;

import java.util.*;
import java.util.stream.Collectors;

import com.api.common.constant.Constants;
import org.springframework.util.AntPathMatcher;



/**
 * Utility class for String operations.
 * Extends Apache Commons Lang3 {@link org.apache.commons.lang3.StringUtils}
 * with additional features for collections, arrays, masking,
 * case conversion, path matching, and formatting.
 *
 * @author
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /** Empty string constant */
    private static final String NULLSTR = "";

    /** Underscore separator */
    private static final char SEPARATOR = '_';

    /** Masking character */
    private static final char ASTERISK = '*';

    /**
     * Returns the value if not null, otherwise returns the default.
     *
     * @param value        value to check
     * @param defaultValue fallback if value is null
     * @return non-null value
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    // ---------- Collection & Array Checks ----------

    /** Checks if a collection is empty (null or no elements). */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /** Checks if a collection is not empty. */
    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /** Checks if an array is empty (null or zero length). */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /** Checks if an array is not empty. */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /** Checks if a map is empty. */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /** Checks if a map is not empty. */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    // ---------- String Checks ----------

    /** Checks if a string is empty (null or blank after trim). */
    public static boolean isEmpty(String str) {
        return str == null || NULLSTR.equals(str.trim());
    }

    /** Checks if a string is not empty. */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /** Checks if an object is null. */
    public static boolean isNull(Object obj) {
        return obj == null;
    }

    /** Checks if an object is not null. */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /** Checks if an object is an array type. */
    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    /** Trim string safely (null → ""). */
    public static String trim(String str) {
        return str == null ? NULLSTR : str.trim();
    }

    // ---------- String Transformations ----------

    /**
     * Masks part of the string with '*'.
     *
     * @param str         source string
     * @param startIndex  start index (inclusive)
     * @param endIndex    end index (exclusive)
     * @return masked string
     */
    public static String hide(CharSequence str, int startIndex, int endIndex) {
        if (isEmpty(str)) return NULLSTR;
        int length = str.length();

        if (startIndex > length) return NULLSTR;
        if (endIndex > length) endIndex = length;
        if (startIndex > endIndex) return NULLSTR;

        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (i >= startIndex && i < endIndex) ? ASTERISK : str.charAt(i);
        }
        return new String(chars);
    }

    /** Extract substring from start index. */
    public static String substring(final String str, int start) {
        if (str == null) return NULLSTR;
        if (start < 0) start = str.length() + start;
        if (start < 0) start = 0;
        if (start > str.length()) return NULLSTR;
        return str.substring(start);
    }

    /** Extract substring between start and end indices. */
    public static String substring(final String str, int start, int end) {
        if (str == null) return NULLSTR;
        if (end < 0) end = str.length() + end;
        if (start < 0) start = str.length() + start;
        if (end > str.length()) end = str.length();
        if (start > end) return NULLSTR;
        return str.substring(Math.max(start, 0), Math.max(end, 0));
    }

    /** Extract substring between first occurrence of `open` and last occurrence of `close`. */
    public static String substringBetweenLast(final String str, final String open, final String close) {
        if (isEmpty(str) || isEmpty(open) || isEmpty(close)) return NULLSTR;
        int start = str.indexOf(open);
        int end = str.lastIndexOf(close);
        return (start != INDEX_NOT_FOUND && end != INDEX_NOT_FOUND)
                ? str.substring(start + open.length(), end)
                : NULLSTR;
    }

    /** Checks if string contains non-whitespace characters. */
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    // ---------- Formatters & Conversions ----------

    /** Format string with `{}` placeholders. */
    public static String format(String template, Object... params) {
        if (isEmpty(params) || isEmpty(template)) return template;
        return StrFormatter.format(template, params);
    }

    /** Checks if URL starts with http(s). */
    public static boolean isHttp(String link) {
        return startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /** Convert string to set. */
    public static Set<String> str2Set(String str, String sep) {
        return new HashSet<>(str2List(str, sep, true, false));
    }

    /** Convert string to list. */
    public static List<String> str2List(String str, String sep) {
        return str2List(str, sep, true, false);
    }

    /** Convert string to list with options for blank filtering and trimming. */
    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        if (isEmpty(str)) return Collections.emptyList();
        return Arrays.stream(str.split(sep))
                .map(s -> trim ? s.trim() : s)
                .filter(s -> !(filterBlank && isBlank(s)))
                .collect(Collectors.toList());
    }

    // ---------- Matching ----------

    /** Checks if collection contains any of given array elements. */
    public static boolean containsAny(Collection<String> collection, String... array) {
        return collection != null && array != null &&
                Arrays.stream(array).anyMatch(collection::contains);
    }

    /** Checks if string contains any of given substrings (ignore case). */
    public static boolean containsAnyIgnoreCase(CharSequence cs, CharSequence... searchSeqs) {
        return cs != null && searchSeqs != null &&
                Arrays.stream(searchSeqs).anyMatch(seq -> containsIgnoreCase(cs, seq));
    }

    /** Convert camelCase to underscore_case. */
    public static String toUnderScoreCase(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0 && Character.isUpperCase(c) &&
                    (!Character.isUpperCase(str.charAt(i - 1)) ||
                            (i + 1 < str.length() && !Character.isUpperCase(str.charAt(i + 1))))) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    /** Check if string matches any in array (ignore case). */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        return str != null && strs != null &&
                Arrays.stream(strs).anyMatch(s -> str.equalsIgnoreCase(trim(s)));
    }

    /** Convert UPPER_UNDERSCORE to CamelCase. */
    public static String convertToCamelCase(String name) {
        if (isEmpty(name)) return "";
        if (!name.contains("_")) return capitalize(name);
        return Arrays.stream(name.split("_"))
                .filter(s -> !s.isEmpty())
                .map(s -> capitalize(s.toLowerCase()))
                .collect(Collectors.joining());
    }

    /** Convert under_score to camelCase. */
    public static String toCamelCase(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : s.toLowerCase().toCharArray()) {
            if (c == SEPARATOR) {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    /** Check if string matches any AntPath pattern. */
    public static boolean matches(String str, List<String> patterns) {
        return str != null && patterns != null &&
                patterns.stream().anyMatch(p -> isMatch(p, str));
    }

    /** Ant-style path matcher. */
    public static boolean isMatch(String pattern, String url) {
        return new AntPathMatcher().match(pattern, url);
    }

    /** Cast object with suppression of warnings. */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    // ---------- Padding ----------

    /** Left pad number with '0'. */
    public static String padl(final Number num, final int size) {
        return padl(num.toString(), size, '0');
    }

    /** Left pad string with custom char. */
    public static String padl(final String s, final int size, final char c) {
        if (s == null) return String.valueOf(c).repeat(size);
        int len = s.length();
        return len <= size
                ? String.valueOf(c).repeat(size - len) + s
                : s.substring(len - size);
    }
}
