package com.srikarthik.smartcampus.repository;

import com.srikarthik.smartcampus.model.Issue;
import com.srikarthik.smartcampus.model.IssueCategory;
import com.srikarthik.smartcampus.model.IssuePriority;
import com.srikarthik.smartcampus.model.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long>, JpaSpecificationExecutor<Issue> {

    Optional<Issue> findByIssueCode(String issueCode);

    boolean existsByIssueCode(String issueCode);

    List<Issue> findByStatus(IssueStatus status);

    List<Issue> findByCategory(IssueCategory category);

    List<Issue> findByPriority(IssuePriority priority);

    List<Issue> findByReportedBy(String reportedBy);

    List<Issue> findByStatusIn(List<IssueStatus> statuses);

    // Dashboard queries — real database-backed counts
    long countByStatus(IssueStatus status);

    long countByPriority(IssuePriority priority);

    long countByCategory(IssueCategory category);

    @Query("SELECT i.category, COUNT(i) FROM Issue i GROUP BY i.category")
    List<Object[]> countByEachCategory();

    @Query("SELECT i.status, COUNT(i) FROM Issue i GROUP BY i.status")
    List<Object[]> countByEachStatus();

    @Query("SELECT i.priority, COUNT(i) FROM Issue i GROUP BY i.priority")
    List<Object[]> countByEachPriority();

    @Query("SELECT i FROM Issue i WHERE i.reportedAt >= :since ORDER BY i.reportedAt DESC")
    List<Issue> findRecentIssues(@Param("since") LocalDateTime since);

    @Query("SELECT i FROM Issue i WHERE i.priority IN ('CRITICAL', 'HIGH') AND i.status NOT IN ('RESOLVED', 'CLOSED') ORDER BY i.priority, i.reportedAt ASC")
    List<Issue> findCriticalAndHighUnresolved();

    @Query("SELECT i FROM Issue i WHERE " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:location IS NULL OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:reportedBy IS NULL OR LOWER(i.reportedBy) LIKE LOWER(CONCAT('%', :reportedBy, '%'))) AND " +
           "(:keyword IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.location) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:fromDate IS NULL OR i.reportedAt >= :fromDate) AND " +
           "(:toDate IS NULL OR i.reportedAt <= :toDate) " +
           "ORDER BY i.reportedAt DESC")
    List<Issue> searchIssues(
            @Param("category") IssueCategory category,
            @Param("status") IssueStatus status,
            @Param("priority") IssuePriority priority,
            @Param("location") String location,
            @Param("reportedBy") String reportedBy,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}
