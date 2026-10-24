package com.mjsamaha.redshellissuetracker.issue;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueStatusHistoryRepository extends JpaRepository<IssueStatusHistory, UUID> {

    List<IssueStatusHistory> findByIssueIdOrderByCreatedAtAsc(UUID issueId);
}