package com.vala.gestionStagiaires.DTOs.auth;

import com.vala.gestionStagiaires.enums.Role;
import jakarta.validation.constraints.NotNull;

/** Modification du rôle, de l'encadrant lié et de l'état d'un compte. */
public record UtilisateurModification(
        @NotNull(message = "Le rôle est obligatoire") Role role,
        Long encadrantId,
        @NotNull(message = "Indiquez si le compte est actif") Boolean actif) {
}
