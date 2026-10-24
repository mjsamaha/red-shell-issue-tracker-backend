package com.mjsamaha.redshellissuetracker.comment;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Staff view
    List<Comment> findByIssueIdOrderByCreatedAtAsc(UUID issueId);

    // Public view
    List<Comment> findByIssueIdAndInternalFalseOrderByCreatedAtAsc(UUID issueId);
}