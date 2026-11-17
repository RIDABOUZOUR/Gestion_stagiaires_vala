package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StageRepository extends JpaRepository<Stage,Long> {
    List<Stage> findBySujetIgnoreCaseContaining(String nomKeyword);

    /** Un encadrant a-t-il encore des stages ? (bloque sa suppression) */
    boolean existsByEncadrantId(Long encadrantId);

    /** Stages planifiés dont la date de début est atteinte. */
    List<Stage> findByStatusAndDateDebutLessThanEqual(StageStatus status, LocalDate date);

    /** Stages en cours dont la date de fin est dépassée. */
    List<Stage> findByStatusAndDateFinBefore(StageStatus status, LocalDate date);
}
