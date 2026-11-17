package com.vala.gestionStagiaires.repository.projection;

/** Ligne de statistique : un libellé (filière, organisme, département…) et un effectif. */
public record ComptageParLibelle(String libelle, Long nombre) {
}
