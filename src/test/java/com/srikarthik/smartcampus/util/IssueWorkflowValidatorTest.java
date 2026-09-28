package com.srikarthik.smartcampus.util;

import com.srikarthik.smartcampus.model.IssueStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IssueWorkflowValidator Tests")
class IssueWorkflowValidatorTest {

    @Test
    @DisplayName("OPEN → ASSIGNED should be valid")
    void openToAssignedIsValid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.OPEN, IssueStatus.ASSIGNED)).isTrue();
    }

    @Test
    @DisplayName("ASSIGNED → IN_PROGRESS should be valid")
    void assignedToInProgressIsValid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.ASSIGNED, IssueStatus.IN_PROGRESS)).isTrue();
    }

    @Test
    @DisplayName("IN_PROGRESS → RESOLVED should be valid")
    void inProgressToResolvedIsValid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.IN_PROGRESS, IssueStatus.RESOLVED)).isTrue();
    }

    @Test
    @DisplayName("RESOLVED → CLOSED should be valid")
    void resolvedToClosedIsValid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.RESOLVED, IssueStatus.CLOSED)).isTrue();
    }

    @Test
    @DisplayName("OPEN → RESOLVED should be invalid (skipping steps)")
    void openToResolvedIsInvalid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.OPEN, IssueStatus.RESOLVED)).isFalse();
    }

    @Test
    @DisplayName("OPEN → IN_PROGRESS should be invalid")
    void openToInProgressIsInvalid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.OPEN, IssueStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    @DisplayName("CLOSED → OPEN should be invalid (terminal state)")
    void closedToOpenIsInvalid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.CLOSED, IssueStatus.OPEN)).isFalse();
    }

    @Test
    @DisplayName("CLOSED → any state should be invalid")
    void closedToAnyIsInvalid() {
        for (IssueStatus target : IssueStatus.values()) {
            assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.CLOSED, target))
                    .as("CLOSED → %s should be invalid", target)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("RESOLVED → IN_PROGRESS should be invalid (backward)")
    void resolvedToInProgressIsInvalid() {
        assertThat(IssueWorkflowValidator.isValidTransition(IssueStatus.RESOLVED, IssueStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    @DisplayName("describeAllowedTransition should return non-empty message")
    void describeTransitionReturnsMessage() {
        for (IssueStatus status : IssueStatus.values()) {
            String msg = IssueWorkflowValidator.describeAllowedTransition(status);
            assertThat(msg).isNotBlank();
        }
    }
}
