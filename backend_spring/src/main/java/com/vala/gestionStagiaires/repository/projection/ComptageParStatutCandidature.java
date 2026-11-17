package com.vala.gestionStagiaires.repository.projection;

import com.vala.gestionStagiaires.enums.CandidatureStatus;

public record ComptageParStatutCandidature(CandidatureStatus statut, Long nombre) {
}
