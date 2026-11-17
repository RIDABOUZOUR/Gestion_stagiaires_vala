package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.StageStatus;

/** Nombre de stages dans un statut donné. */
public record ComptageParStatut(StageStatus status, Long nombre) {
}
