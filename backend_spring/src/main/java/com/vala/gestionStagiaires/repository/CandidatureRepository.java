package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatutCandidature;
import com.vala.gestionStagiaires.repository.projection.DelaiDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long>, JpaSpecificationExecutor<Candidature> {

    @Override
    @EntityGraph(attributePaths = "departementSouhaite")
    Page<Candidature> findAll(Specification<Candidature> spec, Pageable pageable);

    /** Une candidature active existe-t-elle déjà pour cet email ? */
    boolean existsByEmailIgnoreCaseAndStatutIn(String email, Collection<CandidatureStatus> statuts);

    boolean existsByEmailIgnoreCaseAndStatutInAndIdNot(String email, Collection<CandidatureStatus> statuts, Long id);

    // ---- Nettoyage lors des suppressions --------------------------------

    /** Le département souhaité n'est qu'un vœu : il est effacé si le département disparaît. */
    @Modifying
    @Query("UPDATE Candidature c SET c.departementSouhaite = null WHERE c.departementSouhaite.id = :departementId")
    void detacherDepartement(Long departementId);

    /** La candidature acceptée reste dans l'historique même si le stagiaire est supprimé. */
    @Modifying
    @Query("UPDATE Candidature c SET c.stagiaire = null WHERE c.stagiaire.id = :stagiaireId")
    void detacherStagiaire(Long stagiaireId);

    // ---- Statistiques ---------------------------------------------------

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParStatutCandidature(c.statut, COUNT(c)) "
            + "FROM Candidature c GROUP BY c.statut")
    List<ComptageParStatutCandidature> compterParStatut();

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParLibelle(c.organisme, COUNT(c)) "
            + "FROM Candidature c GROUP BY c.organisme")
    List<ComptageParLibelle> compterParOrganisme();

    @Query("SELECT c.dateSoumission FROM Candidature c WHERE c.dateSoumission >= :depuis")
    List<Instant> datesSoumissionDepuis(Instant depuis);

    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.DelaiDecision(c.dateSoumission, c.dateDecision) "
            + "FROM Candidature c WHERE c.statut IN :statuts AND c.dateDecision IS NOT NULL")
    List<DelaiDecision> delaisDecision(Collection<CandidatureStatus> statuts);
}
