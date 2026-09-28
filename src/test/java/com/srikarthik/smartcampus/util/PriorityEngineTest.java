package com.srikarthik.smartcampus.util;

import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PriorityEngine Tests")
class PriorityEngineTest {

    private PriorityEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PriorityEngine();
    }

    // --- Safety impact escalation ---

    @Test
    @DisplayName("Safety impact alone should push score to at least MEDIUM")
    void safetyImpactEscalatesScore() {
        PriorityEngine.PriorityResult result = engine.calculate(true, 1, IssueCategory.OTHER);
        assertThat(result.getSafetyScore()).isEqualTo(40);
        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(40);
    }

    @Test
    @DisplayName("Safety + ELECTRICAL + 50+ users should produce CRITICAL")
    void safetyWithElectricalAndManyUsersIsCritical() {
        PriorityEngine.PriorityResult result = engine.calculate(true, 60, IssueCategory.ELECTRICAL);
        assertThat(result.getPriority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(75);
    }

    @Test
    @DisplayName("No safety impact, single user, OTHER category should produce LOW")
    void noRiskProducesLow() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.OTHER);
        assertThat(result.getPriority()).isEqualTo(IssuePriority.LOW);
    }

    // --- Affected users tiers ---

    @Test
    @DisplayName("50+ affected users should give maximum user score (25)")
    void fiftyPlusUsersMaxUserScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 50, IssueCategory.OTHER);
        assertThat(result.getUserScore()).isEqualTo(25);
    }

    @Test
    @DisplayName("20-49 affected users should give user score of 18")
    void twentyToFortyNineUsersScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 25, IssueCategory.OTHER);
        assertThat(result.getUserScore()).isEqualTo(18);
    }

    @Test
    @DisplayName("5-19 affected users should give user score of 10")
    void fiveToNineteenUsersScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 10, IssueCategory.OTHER);
        assertThat(result.getUserScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("Under 5 affected users should give user score of 5")
    void underFiveUsersScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 3, IssueCategory.OTHER);
        assertThat(result.getUserScore()).isEqualTo(5);
    }

    // --- Category scores ---

    @Test
    @DisplayName("SAFETY category should receive maximum category score (20)")
    void safetyCategoryMaxScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.SAFETY);
        assertThat(result.getCategoryScore()).isEqualTo(20);
    }

    @Test
    @DisplayName("ELECTRICAL category should receive maximum category score (20)")
    void electricalCategoryMaxScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.ELECTRICAL);
        assertThat(result.getCategoryScore()).isEqualTo(20);
    }

    @Test
    @DisplayName("PLUMBING category should receive category score of 15")
    void plumbingCategoryScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.PLUMBING);
        assertThat(result.getCategoryScore()).isEqualTo(15);
    }

    @Test
    @DisplayName("WIFI and CLASSROOM categories should receive category score of 10")
    void wifiAndClassroomCategoryScore() {
        PriorityEngine.PriorityResult resultWifi = engine.calculate(false, 1, IssueCategory.WIFI);
        PriorityEngine.PriorityResult resultClass = engine.calculate(false, 1, IssueCategory.CLASSROOM);
        assertThat(resultWifi.getCategoryScore()).isEqualTo(10);
        assertThat(resultClass.getCategoryScore()).isEqualTo(10);
    }

    // --- Age-based escalation ---

    @Test
    @DisplayName("Issue reported today should have zero age score")
    void issueReportedTodayHasZeroAgeScore() {
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.OTHER, LocalDateTime.now());
        assertThat(result.getAgeScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Issue reported 2 days ago should have age score of 5")
    void issueTwoDaysOldHasAgeScoreFive() {
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.OTHER, twoDaysAgo);
        assertThat(result.getAgeScore()).isEqualTo(5);
    }

    @Test
    @DisplayName("Issue reported 4 days ago should have age score of 10")
    void issueFourDaysOldHasAgeScoreTen() {
        LocalDateTime fourDaysAgo = LocalDateTime.now().minusDays(4);
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.OTHER, fourDaysAgo);
        assertThat(result.getAgeScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("Issue 7+ days old should have maximum age score (15)")
    void issueWeekOldHasMaxAgeScore() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        PriorityEngine.PriorityResult result = engine.calculate(false, 1, IssueCategory.OTHER, sevenDaysAgo);
        assertThat(result.getAgeScore()).isEqualTo(15);
    }

    // --- Priority boundaries ---

    @Test
    @DisplayName("Score >= 75 should map to CRITICAL")
    void highScoreIsCritical() {
        // Safety(40) + 50+users(25) + ELECTRICAL(20) = 85 → CRITICAL
        PriorityEngine.PriorityResult result = engine.calculate(true, 60, IssueCategory.ELECTRICAL);
        assertThat(result.getPriority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(result.getTotalScore()).isGreaterThanOrEqualTo(75);
    }

    @Test
    @DisplayName("Score 50-74 should map to HIGH")
    void mediumHighScoreIsHigh() {
        // No safety + 50+users(25) + ELECTRICAL(20) = 45 → MEDIUM (barely below HIGH)
        // With WIFI: 25 + 10 = 35 → MEDIUM. With more users + age:
        // No safety + 50+users(25) + SAFETY-category(20) + 3-day-age(10) = 55 → HIGH
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        PriorityEngine.PriorityResult result = engine.calculate(false, 50, IssueCategory.SAFETY, threeDaysAgo);
        assertThat(result.getPriority()).isEqualTo(IssuePriority.HIGH);
        assertThat(result.getTotalScore()).isBetween(50, 74);
    }

    @Test
    @DisplayName("Total score components should sum correctly")
    void scoreComponentsSumCorrectly() {
        PriorityEngine.PriorityResult result = engine.calculate(true, 25, IssueCategory.PLUMBING, LocalDateTime.now());
        int expectedTotal = result.getSafetyScore() + result.getUserScore()
                + result.getCategoryScore() + result.getAgeScore();
        assertThat(result.getTotalScore()).isEqualTo(expectedTotal);
    }
}
