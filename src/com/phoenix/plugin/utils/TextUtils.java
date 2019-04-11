package com.phoenix.plugin.utils;

public class TextUtils {
    /**
     * 字符串空判断
     */
    public static boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }
}
