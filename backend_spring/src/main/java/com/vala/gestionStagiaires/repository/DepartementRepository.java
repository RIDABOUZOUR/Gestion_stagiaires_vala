package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartementRepository extends JpaRepository<Departement,Long> {
    List<Departement> findByNomIgnoreCaseContaining(String nomKeyword);

}
