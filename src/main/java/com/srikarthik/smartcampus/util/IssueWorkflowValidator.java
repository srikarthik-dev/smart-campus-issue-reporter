package com.srikarthik.smartcampus.util;

import com.srikarthik.smartcampus.model.IssueStatus;

/**
 * Defines and validates the legal transitions for the issue lifecycle.
 *
 * Allowed transitions:
 *   OPEN        → ASSIGNED
 *   ASSIGNED    → IN_PROGRESS
 *   IN_PROGRESS → RESOLVED
 *   RESOLVED    → CLOSED
 *
 * Any other transition is invalid and will be rejected.
 */
public class IssueWorkflowValidator {

    private IssueWorkflowValidator() {}

    /**
     * Returns true if transitioning from {@code current} to {@code next} is a valid workflow step.
     */
    public static boolean isValidTransition(IssueStatus current, IssueStatus next) {
        return switch (current) {
            case OPEN        -> next == IssueStatus.ASSIGNED;
            case ASSIGNED    -> next == IssueStatus.IN_PROGRESS;
            case IN_PROGRESS -> next == IssueStatus.RESOLVED;
            case RESOLVED    -> next == IssueStatus.CLOSED;
            case CLOSED      -> false; // Terminal state — no further transitions allowed
        };
    }

    /**
     * Returns a human-readable message explaining what transitions are allowed from a given status.
     */
    public static String describeAllowedTransition(IssueStatus current) {
        return switch (current) {
            case OPEN        -> "OPEN issues can only be moved to ASSIGNED";
            case ASSIGNED    -> "ASSIGNED issues can only be moved to IN_PROGRESS";
            case IN_PROGRESS -> "IN_PROGRESS issues can only be moved to RESOLVED";
            case RESOLVED    -> "RESOLVED issues can only be moved to CLOSED";
            case CLOSED      -> "CLOSED issues cannot be transitioned further";
        };
    }
}
