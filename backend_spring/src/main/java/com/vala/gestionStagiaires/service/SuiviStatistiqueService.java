package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStatistiques;
import com.vala.gestionStagiaires.DTOs.suivi.RapportEnRetard;

import java.util.List;

/** Indicateurs de suivi des stagiaires en cours de stage. */
public interface SuiviStatistiqueService {

    int JOURS_SANS_RAPPORT_PAR_DEFAUT = 14;

    AssiduiteStatistiques assiduite();

    /** Stagiaires en cours sans rapport depuis plus de {@code jours} jours (1 à 365), les plus en retard d'abord. */
    List<RapportEnRetard> rapportsEnRetard(int jours);
}
