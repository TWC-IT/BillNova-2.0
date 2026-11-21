package com.thewizecompany.billnova.repository;

import com.thewizecompany.billnova.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndIsDeletedFalse(UUID projectId);

    Page<Project> findAllByIsDeletedFalse(Pageable pageable);

    Page<Project> findAllByClientIdAndIsDeletedFalse(Pageable pageable, UUID clientId);
}
