package com.example.ridenow.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    /**
     * Formats date and time from ISO string to DD/MM/YYYY format
     * @param isoDateTimeString ISO datetime string (e.g., "2025-12-12T14:30:00")
     * @return Formatted date string in DD/MM/YYYY format
     */
    public static String formatDateFromISO(String isoDateTimeString) {
        if (isoDateTimeString == null || isoDateTimeString.trim().isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date date = inputFormat.parse(isoDateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Try alternative format without 'T'
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                Date date = inputFormat.parse(isoDateTimeString);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return isoDateTimeString; // Return original if parsing fails
            }
        }
    }

    /**
     * Formats time from ISO string to HH:MM format
     * @param isoDateTimeString ISO datetime string (e.g., "2025-12-12T14:30:00")
     * @return Formatted time string in HH:MM format
     */
    public static String formatTimeFromISO(String isoDateTimeString) {
        if (isoDateTimeString == null || isoDateTimeString.trim().isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(isoDateTimeString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Try alternative format without 'T'
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                Date date = inputFormat.parse(isoDateTimeString);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return "N/A"; // Return N/A if parsing fails
            }
        }
    }

    /**
     * Calculates duration in minutes between start and end time
     * @param startTimeISO Start time as ISO string
     * @param endTimeISO End time as ISO string
     * @return Duration in minutes, or 0 if parsing fails
     */
    public static long calculateDurationMinutes(String startTimeISO, String endTimeISO) {
        if (startTimeISO == null || endTimeISO == null ||
            startTimeISO.trim().isEmpty() || endTimeISO.trim().isEmpty()) {
            return 0;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

            Date startTime = inputFormat.parse(startTimeISO);
            Date endTime = inputFormat.parse(endTimeISO);

            long durationMs = endTime.getTime() - startTime.getTime();
            return Math.round(durationMs / (1000.0 * 60.0)); // Convert to minutes
        } catch (ParseException e) {
            // Try alternative format without 'T'
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                Date startTime = inputFormat.parse(startTimeISO);
                Date endTime = inputFormat.parse(endTimeISO);

                long durationMs = endTime.getTime() - startTime.getTime();
                return Math.round(durationMs / (1000.0 * 60.0)); // Convert to minutes
            } catch (ParseException e2) {
                return 0; // Return 0 if parsing fails
            }
        }
    }

    /**
     * Creates a time range string from start and end times (HH:MM - HH:MM)
     * @param startTimeISO Start time as ISO string
     * @param endTimeISO End time as ISO string
     * @return Time range string in HH:MM - HH:MM format
     */
    public static String formatTimeRange(String startTimeISO, String endTimeISO) {
        String startTime = formatTimeFromISO(startTimeISO);
        String endTime = formatTimeFromISO(endTimeISO);

        if ("N/A".equals(startTime) || "N/A".equals(endTime)) {
            return "N/A";
        }

        return startTime + " - " + endTime;
    }

    /**
     * Formats LocalDateTime to a readable date and time string
     * @param dateTime LocalDateTime to format
     * @return Formatted date time string (e.g., "Dec 26, 2025 at 14:30")
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return dateTime.format(formatter);
    }
}
