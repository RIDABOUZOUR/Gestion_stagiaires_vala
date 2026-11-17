package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.CvCandidature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CvCandidatureRepository extends JpaRepository<CvCandidature, Long> {

    Optional<CvCandidature> findByCandidatureId(Long candidatureId);

    @Modifying
    @Query("DELETE FROM CvCandidature cv WHERE cv.candidature.id = :candidatureId")
    void supprimerParCandidature(Long candidatureId);
}
