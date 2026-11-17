package com.vala.gestionStagiaires.DTOs.suivi;

/**
 * Assiduité d'un stagiaire en cours de stage.
 *
 * @param tauxPresence pourcentage (1 décimale) de jours présents ou en retard sur les jours saisis ;
 *                     {@code null} si aucun jour n'est saisi
 */
public record AssiduiteStagiaire(
        Long stagiaireId,
        String nom,
        String prenom,
        String departement,
        long joursSaisis,
        long presents,
        long retards,
        long absences,
        long absencesJustifiees,
        Double tauxPresence) {
}
