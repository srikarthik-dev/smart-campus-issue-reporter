package com.srikarthik.smartcampus.controller;

import com.srikarthik.smartcampus.dto.*;
import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;
import com.srikarthik.smartcampus.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    // POST /api/issues — creates a new issue; priority is calculated, not accepted from client
    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        IssueResponse response = issueService.createIssue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/issues — returns all issues
    @GetMapping
    public ResponseEntity<List<IssueResponse>> getAllIssues() {
        return ResponseEntity.ok(issueService.getAllIssues());
    }

    // GET /api/issues/{id}
    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }

    // PUT /api/issues/{id}
    @PutMapping("/{id}")
    public ResponseEntity<IssueResponse> updateIssue(@PathVariable Long id,
                                                      @Valid @RequestBody CreateIssueRequest request) {
        return ResponseEntity.ok(issueService.updateIssue(id, request));
    }

    // DELETE /api/issues/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable Long id) {
        issueService.deleteIssue(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/issues/{id}/assign — assign staff, transitions OPEN → ASSIGNED
    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(@PathVariable Long id,
                                                      @Valid @RequestBody AssignIssueRequest request) {
        return ResponseEntity.ok(issueService.assignIssue(id, request));
    }

    // PUT /api/issues/{id}/status — generic status update with workflow validation
    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateStatusRequest request) {
        return ResponseEntity.ok(issueService.updateStatus(id, request));
    }

    // PUT /api/issues/{id}/resolve — resolve with mandatory resolution notes
    @PutMapping("/{id}/resolve")
    public ResponseEntity<IssueResponse> resolveIssue(@PathVariable Long id,
                                                       @Valid @RequestBody ResolveIssueRequest request) {
        return ResponseEntity.ok(issueService.resolveIssue(id, request));
    }

    // GET /api/issues/search — full-text + multi-filter search
    @GetMapping("/search")
    public ResponseEntity<List<IssueResponse>> searchIssues(
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssuePriority priority,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String reportedBy,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(issueService.searchIssues(
                category, status, priority, location, reportedBy, keyword, fromDate, toDate));
    }
}
