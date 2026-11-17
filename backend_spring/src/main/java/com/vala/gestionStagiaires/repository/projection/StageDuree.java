package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.StageStatus;

import java.time.LocalDate;

/** Données d'un stage utiles au calcul des durées et des taux. */
public record StageDuree(Long stageId, StageStatus status, LocalDate dateDebut, LocalDate dateFin,
                         Long encadrantId, String encadrantNom, String encadrantPrenom) {
}
