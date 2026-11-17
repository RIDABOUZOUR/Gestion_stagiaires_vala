package com.vala.gestionStagiaires.DTOs.suivi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Demande de révision d'un rapport : l'encadrant doit expliquer ce qu'il faut revoir. */
public record RevisionRequest(
        @NotBlank(message = "Un commentaire est obligatoire pour demander une révision")
        @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
        String commentaire) {
}
