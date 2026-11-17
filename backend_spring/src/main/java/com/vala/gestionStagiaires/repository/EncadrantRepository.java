package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EncadrantRepository extends JpaRepository<Encadrant,Long> {
    List<Encadrant> findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(String nomKeyword, String prenomKeyword);

    boolean existsByEmailIgnoreCase(String email);

    /** Nombre de stagiaires suivis par chaque encadrant (via ses stages), y compris 0. */
    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant(e.id, e.nom, e.prenom, COUNT(s)) "
            + "FROM Encadrant e LEFT JOIN e.stages st LEFT JOIN st.stagiaires s "
            + "GROUP BY e.id, e.nom, e.prenom")
    List<ComptageParEncadrant> compterStagiairesParEncadrant();

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

}
