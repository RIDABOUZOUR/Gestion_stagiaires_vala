package com.vala.gestionStagiaires.DTOs.candidature;

import jakarta.validation.constraints.NotNull;

/**
 * Acceptation d'une candidature : stage d'affectation et département du futur stagiaire
 * (par défaut, le département souhaité par le candidat).
 */
public record AcceptationRequest(
        @NotNull(message = "Le stage d'affectation est obligatoire")
        Long stageId,

        Long departementId) {
}
