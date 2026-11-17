package com.vala.gestionStagiaires.DTOs.candidature;

import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.Sexe;

import java.time.Instant;
import java.time.LocalDate;

public record CandidatureDto(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateNaissance,
        Sexe sexe,
        String organisme,
        String niveau,
        String filiere,
        Long departementSouhaiteId,
        String departementSouhaiteNom,
        LocalDate dateDebutSouhaitee,
        LocalDate dateFinSouhaitee,
        String motivation,
        CandidatureStatus statut,
        String motifRefus,
        Instant dateSoumission,
        Instant dateDecision,
        Long stagiaireId,
        boolean cvFourni) {

    public static CandidatureDto of(Candidature c) {
        return new CandidatureDto(c.getId(), c.getNom(), c.getPrenom(), c.getEmail(), c.getTelephone(),
                c.getDateNaissance(), c.getSexe(), c.getOrganisme(), c.getNiveau(), c.getFiliere(),
                c.getDepartementSouhaite() != null ? c.getDepartementSouhaite().getId() : null,
                c.getDepartementSouhaite() != null ? c.getDepartementSouhaite().getNom() : null,
                c.getDateDebutSouhaitee(), c.getDateFinSouhaitee(), c.getMotivation(), c.getStatut(),
                c.getMotifRefus(), c.getDateSoumission(), c.getDateDecision(),
                c.getStagiaire() != null ? c.getStagiaire().getId() : null,
                c.isCvFourni());
    }
}
