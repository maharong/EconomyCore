package com.github.maharong.economycore.util;

public class FormatUtil {
    // 천 단위 콤마링
    public static String format(double amount) {
        return String.format("%,.2f", amount);
    }
}
