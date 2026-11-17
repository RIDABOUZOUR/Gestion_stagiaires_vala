package com.vala.gestionStagiaires.DTOs.suivi;

import jakarta.validation.constraints.Size;

/** Commentaire de l'encadrant lors de la validation ou de la demande de révision d'un rapport. */
public record CommentaireRequest(
        @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
        String commentaire) {
}
