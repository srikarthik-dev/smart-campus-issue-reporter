package com.srikarthik.smartcampus.dto;

import java.util.Map;

/**
 * Dashboard summary DTO containing real database-backed statistics.
 */
public class DashboardSummaryResponse {

    private long totalIssues;
    private long openIssues;
    private long assignedIssues;
    private long inProgressIssues;
    private long resolvedIssues;
    private long closedIssues;
    private long criticalIssues;
    private long highPriorityIssues;

    // category label -> count
    private Map<String, Long> byCategory;

    // status label -> count
    private Map<String, Long> byStatus;

    // priority label -> count
    private Map<String, Long> byPriority;

    // --- Getters and Setters ---

    public long getTotalIssues() { return totalIssues; }
    public void setTotalIssues(long totalIssues) { this.totalIssues = totalIssues; }

    public long getOpenIssues() { return openIssues; }
    public void setOpenIssues(long openIssues) { this.openIssues = openIssues; }

    public long getAssignedIssues() { return assignedIssues; }
    public void setAssignedIssues(long assignedIssues) { this.assignedIssues = assignedIssues; }

    public long getInProgressIssues() { return inProgressIssues; }
    public void setInProgressIssues(long inProgressIssues) { this.inProgressIssues = inProgressIssues; }

    public long getResolvedIssues() { return resolvedIssues; }
    public void setResolvedIssues(long resolvedIssues) { this.resolvedIssues = resolvedIssues; }

    public long getClosedIssues() { return closedIssues; }
    public void setClosedIssues(long closedIssues) { this.closedIssues = closedIssues; }

    public long getCriticalIssues() { return criticalIssues; }
    public void setCriticalIssues(long criticalIssues) { this.criticalIssues = criticalIssues; }

    public long getHighPriorityIssues() { return highPriorityIssues; }
    public void setHighPriorityIssues(long highPriorityIssues) { this.highPriorityIssues = highPriorityIssues; }

    public Map<String, Long> getByCategory() { return byCategory; }
    public void setByCategory(Map<String, Long> byCategory) { this.byCategory = byCategory; }

    public Map<String, Long> getByStatus() { return byStatus; }
    public void setByStatus(Map<String, Long> byStatus) { this.byStatus = byStatus; }

    public Map<String, Long> getByPriority() { return byPriority; }
    public void setByPriority(Map<String, Long> byPriority) { this.byPriority = byPriority; }
}
