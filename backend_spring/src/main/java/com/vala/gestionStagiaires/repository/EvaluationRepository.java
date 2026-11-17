package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Evaluation;
import com.vala.gestionStagiaires.enums.StageStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    Optional<Evaluation> findByStagiaireId(Long stagiaireId);

    /** Toutes les évaluations avec le département, le stage et l'encadrant du stagiaire (statistiques). */
    @Override
    @EntityGraph(attributePaths = {"stagiaire.departement", "stagiaire.stage.encadrant"})
    List<Evaluation> findAll();

    /** Stagiaires dont le stage est dans ce statut mais qui n'ont pas encore été évalués. */
    @Query("SELECT COUNT(s) FROM Stagiaire s WHERE s.stage.status = :status "
            + "AND NOT EXISTS (SELECT e.id FROM Evaluation e WHERE e.stagiaire = s)")
    long compterStagiairesSansEvaluation(StageStatus status);

    @Modifying
    @Query("DELETE FROM Evaluation e WHERE e.stagiaire.id = :stagiaireId")
    void supprimerParStagiaire(Long stagiaireId);
}
