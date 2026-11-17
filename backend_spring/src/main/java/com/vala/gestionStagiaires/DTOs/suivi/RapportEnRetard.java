package com.vala.gestionStagiaires.DTOs.suivi;

import java.time.LocalDate;

/**
 * Stagiaire en cours de stage qui n'a pas rendu de rapport récemment.
 *
 * @param dernierRapport   lundi de la semaine du dernier rapport ({@code null} si aucun)
 * @param joursSansRapport jours écoulés depuis ce lundi, ou depuis le début du stage s'il n'y a aucun rapport
 */
public record RapportEnRetard(
        Long stagiaireId,
        String nom,
        String prenom,
        String email,
        String sujetStage,
        String encadrant,
        LocalDate dernierRapport,
        long joursSansRapport) {
}
