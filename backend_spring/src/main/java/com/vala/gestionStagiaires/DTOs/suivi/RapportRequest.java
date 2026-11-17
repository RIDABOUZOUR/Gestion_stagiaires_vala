package com.vala.gestionStagiaires.DTOs.suivi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dépôt ou modification d'un rapport hebdomadaire.
 * {@code semaine} peut être n'importe quel jour : il est ramené au lundi.
 */
public record RapportRequest(
        @NotNull(message = "La semaine est obligatoire")
        LocalDate semaine,

        @NotBlank(message = "Les travaux réalisés sont obligatoires")
        @Size(max = 4000, message = "Les travaux réalisés ne doivent pas dépasser 4000 caractères")
        String travauxRealises,

        @Size(max = 2000, message = "Les difficultés ne doivent pas dépasser 2000 caractères")
        String difficultes,

        @Size(max = 2000, message = "Les objectifs ne doivent pas dépasser 2000 caractères")
        String objectifs) {
}
