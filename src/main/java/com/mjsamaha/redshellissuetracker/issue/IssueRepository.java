package com.mjsamaha.redshellissuetracker.issue;

import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mjsamaha.redshellissuetracker.common.ModerationState;
import com.mjsamaha.redshellissuetracker.common.Visibility;

public interface IssueRepository extends JpaRepository<Issue, UUID> {

    long countByCreatedAtGreaterThanEqual(Instant start);

    Optional<Issue> findByApplicationIdAndNumber(UUID applicationId, long number);

    // Public list: pass Visibility.PUBLIC and ModerationState.APPROVED
    Page<Issue> findByApplicationIdAndVisibilityAndModerationState(
            UUID applicationId, Visibility visibility,
            ModerationState moderationState, Pageable pageable);

    // Moderation queue
    Page<Issue> findByModerationState(ModerationState moderationState, Pageable pageable);

    // Atomic, avoids lost updates when votes arrive concurrently
    @Modifying
    @Query("update Issue i set i.voteCount = i.voteCount + 1 where i.id = :id")
    int incrementVoteCount(@Param("id") UUID id);
}
