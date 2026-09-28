package com.srikarthik.smartcampus.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlaCalculatorTest {

    @Test
    void testUnder24Hours() {
        LocalDateTime reported = LocalDateTime.now().minusHours(12);
        LocalDateTime resolved = LocalDateTime.now();
        assertEquals("< 24 hours", SlaCalculator.calculateSlaIndicator(reported, resolved));
    }

    @Test
    void testOneToThreeDays() {
        LocalDateTime reported = LocalDateTime.now().minusDays(2);
        LocalDateTime resolved = LocalDateTime.now();
        assertEquals("1–3 days", SlaCalculator.calculateSlaIndicator(reported, resolved));
    }

    @Test
    void testThreeToSevenDays() {
        LocalDateTime reported = LocalDateTime.now().minusDays(5);
        LocalDateTime resolved = LocalDateTime.now();
        assertEquals("3–7 days", SlaCalculator.calculateSlaIndicator(reported, resolved));
    }

    @Test
    void testOverSevenDays() {
        LocalDateTime reported = LocalDateTime.now().minusDays(8);
        LocalDateTime resolved = LocalDateTime.now();
        assertEquals("7+ days", SlaCalculator.calculateSlaIndicator(reported, resolved));
    }

    @Test
    void testUnresolvedIssue() {
        LocalDateTime reported = LocalDateTime.now().minusHours(10);
        // resolvedAt is null, so it checks against current time
        assertEquals("< 24 hours", SlaCalculator.calculateSlaIndicator(reported, null));
    }
}
