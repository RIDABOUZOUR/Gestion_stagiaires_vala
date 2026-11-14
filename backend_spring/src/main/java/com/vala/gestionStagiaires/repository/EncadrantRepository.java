package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncadrantRepository extends JpaRepository<Encadrant,Long> {
    List<Encadrant> findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(String nomKeyword, String prenomKeyword);

}
