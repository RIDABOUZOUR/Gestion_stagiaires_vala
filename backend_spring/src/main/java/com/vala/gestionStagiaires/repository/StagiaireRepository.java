package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StagiaireRepository extends JpaRepository<Stagiaire,Long> {
    List<Stagiaire> findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(String nomKeyword, String prenomKeyword);

}
