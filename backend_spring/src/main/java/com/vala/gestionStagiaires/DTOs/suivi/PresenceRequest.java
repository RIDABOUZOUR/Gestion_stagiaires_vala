package com.vala.gestionStagiaires.DTOs.suivi;

import com.vala.gestionStagiaires.enums.StatutPresence;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de {@code PUT /stagiaires/{id}/presences/{date}}. */
public record PresenceRequest(
        @NotNull(message = "Le statut de présence est obligatoire")
        StatutPresence statut,

        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire) {
}
