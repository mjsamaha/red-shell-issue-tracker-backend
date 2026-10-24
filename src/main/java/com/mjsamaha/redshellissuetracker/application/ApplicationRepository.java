package com.mjsamaha.redshellissuetracker.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findBySlug(String slug);

    List<Application> findByPublicVisibleTrueAndArchivedAtIsNull();

    long countByArchivedAtIsNull();

    // Use when allocating an issue number
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Application a where a.id = :id")
    Optional<Application> findByIdForUpdate(@Param("id") UUID id);
}