package com.example.fitbook;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateFormatUtils {
    private DateFormatUtils() {
    }

    public static String formatRussianDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) {
            return "—";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = input.parse(isoDate.trim());
            if (date == null) {
                return isoDate;
            }
            SimpleDateFormat output = new SimpleDateFormat("d MMMM, yyyy", new Locale("ru"));
            return output.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
