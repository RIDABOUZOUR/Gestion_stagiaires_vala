package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StagiaireRepository extends JpaRepository<Stagiaire,Long> {
    List<Stagiaire> findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(String nomKeyword, String prenomKeyword);

    /** Un stage a-t-il encore des stagiaires ? (bloque sa suppression) */
    boolean existsByStageId(Long stageId);

    /** Un département a-t-il encore des stagiaires ? (bloque sa suppression) */
    boolean existsByDepartementId(Long departementId);
    @Query("SELECT s.departement.nom, COUNT(s) FROM Stagiaire s GROUP BY s.departement.nom")
    List<Object[]> countStagiairesParDepartement();
    @Query("SELECT s.filiere, COUNT(s) FROM Stagiaire s GROUP BY s.filiere")
    List<Object[]> countStagiairesByFiliereRaw();
    @Query("SELECT s.organisme, COUNT(s) FROM Stagiaire s GROUP BY s.organisme")
    List<Object[]> countStagiairesByOrganisme();
    @Query("SELECT CONCAT(e.nom, ' ', e.prenom), COUNT(s) FROM Stagiaire s JOIN s.stage enc JOIN enc.encadrant e GROUP BY e.nom, e.prenom")
    List<Object[]> countStagiairesParEncadrant();



}
