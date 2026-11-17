package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Presence;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.projection.ComptagePresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresenceRepository extends JpaRepository<Presence, Long> {

    Optional<Presence> findByStagiaireIdAndDate(Long stagiaireId, LocalDate date);

    List<Presence> findByStagiaireIdAndDateBetweenOrderByDateAsc(Long stagiaireId, LocalDate du, LocalDate au);

    /** Jours saisis par statut, pour les stagiaires dont le stage est dans le statut donné. */
    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptagePresence(p.stagiaire.id, p.statut, COUNT(p)) "
            + "FROM Presence p WHERE p.stagiaire.stage.status = :status GROUP BY p.stagiaire.id, p.statut")
    List<ComptagePresence> compterParStagiaireEtStatut(StageStatus status);

    @Modifying
    @Query("DELETE FROM Presence p WHERE p.stagiaire.id = :stagiaireId")
    void supprimerParStagiaire(Long stagiaireId);
}
