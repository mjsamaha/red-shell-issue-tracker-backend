package com.mjsamaha.redshellissuetracker.reporter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporterRepository extends JpaRepository<Reporter, UUID> {

    Optional<Reporter> findByEmail(String email);
}