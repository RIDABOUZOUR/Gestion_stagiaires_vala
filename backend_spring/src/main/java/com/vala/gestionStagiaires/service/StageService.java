package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.enums.StageStatus;

import java.util.List;

public interface StageService {
    List<StageDto> getStages();
    StageDto addStage(StageDto stageDto);

    List<StageDto> searchStage(String keyword);
    void deleteStage(Long id);
    StageDto updateStage(StageDto stageDto, Long id);
    StageDto getStageById(Long id);

    /** Fait passer un stage à un nouveau statut en respectant son cycle de vie. */
    StageDto changerStatut(Long id, StageStatus cible);

    /**
     * Met à jour les statuts selon la date du jour (PLANIFIE → EN_COURS → TERMINE).
     * @return le nombre de stages modifiés
     */
    int mettreAJourStatutsAutomatiques();
}
