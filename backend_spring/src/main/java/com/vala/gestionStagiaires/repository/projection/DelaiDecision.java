package com.vala.gestionStagiaires.repository.projection;

import java.time.Instant;

/** Dates de soumission et de décision d'une candidature tranchée. */
public record DelaiDecision(Instant dateSoumission, Instant dateDecision) {
}
