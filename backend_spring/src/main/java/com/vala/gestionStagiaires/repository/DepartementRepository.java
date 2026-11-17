package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartementRepository extends JpaRepository<Departement,Long> {
    List<Departement> findByNomIgnoreCaseContaining(String nomKeyword);

    /** Effectif de chaque département, y compris ceux sans stagiaire (0). */
    @Query("SELECT new com.vala.gestionStagiaires.repository.projection.ComptageParLibelle(d.nom, COUNT(s)) "
            + "FROM Departement d LEFT JOIN d.stagiaires s GROUP BY d.id, d.nom")
    List<ComptageParLibelle> compterStagiairesParDepartement();

}
