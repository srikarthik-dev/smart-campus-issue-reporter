package com.srikarthik.smartcampus.service;

import com.srikarthik.smartcampus.dto.CreateIssueRequest;
import com.srikarthik.smartcampus.dto.DashboardSummaryResponse;
import com.srikarthik.smartcampus.model.IssueCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private IssueService issueService;

    @Test
    @DisplayName("Summary should reflect real database counts")
    void summaryReflectsDatabaseCounts() {
        DashboardSummaryResponse before = dashboardService.getSummary();
        long initialTotal = before.getTotalIssues();
        long initialOpen = before.getOpenIssues();

        // Add a new OPEN issue
        CreateIssueRequest req = new CreateIssueRequest();
        req.setReportedBy("Dashboard Tester");
        req.setCategory(IssueCategory.WIFI);
        req.setLocation("Test Location");
        req.setDescription("Dashboard integration test — verifying live counts.");
        req.setAffectedUsers(10);
        req.setSafetyImpact(false);
        issueService.createIssue(req);

        DashboardSummaryResponse after = dashboardService.getSummary();
        assertThat(after.getTotalIssues()).isEqualTo(initialTotal + 1);
        assertThat(after.getOpenIssues()).isEqualTo(initialOpen + 1);
    }

    @Test
    @DisplayName("By-category map should contain all categories")
    void byCategoryContainsAllCategories() {
        var map = dashboardService.getByCategory();
        assertThat(map).containsKeys("ELECTRICAL", "PLUMBING", "CLASSROOM",
                "WIFI", "CLEANLINESS", "SAFETY", "OTHER");
    }

    @Test
    @DisplayName("By-status map should contain all statuses")
    void byStatusContainsAllStatuses() {
        var map = dashboardService.getByStatus();
        assertThat(map).containsKeys("OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED");
    }

    @Test
    @DisplayName("By-priority map should contain all priorities")
    void byPriorityContainsAllPriorities() {
        var map = dashboardService.getByPriority();
        assertThat(map).containsKeys("LOW", "MEDIUM", "HIGH", "CRITICAL");
    }
}
