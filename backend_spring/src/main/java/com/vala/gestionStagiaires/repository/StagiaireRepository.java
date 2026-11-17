package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StagiaireRepository extends JpaRepository<Stagiaire,Long>, JpaSpecificationExecutor<Stagiaire> {

    /*
     * Les relations sont LAZY mais toujours mappées dans le DTO : on les charge
     * dans la même requête (fetch join) pour éviter les requêtes N+1.
     */
    @Override
    @EntityGraph(attributePaths = {"stage.encadrant", "departement"})
    List<Stagiaire> findAll();

    @Override
    @EntityGraph(attributePaths = {"stage.encadrant", "departement"})
    Page<Stagiaire> findAll(Specification<Stagiaire> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"stage.encadrant", "departement"})
    List<Stagiaire> findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(String nomKeyword, String prenomKeyword);

    /** Stagiaires dont le stage est dans le statut donné, avec stage, encadrant et département. */
    @EntityGraph(attributePaths = {"stage.encadrant", "departement"})
    List<Stagiaire> findByStageStatus(StageStatus status);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    /** Un stage a-t-il encore des stagiaires ? (bloque sa suppression) */
    boolean existsByStageId(Long stageId);

    /** Un département a-t-il encore des stagiaires ? (bloque sa suppression) */
    boolean existsByDepartementId(Long departementId);
    // ---- Statistiques -------------------------------------------------

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParLibelle(s.filiere, COUNT(s)) FROM Stagiaire s GROUP BY s.filiere")
    List<ComptageParLibelle> compterParFiliere();

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParLibelle(s.organisme, COUNT(s)) FROM Stagiaire s GROUP BY s.organisme")
    List<ComptageParLibelle> compterParOrganisme();

    /** Stagiaires dont le stage est dans le statut donné (ex. EN_COURS = stagiaires actifs). */
    long countByStageStatus(StageStatus status);



}
