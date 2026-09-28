package com.srikarthik.smartcampus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique, human-readable issue codes.
 *
 * Format: SCI-YYYYMMDD-XXXX
 *   SCI  = Smart Campus Issue prefix
 *   YYYYMMDD = date of creation
 *   XXXX = 4-digit random suffix for uniqueness
 *
 * Example: SCI-20260928-4721
 */
public class IssueCodeGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private IssueCodeGenerator() {}

    public static String generate() {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        int suffix = ThreadLocalRandom.current().nextInt(1000, 9999);
        return String.format("SCI-%s-%d", date, suffix);
    }
}
