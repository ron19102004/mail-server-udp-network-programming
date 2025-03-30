package com.ronial.app.utils;

public class StringUtils {
    public static String truncateString(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }

        return input.substring(0, maxLength) + "...";
    }
}
