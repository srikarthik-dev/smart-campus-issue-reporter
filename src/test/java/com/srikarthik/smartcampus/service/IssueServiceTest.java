package com.srikarthik.smartcampus.service;

import com.srikarthik.smartcampus.dto.*;
import com.srikarthik.smartcampus.exception.InvalidWorkflowTransitionException;
import com.srikarthik.smartcampus.exception.IssueNotFoundException;
import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for IssueService using H2 in-memory database.
 * Tests real business behavior including priority calculation,
 * workflow enforcement, and search.
 */
@SpringBootTest
@Transactional
@DisplayName("IssueService Integration Tests")
class IssueServiceTest {

    @Autowired
    private IssueService issueService;

    // --- Helper ---

    private CreateIssueRequest buildRequest(String reporter, IssueCategory category,
                                             String location, int affectedUsers, boolean safety) {
        CreateIssueRequest req = new CreateIssueRequest();
        req.setReportedBy(reporter);
        req.setCategory(category);
        req.setLocation(location);
        req.setDescription("Detailed description of the campus issue that needs attention urgently.");
        req.setAffectedUsers(affectedUsers);
        req.setSafetyImpact(safety);
        return req;
    }

    // --- Issue creation and priority engine integration ---

    @Test
    @DisplayName("Created issue should have engine-calculated priority, not a default")
    void createdIssueHasCalculatedPriority() {
        CreateIssueRequest req = buildRequest("Test Student", IssueCategory.ELECTRICAL,
                "Block A Lab", 60, true);
        IssueResponse response = issueService.createIssue(req);

        assertThat(response.getPriority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(response.getPriorityScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Created issue should have a generated issue code in SCI-YYYYMMDD-XXXX format")
    void createdIssueHasValidIssueCode() {
        CreateIssueRequest req = buildRequest("Code Tester", IssueCategory.WIFI,
                "Library", 10, false);
        IssueResponse response = issueService.createIssue(req);

        assertThat(response.getIssueCode()).matches("SCI-\\d{8}-\\d{4}");
    }

    @Test
    @DisplayName("Created issue should default to OPEN status")
    void createdIssueHasOpenStatus() {
        CreateIssueRequest req = buildRequest("Status Tester", IssueCategory.CLEANLINESS,
                "Canteen", 5, false);
        IssueResponse response = issueService.createIssue(req);

        assertThat(response.getStatus()).isEqualTo(IssueStatus.OPEN);
    }

    @Test
    @DisplayName("Low-risk issue should receive LOW or MEDIUM priority")
    void lowRiskIssueHasLowPriority() {
        CreateIssueRequest req = buildRequest("Low Risk Reporter", IssueCategory.OTHER,
                "Parking Lot", 2, false);
        IssueResponse response = issueService.createIssue(req);

        assertThat(response.getPriority()).isIn(IssuePriority.LOW, IssuePriority.MEDIUM);
    }

    // --- Retrieval ---

    @Test
    @DisplayName("Getting issue by ID should return correct issue")
    void getIssueByIdReturnsCorrectIssue() {
        CreateIssueRequest req = buildRequest("Retrieve Test", IssueCategory.PLUMBING,
                "Hostel B", 15, false);
        IssueResponse created = issueService.createIssue(req);

        IssueResponse fetched = issueService.getIssueById(created.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getIssueCode()).isEqualTo(created.getIssueCode());
    }

    @Test
    @DisplayName("Getting non-existent issue should throw IssueNotFoundException")
    void nonExistentIssueThrowsException() {
        assertThatThrownBy(() -> issueService.getIssueById(999999L))
                .isInstanceOf(IssueNotFoundException.class);
    }

    // --- Workflow transitions ---

    @Test
    @DisplayName("Assigning an OPEN issue should transition it to ASSIGNED")
    void assigningOpenIssueMovesToAssigned() {
        IssueResponse created = issueService.createIssue(
                buildRequest("Workflow Test", IssueCategory.SAFETY, "Block C", 10, true));

        AssignIssueRequest assign = new AssignIssueRequest();
        assign.setAssignedTo("Maintenance Staff");
        IssueResponse assigned = issueService.assignIssue(created.getId(), assign);

        assertThat(assigned.getStatus()).isEqualTo(IssueStatus.ASSIGNED);
        assertThat(assigned.getAssignedTo()).isEqualTo("Maintenance Staff");
    }

    @Test
    @DisplayName("Full lifecycle: OPEN → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED")
    void fullLifecycleTransitionsSuccessfully() {
        IssueResponse issue = issueService.createIssue(
                buildRequest("Lifecycle User", IssueCategory.ELECTRICAL, "Lab 2", 30, false));

        // OPEN → ASSIGNED
        AssignIssueRequest assign = new AssignIssueRequest();
        assign.setAssignedTo("Engineer A");
        issue = issueService.assignIssue(issue.getId(), assign);
        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ASSIGNED);

        // ASSIGNED → IN_PROGRESS
        UpdateStatusRequest inProgress = new UpdateStatusRequest();
        inProgress.setStatus(IssueStatus.IN_PROGRESS);
        issue = issueService.updateStatus(issue.getId(), inProgress);
        assertThat(issue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);

        // IN_PROGRESS → RESOLVED
        ResolveIssueRequest resolve = new ResolveIssueRequest();
        resolve.setResolutionNotes("Issue fixed by replacing faulty component.");
        issue = issueService.resolveIssue(issue.getId(), resolve);
        assertThat(issue.getStatus()).isEqualTo(IssueStatus.RESOLVED);
        assertThat(issue.getResolvedAt()).isNotNull();

        // RESOLVED → CLOSED
        UpdateStatusRequest close = new UpdateStatusRequest();
        close.setStatus(IssueStatus.CLOSED);
        issue = issueService.updateStatus(issue.getId(), close);
        assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
    }

    @Test
    @DisplayName("Attempting to resolve an OPEN issue should throw InvalidWorkflowTransitionException")
    void resolvingOpenIssueThrowsException() {
        IssueResponse created = issueService.createIssue(
                buildRequest("Bad Transition", IssueCategory.WIFI, "Library", 20, false));

        ResolveIssueRequest resolve = new ResolveIssueRequest();
        resolve.setResolutionNotes("This should not work.");

        assertThatThrownBy(() -> issueService.resolveIssue(created.getId(), resolve))
                .isInstanceOf(InvalidWorkflowTransitionException.class);
    }

    @Test
    @DisplayName("Attempting to transition CLOSED → any state should throw InvalidWorkflowTransitionException")
    void transitioningClosedIssueThrowsException() {
        IssueResponse issue = issueService.createIssue(
                buildRequest("Closed Test", IssueCategory.OTHER, "Parking", 2, false));

        // Walk through full lifecycle
        AssignIssueRequest assign = new AssignIssueRequest();
        assign.setAssignedTo("Staff X");
        issue = issueService.assignIssue(issue.getId(), assign);

        UpdateStatusRequest inProgress = new UpdateStatusRequest();
        inProgress.setStatus(IssueStatus.IN_PROGRESS);
        issue = issueService.updateStatus(issue.getId(), inProgress);

        ResolveIssueRequest resolve = new ResolveIssueRequest();
        resolve.setResolutionNotes("Fixed the parking issue.");
        issue = issueService.resolveIssue(issue.getId(), resolve);

        UpdateStatusRequest close = new UpdateStatusRequest();
        close.setStatus(IssueStatus.CLOSED);
        issue = issueService.updateStatus(issue.getId(), close);

        // Now try to re-open — should fail
        final Long closedId = issue.getId();
        UpdateStatusRequest reopen = new UpdateStatusRequest();
        reopen.setStatus(IssueStatus.OPEN);
        assertThatThrownBy(() -> issueService.updateStatus(closedId, reopen))
                .isInstanceOf(InvalidWorkflowTransitionException.class);
    }

    // --- Search and filter ---

    @Test
    @DisplayName("Searching by category should return only matching issues")
    void searchByCategoryFiltersCorrectly() {
        issueService.createIssue(buildRequest("Search User 1", IssueCategory.WIFI, "Hostel", 10, false));
        issueService.createIssue(buildRequest("Search User 2", IssueCategory.WIFI, "Library", 20, false));
        issueService.createIssue(buildRequest("Search User 3", IssueCategory.PLUMBING, "Block B", 5, false));

        List<IssueResponse> results = issueService.searchIssues(
                IssueCategory.WIFI, null, null, null, null, null, null, null);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getCategory() == IssueCategory.WIFI);
    }

    @Test
    @DisplayName("Searching by keyword should match description text")
    void searchByKeywordMatchesDescription() {
        CreateIssueRequest req = buildRequest("Keyword Test", IssueCategory.ELECTRICAL, "Lab 5", 8, false);
        req.setDescription("The circuit breaker keeps tripping every morning at startup.");
        issueService.createIssue(req);

        List<IssueResponse> results = issueService.searchIssues(
                null, null, null, null, null, "circuit breaker", null, null);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(r -> r.getDescription().toLowerCase().contains("circuit breaker"));
    }

    // --- Delete ---

    @Test
    @DisplayName("Deleting an existing issue should remove it")
    void deleteIssueRemovesIt() {
        IssueResponse created = issueService.createIssue(
                buildRequest("Delete Me", IssueCategory.CLEANLINESS, "Corridor", 3, false));
        Long id = created.getId();

        issueService.deleteIssue(id);

        assertThatThrownBy(() -> issueService.getIssueById(id))
                .isInstanceOf(IssueNotFoundException.class);
    }

    @Test
    @DisplayName("Deleting a non-existent issue should throw IssueNotFoundException")
    void deleteNonExistentIssueThrowsException() {
        assertThatThrownBy(() -> issueService.deleteIssue(999999L))
                .isInstanceOf(IssueNotFoundException.class);
    }
}
