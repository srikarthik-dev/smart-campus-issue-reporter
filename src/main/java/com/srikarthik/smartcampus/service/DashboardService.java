package com.srikarthik.smartcampus.service;

import com.srikarthik.smartcampus.dto.DashboardSummaryResponse;
import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;
import com.srikarthik.smartcampus.repository.IssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides all dashboard statistics backed by live database queries.
 * No values are hardcoded.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final IssueRepository issueRepository;

    public DashboardService(IssueRepository issueRepository) {
        this.issueRepository = issueRepository;
    }

    public DashboardSummaryResponse getSummary() {
        DashboardSummaryResponse response = new DashboardSummaryResponse();

        response.setTotalIssues(issueRepository.count());
        response.setOpenIssues(issueRepository.countByStatus(IssueStatus.OPEN));
        response.setAssignedIssues(issueRepository.countByStatus(IssueStatus.ASSIGNED));
        response.setInProgressIssues(issueRepository.countByStatus(IssueStatus.IN_PROGRESS));
        response.setResolvedIssues(issueRepository.countByStatus(IssueStatus.RESOLVED));
        response.setClosedIssues(issueRepository.countByStatus(IssueStatus.CLOSED));
        response.setCriticalIssues(issueRepository.countByPriority(IssuePriority.CRITICAL));
        response.setHighPriorityIssues(issueRepository.countByPriority(IssuePriority.HIGH));

        response.setByCategory(buildCategoryMap());
        response.setByStatus(buildStatusMap());
        response.setByPriority(buildPriorityMap());

        return response;
    }

    public Map<String, Long> getByCategory() {
        return buildCategoryMap();
    }

    public Map<String, Long> getByStatus() {
        return buildStatusMap();
    }

    public Map<String, Long> getByPriority() {
        return buildPriorityMap();
    }

    private Map<String, Long> buildCategoryMap() {
        // Initialise all categories to 0 so charts always show all labels
        Map<String, Long> map = new LinkedHashMap<>();
        for (IssueCategory c : IssueCategory.values()) {
            map.put(c.name(), 0L);
        }
        List<Object[]> rows = issueRepository.countByEachCategory();
        for (Object[] row : rows) {
            IssueCategory category = (IssueCategory) row[0];
            Long count = (Long) row[1];
            map.put(category.name(), count);
        }
        return map;
    }

    private Map<String, Long> buildStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (IssueStatus s : IssueStatus.values()) {
            map.put(s.name(), 0L);
        }
        List<Object[]> rows = issueRepository.countByEachStatus();
        for (Object[] row : rows) {
            IssueStatus status = (IssueStatus) row[0];
            Long count = (Long) row[1];
            map.put(status.name(), count);
        }
        return map;
    }

    private Map<String, Long> buildPriorityMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (IssuePriority p : IssuePriority.values()) {
            map.put(p.name(), 0L);
        }
        List<Object[]> rows = issueRepository.countByEachPriority();
        for (Object[] row : rows) {
            IssuePriority priority = (IssuePriority) row[0];
            Long count = (Long) row[1];
            map.put(priority.name(), count);
        }
        return map;
    }
}
