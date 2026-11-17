package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatut;
import com.vala.gestionStagiaires.repository.projection.StageDuree;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

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

    // ---- Statistiques -------------------------------------------------

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParStatut(s.status, COUNT(s)) FROM Stage s GROUP BY s.status")
    List<ComptageParStatut> compterParStatut();

    long countByStatus(StageStatus status);

    /** Stages dans un statut donné dont la date de fin tombe dans l'intervalle (bornes incluses). */
    @EntityGraph(attributePaths = "encadrant")
    List<Stage> findByStatusAndDateFinBetweenOrderByDateFinAsc(StageStatus status, LocalDate debut, LocalDate fin);

    long countByStatusAndDateFinBetween(StageStatus status, LocalDate debut, LocalDate fin);

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.StageDuree(s.id, s.status, s.dateDebut, s.dateFin, e.id, e.nom, e.prenom) "
            + "FROM Stage s LEFT JOIN s.encadrant e")
    List<StageDuree> donneesDurees();
}
