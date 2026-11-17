package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.RapportDto;
import com.vala.gestionStagiaires.DTOs.suivi.RapportRequest;

import java.util.List;

public interface RapportService {

    RapportDto deposer(Long stagiaireId, RapportRequest request);

    /** Rapports du stagiaire, de la semaine la plus récente à la plus ancienne. */
    List<RapportDto> lister(Long stagiaireId);

    RapportDto getRapport(Long id);

    /** Modifie un rapport SOUMIS ou A_REVOIR ; il repasse alors à SOUMIS. */
    RapportDto modifier(Long id, RapportRequest request);

    RapportDto valider(Long id, String commentaire);

    RapportDto demanderRevision(Long id, String commentaire);
}
