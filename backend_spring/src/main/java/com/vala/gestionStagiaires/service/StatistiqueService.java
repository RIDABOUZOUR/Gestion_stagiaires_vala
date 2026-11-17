package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.enums.StageStatus;

import java.util.List;
import java.util.Map;

/**
 * Indicateurs du tableau de bord.
 * Les répartitions sont triées par effectif décroissant (puis par libellé).
 */
public interface StatistiqueService {

    /** Libellé utilisé quand la donnée n'est pas renseignée (null ou vide). */
    String NON_RENSEIGNE = StatistiquesUtils.NON_RENSEIGNE;

    /** Horizon par défaut des stages « à échéance », en jours. */
    int JOURS_ECHEANCE_PAR_DEFAUT = 30;

    Map<String, Long> stagiairesParDepartement();

    Map<String, Long> stagiairesParFiliere();

    Map<String, Long> stagiairesParOrganisme();

    Map<String, Long> stagiairesParEncadrant();

    /** Nombre de stages pour chaque statut (tous les statuts sont présents, éventuellement à 0). */
    Map<StageStatus, Long> stagesParStatut();

    /** Stages EN_COURS dont la fin tombe entre aujourd'hui et aujourd'hui + {@code jours} (1 à 365). */
    List<StageDto> stagesAEcheance(int jours);

    ResumeStatistiques resume();
}
