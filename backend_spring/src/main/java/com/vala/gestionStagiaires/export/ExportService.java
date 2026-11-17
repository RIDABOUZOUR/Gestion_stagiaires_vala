package com.vala.gestionStagiaires.export;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import org.springframework.data.domain.Sort;

/**
 * Exports CSV des listes, avec les mêmes filtres et le même tri que les
 * recherches paginées. Au-delà de {@link #MAX_LIGNES} lignes, l'export est refusé.
 */
public interface ExportService {

    int MAX_LIGNES = 10_000;

    ExportCsv exporterStagiaires(StagiaireFiltre filtre, Sort tri);

    ExportCsv exporterStages(StageFiltre filtre, Sort tri);

    ExportCsv exporterCandidatures(CandidatureFiltre filtre, Sort tri);
}
