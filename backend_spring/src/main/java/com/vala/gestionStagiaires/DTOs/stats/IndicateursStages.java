package com.vala.gestionStagiaires.DTOs.stats;

/**
 * Indicateurs de durée et d'issue d'un ensemble de stages.
 *
 * @param dureeMoyenneJours durée moyenne des stages TERMINE, en jours calendaires bornes incluses (1 décimale)
 * @param tauxAchevement    TERMINE / (TERMINE + ANNULE), en % : part des stages clôturés menés à terme
 * @param tauxAnnulation    ANNULE / nombre de stages, en %
 */
public record IndicateursStages(
        long nombreStages,
        long stagesTermines,
        long stagesAnnules,
        Double dureeMoyenneJours,
        Double tauxAchevement,
        Double tauxAnnulation) {
}
