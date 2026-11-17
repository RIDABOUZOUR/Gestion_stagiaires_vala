package com.vala.gestionStagiaires.DTOs.suivi;

import com.vala.gestionStagiaires.entities.RapportHebdomadaire;
import com.vala.gestionStagiaires.enums.StatutRapport;

import java.time.Instant;
import java.time.LocalDate;

public record RapportDto(
        Long id,
        Long stagiaireId,
        LocalDate semaine,
        String travauxRealises,
        String difficultes,
        String objectifs,
        Instant dateDepot,
        StatutRapport statut,
        String commentaireEncadrant) {

    public static RapportDto of(RapportHebdomadaire r) {
        return new RapportDto(r.getId(), r.getStagiaire().getId(), r.getSemaine(), r.getTravauxRealises(),
                r.getDifficultes(), r.getObjectifs(), r.getDateDepot(), r.getStatut(), r.getCommentaireEncadrant());
    }
}
