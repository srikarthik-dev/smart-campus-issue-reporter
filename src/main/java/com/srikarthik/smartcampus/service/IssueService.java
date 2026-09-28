package com.srikarthik.smartcampus.service;

import com.srikarthik.smartcampus.dto.*;
import com.srikarthik.smartcampus.exception.InvalidAssignmentException;
import com.srikarthik.smartcampus.exception.InvalidWorkflowTransitionException;
import com.srikarthik.smartcampus.exception.IssueNotFoundException;
import com.srikarthik.smartcampus.model.Issue;
import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;
import com.srikarthik.smartcampus.repository.IssueRepository;
import com.srikarthik.smartcampus.util.IssueCodeGenerator;
import com.srikarthik.smartcampus.util.IssueWorkflowValidator;
import com.srikarthik.smartcampus.util.PriorityEngine;
import com.srikarthik.smartcampus.util.SlaCalculator;
import com.srikarthik.smartcampus.util.PriorityEngine.PriorityResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IssueService {

    private final IssueRepository issueRepository;
    private final PriorityEngine priorityEngine;

    public IssueService(IssueRepository issueRepository, PriorityEngine priorityEngine) {
        this.issueRepository = issueRepository;
        this.priorityEngine = priorityEngine;
    }

    // --- Create ---

    public IssueResponse createIssue(CreateIssueRequest request) {
        // Generate a unique issue code, retrying on the rare collision
        String issueCode;
        int attempts = 0;
        do {
            issueCode = IssueCodeGenerator.generate();
            attempts++;
        } while (issueRepository.existsByIssueCode(issueCode) && attempts < 10);

        // Compute priority via the engine — clients cannot set priority directly
        PriorityResult result = priorityEngine.calculate(
                request.isSafetyImpact(),
                request.getAffectedUsers(),
                request.getCategory()
        );

        Issue issue = new Issue();
        issue.setIssueCode(issueCode);
        issue.setReportedBy(request.getReportedBy());
        issue.setCategory(request.getCategory());
        issue.setLocation(request.getLocation());
        issue.setDescription(request.getDescription());
        issue.setAffectedUsers(request.getAffectedUsers());
        issue.setSafetyImpact(request.isSafetyImpact());
        issue.setPriority(result.getPriority());
        issue.setPriorityScore(result.getTotalScore());
        issue.setStatus(IssueStatus.OPEN);

        Issue saved = issueRepository.save(issue);
        return toResponse(saved);
    }

    // --- Read ---

    @Transactional(readOnly = true)
    public List<IssueResponse> getAllIssues() {
        return issueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueById(Long id) {
        Issue issue = findIssueOrThrow(id);
        return toResponse(issue);
    }

    @Transactional(readOnly = true)
    public IssueResponse getIssueByCode(String code) {
        Issue issue = issueRepository.findByIssueCode(code)
                .orElseThrow(() -> new IssueNotFoundException("Issue not found with code: " + code));
        return toResponse(issue);
    }

    // --- Update ---

    public IssueResponse updateIssue(Long id, CreateIssueRequest request) {
        Issue issue = findIssueOrThrow(id);

        issue.setReportedBy(request.getReportedBy());
        issue.setCategory(request.getCategory());
        issue.setLocation(request.getLocation());
        issue.setDescription(request.getDescription());
        issue.setAffectedUsers(request.getAffectedUsers());
        issue.setSafetyImpact(request.isSafetyImpact());

        // Recalculate priority based on updated data and current age
        PriorityResult result = priorityEngine.calculate(
                request.isSafetyImpact(),
                request.getAffectedUsers(),
                request.getCategory(),
                issue.getReportedAt()
        );
        issue.setPriority(result.getPriority());
        issue.setPriorityScore(result.getTotalScore());

        Issue updated = issueRepository.save(issue);
        return toResponse(updated);
    }

    // --- Delete ---

    public void deleteIssue(Long id) {
        if (!issueRepository.existsById(id)) {
            throw new IssueNotFoundException("Issue not found with id: " + id);
        }
        issueRepository.deleteById(id);
    }

    // --- Workflow actions ---

    public IssueResponse assignIssue(Long id, AssignIssueRequest request) {
        Issue issue = findIssueOrThrow(id);

        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new InvalidAssignmentException("Cannot assign a CLOSED issue.");
        }
        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new InvalidAssignmentException("Cannot assign a RESOLVED issue.");
        }

        // Assigning moves OPEN → ASSIGNED automatically
        if (issue.getStatus() == IssueStatus.OPEN) {
            issue.setStatus(IssueStatus.ASSIGNED);
        }
        issue.setAssignedTo(request.getAssignedTo());

        return toResponse(issueRepository.save(issue));
    }

    public IssueResponse updateStatus(Long id, UpdateStatusRequest request) {
        Issue issue = findIssueOrThrow(id);
        IssueStatus newStatus = request.getStatus();

        if (!IssueWorkflowValidator.isValidTransition(issue.getStatus(), newStatus)) {
            throw new InvalidWorkflowTransitionException(
                    String.format("Cannot transition from %s to %s. %s",
                            issue.getStatus(), newStatus,
                            IssueWorkflowValidator.describeAllowedTransition(issue.getStatus()))
            );
        }

        issue.setStatus(newStatus);
        return toResponse(issueRepository.save(issue));
    }

    public IssueResponse resolveIssue(Long id, ResolveIssueRequest request) {
        Issue issue = findIssueOrThrow(id);

        // Resolution is only valid from IN_PROGRESS
        if (!IssueWorkflowValidator.isValidTransition(issue.getStatus(), IssueStatus.RESOLVED)) {
            throw new InvalidWorkflowTransitionException(
                    String.format("Cannot resolve issue in status '%s'. Issue must be IN_PROGRESS before resolving. %s",
                            issue.getStatus(),
                            IssueWorkflowValidator.describeAllowedTransition(issue.getStatus()))
            );
        }

        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolutionNotes(request.getResolutionNotes());
        issue.setResolvedAt(LocalDateTime.now());

        // Re-evaluate priority after resolution (age still matters for analytics)
        PriorityResult result = priorityEngine.calculate(
                issue.isSafetyImpact(), issue.getAffectedUsers(),
                issue.getCategory(), issue.getReportedAt()
        );
        issue.setPriority(result.getPriority());
        issue.setPriorityScore(result.getTotalScore());

        return toResponse(issueRepository.save(issue));
    }

    // --- Search ---

    @Transactional(readOnly = true)
    public List<IssueResponse> searchIssues(IssueCategory category, IssueStatus status,
                                             IssuePriority priority, String location,
                                             String reportedBy, String keyword,
                                             LocalDateTime fromDate, LocalDateTime toDate) {
        return issueRepository.searchIssues(category, status, priority, location,
                        reportedBy, keyword, fromDate, toDate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Helpers ---

    private Issue findIssueOrThrow(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new IssueNotFoundException("Issue not found with id: " + id));
    }

    /**
     * Maps an Issue entity to the full response DTO.
     */
    public IssueResponse toResponse(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.setId(issue.getId());
        response.setIssueCode(issue.getIssueCode());
        response.setReportedBy(issue.getReportedBy());
        response.setCategory(issue.getCategory());
        response.setLocation(issue.getLocation());
        response.setDescription(issue.getDescription());
        response.setAffectedUsers(issue.getAffectedUsers());
        response.setSafetyImpact(issue.isSafetyImpact());
        response.setPriority(issue.getPriority());
        response.setPriorityScore(issue.getPriorityScore());
        
        PriorityResult result = priorityEngine.calculate(
                issue.isSafetyImpact(), issue.getAffectedUsers(),
                issue.getCategory(), issue.getReportedAt()
        );
        response.setSafetyScore(result.getSafetyScore());
        response.setUserScore(result.getUserScore());
        response.setCategoryScore(result.getCategoryScore());
        response.setAgeScore(result.getAgeScore());

        response.setStatus(issue.getStatus());
        response.setAssignedTo(issue.getAssignedTo());
        response.setResolutionNotes(issue.getResolutionNotes());
        response.setReportedAt(issue.getReportedAt());
        response.setUpdatedAt(issue.getUpdatedAt());
        response.setResolvedAt(issue.getResolvedAt());
        response.setSlaIndicator(SlaCalculator.calculateSlaIndicator(issue.getReportedAt(), issue.getResolvedAt()));
        return response;
    }
}
