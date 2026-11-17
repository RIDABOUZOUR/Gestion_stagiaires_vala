package com.vala.gestionStagiaires.repository;

import com.vala.gestionStagiaires.entities.Attestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AttestationRepository extends JpaRepository<Attestation, Long> {

    Optional<Attestation> findByStagiaireId(Long stagiaireId);

    boolean existsByStagiaireId(Long stagiaireId);

    /** Plus grand rang attribué dans l'année (0 si aucune attestation). */
    @Query("SELECT COALESCE(MAX(a.sequence), 0) FROM Attestation a WHERE a.annee = :annee")
    int dernierRang(int annee);
}
