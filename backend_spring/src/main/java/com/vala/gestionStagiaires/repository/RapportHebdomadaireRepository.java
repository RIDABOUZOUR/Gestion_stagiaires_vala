package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.RapportHebdomadaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.projection.DernierRapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RapportHebdomadaireRepository extends JpaRepository<RapportHebdomadaire, Long> {

    List<RapportHebdomadaire> findByStagiaireIdOrderBySemaineDesc(Long stagiaireId);

    boolean existsByStagiaireIdAndSemaine(Long stagiaireId, LocalDate semaine);

    /** Ce rapport concerne-t-il un stagiaire suivi par cet encadrant ? */
    boolean existsByIdAndStagiaireStageEncadrantId(Long id, Long encadrantId);

    /** Semaine du dernier rapport de chaque stagiaire dont le stage est dans le statut donné. */
    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.DernierRapport(r.stagiaire.id, MAX(r.semaine)) "
            + "FROM RapportHebdomadaire r WHERE r.stagiaire.stage.status = :status GROUP BY r.stagiaire.id")
    List<DernierRapport> derniereSemaineParStagiaire(StageStatus status);

    @Modifying
    @Query("DELETE FROM RapportHebdomadaire r WHERE r.stagiaire.id = :stagiaireId")
    void supprimerParStagiaire(Long stagiaireId);
}
