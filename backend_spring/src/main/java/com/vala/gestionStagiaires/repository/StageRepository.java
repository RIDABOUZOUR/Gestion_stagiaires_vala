package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage,Long> {
    List<Stage> findBySujetIgnoreCaseContaining(String nomKeyword);
}
