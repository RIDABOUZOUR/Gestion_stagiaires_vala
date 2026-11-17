package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface StageRepository extends JpaRepository<Stage,Long>, JpaSpecificationExecutor<Stage> {

    /* L'encadrant est toujours mappé dans le DTO : chargé dans la même requête (évite le N+1). */
    @Override
    @EntityGraph(attributePaths = "encadrant")
    List<Stage> findAll();

    @Override
    @EntityGraph(attributePaths = "encadrant")
    Page<Stage> findAll(Specification<Stage> spec, Pageable pageable);

    @EntityGraph(attributePaths = "encadrant")
    List<Stage> findBySujetIgnoreCaseContaining(String nomKeyword);

    /** Un encadrant a-t-il encore des stages ? (bloque sa suppression) */
    boolean existsByEncadrantId(Long encadrantId);

    /** Charge d'un encadrant : nombre de ses stages dans un statut donné. */
    long countByEncadrantIdAndStatus(Long encadrantId, StageStatus status);

    long countByEncadrantIdAndStatusAndIdNot(Long encadrantId, StageStatus status, Long excludedStageId);

    /** Stages planifiés dont la date de début est atteinte. */
    List<Stage> findByStatusAndDateDebutLessThanEqual(StageStatus status, LocalDate date);

    /** Stages en cours dont la date de fin est dépassée. */
    List<Stage> findByStatusAndDateFinBefore(StageStatus status, LocalDate date);
}
