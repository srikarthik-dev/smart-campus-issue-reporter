package com.srikarthik.smartcampus.util;

import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * PriorityEngine — core business logic that automatically determines the priority
 * of a reported campus issue based on multiple weighted factors.
 *
 * Scoring breakdown (max 100 points):
 *   Safety Impact:      0 or 40 points
 *   Affected Users:     0–25 points  (tiered: <5=5, 5-19=10, 20-49=18, 50+=25)
 *   Category Risk:      0–20 points  (SAFETY/ELECTRICAL=20, PLUMBING=15, WIFI/CLASSROOM=10, OTHER=5)
 *   Age of Issue:       0–15 points  (increases every 24h until capped at day 7)
 *
 * Score → Priority mapping:
 *   75–100  → CRITICAL
 *   50–74   → HIGH
 *   25–49   → MEDIUM
 *   0–24    → LOW
 *
 * This design makes the algorithm explainable in one minute during a placement interview.
 */
@Component
public class PriorityEngine {

    // Score boundaries
    private static final int CRITICAL_THRESHOLD = 75;
    private static final int HIGH_THRESHOLD = 50;
    private static final int MEDIUM_THRESHOLD = 25;

    // Factor weights
    private static final int SAFETY_IMPACT_SCORE = 40;
    private static final int MAX_USER_SCORE = 25;
    private static final int MAX_CATEGORY_SCORE = 20;
    private static final int MAX_AGE_SCORE = 15;

    /**
     * Calculates the priority for a new issue that has not yet been persisted.
     * Age is zero since the issue was just reported.
     */
    public PriorityResult calculate(boolean safetyImpact, int affectedUsers, IssueCategory category) {
        return calculate(safetyImpact, affectedUsers, category, LocalDateTime.now());
    }

    /**
     * Calculates the priority for an existing issue, factoring in how long it has been unresolved.
     */
    public PriorityResult calculate(boolean safetyImpact, int affectedUsers, IssueCategory category,
                                    LocalDateTime reportedAt) {
        int safetyScore = computeSafetyScore(safetyImpact);
        int userScore = computeUserScore(affectedUsers);
        int categoryScore = computeCategoryScore(category);
        int ageScore = computeAgeScore(reportedAt);

        int totalScore = safetyScore + userScore + categoryScore + ageScore;
        IssuePriority priority = scoreToPriority(totalScore);

        return new PriorityResult(priority, totalScore, safetyScore, userScore, categoryScore, ageScore);
    }

    // --- Factor computation methods ---

    private int computeSafetyScore(boolean safetyImpact) {
        // Safety issues are treated with highest urgency — 40 points if safety risk present
        return safetyImpact ? SAFETY_IMPACT_SCORE : 0;
    }

    private int computeUserScore(int affectedUsers) {
        // Tiered scoring based on how many people are impacted
        if (affectedUsers >= 50) return MAX_USER_SCORE;        // 25 points — major disruption
        if (affectedUsers >= 20) return 18;                    // 18 points — significant disruption
        if (affectedUsers >= 5)  return 10;                    // 10 points — moderate disruption
        return 5;                                              // 5  points — minor disruption
    }

    private int computeCategoryScore(IssueCategory category) {
        // Electrical and safety categories inherently carry higher risk
        return switch (category) {
            case SAFETY, ELECTRICAL -> 20;
            case PLUMBING            -> 15;
            case WIFI, CLASSROOM     -> 10;
            case CLEANLINESS         -> 7;
            case OTHER               -> 5;
        };
    }

    private int computeAgeScore(LocalDateTime reportedAt) {
        // Issues that remain unresolved accumulate urgency over time, capped at 7 days
        long hoursOld = ChronoUnit.HOURS.between(reportedAt, LocalDateTime.now());
        long daysOld = hoursOld / 24;

        if (daysOld >= 7) return MAX_AGE_SCORE;   // 15 points — week-old issue needs attention
        if (daysOld >= 3) return 10;               // 10 points — 3+ days unresolved
        if (daysOld >= 1) return 5;                // 5 points — 1+ day unresolved
        return 0;                                  // 0 points — reported today
    }

    private IssuePriority scoreToPriority(int score) {
        if (score >= CRITICAL_THRESHOLD) return IssuePriority.CRITICAL;
        if (score >= HIGH_THRESHOLD)     return IssuePriority.HIGH;
        if (score >= MEDIUM_THRESHOLD)   return IssuePriority.MEDIUM;
        return IssuePriority.LOW;
    }

    /**
     * Immutable result object carrying the computed priority, total score,
     * and individual factor scores for full transparency.
     */
    public static class PriorityResult {
        private final IssuePriority priority;
        private final int totalScore;
        private final int safetyScore;
        private final int userScore;
        private final int categoryScore;
        private final int ageScore;

        public PriorityResult(IssuePriority priority, int totalScore,
                              int safetyScore, int userScore, int categoryScore, int ageScore) {
            this.priority = priority;
            this.totalScore = totalScore;
            this.safetyScore = safetyScore;
            this.userScore = userScore;
            this.categoryScore = categoryScore;
            this.ageScore = ageScore;
        }

        public IssuePriority getPriority() { return priority; }
        public int getTotalScore() { return totalScore; }
        public int getSafetyScore() { return safetyScore; }
        public int getUserScore() { return userScore; }
        public int getCategoryScore() { return categoryScore; }
        public int getAgeScore() { return ageScore; }

        @Override
        public String toString() {
            return String.format("Priority=%s Score=%d [safety=%d, users=%d, category=%d, age=%d]",
                    priority, totalScore, safetyScore, userScore, categoryScore, ageScore);
        }
    }
}
