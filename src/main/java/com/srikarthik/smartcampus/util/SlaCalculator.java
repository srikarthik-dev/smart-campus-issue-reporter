package com.srikarthik.smartcampus.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SlaCalculator {
    public static String calculateSlaIndicator(LocalDateTime reportedAt, LocalDateTime resolvedAt) {
        if (reportedAt == null) return "Unknown";
        
        LocalDateTime endTime = (resolvedAt != null) ? resolvedAt : LocalDateTime.now();
        long hours = ChronoUnit.HOURS.between(reportedAt, endTime);
        long days = ChronoUnit.DAYS.between(reportedAt, endTime);

        if (hours < 24) {
            return "< 24 hours";
        } else if (days >= 1 && days < 3) {
            return "1–3 days";
        } else if (days >= 3 && days < 7) {
            return "3–7 days";
        } else {
            return "7+ days";
        }
    }
}
