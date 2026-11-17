package com.vala.gestionStagiaires.DTOs.stats;

import java.util.Map;

/**
 * Profil des stagiaires (éventuellement limité à un département).
 * Répartitions triées par effectif décroissant ; valeurs absentes sous « Non renseigné ».
 */
public record ProfilStagiaires(
        long total,
        Map<String, Long> parSexe,
        Map<String, Long> parNiveau,
        Map<String, Long> parFiliere,
        Map<String, Map<String, Long>> parDepartementEtSexe,
        Map<String, Map<String, Long>> parDepartementEtNiveau) {
}
