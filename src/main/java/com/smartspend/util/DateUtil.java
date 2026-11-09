package com.smartspend.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for Date formatting and operations.
 * Demonstrates static utility methods and modern Java Time API.
 */
public class DateUtil {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter MONTH_HEADER_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMATTER);
    }

    public static String getCurrentYearMonth() {
        return LocalDate.now().format(MONTH_FORMATTER);
    }

    public static String formatMonthHeader(String yearMonth) {
        try {
            YearMonth ym = YearMonth.parse(yearMonth, MONTH_FORMATTER);
            return ym.format(MONTH_HEADER_FORMATTER);
        } catch (DateTimeParseException e) {
            return yearMonth;
        }
    }
}
