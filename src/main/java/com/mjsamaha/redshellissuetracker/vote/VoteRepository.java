package com.mjsamaha.redshellissuetracker.vote;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, UUID> {

    boolean existsByIssueIdAndVoterKey(UUID issueId, String voterKey);
}