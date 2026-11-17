package com.vala.gestionStagiaires.repository.projection;

import java.time.LocalDate;

/** Semaine du dernier rapport déposé par un stagiaire. */
public record DernierRapport(Long stagiaireId, LocalDate semaine) {
}
