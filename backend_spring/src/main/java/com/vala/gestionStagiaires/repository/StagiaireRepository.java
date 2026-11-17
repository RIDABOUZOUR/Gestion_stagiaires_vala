package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stagiaire;
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

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

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
