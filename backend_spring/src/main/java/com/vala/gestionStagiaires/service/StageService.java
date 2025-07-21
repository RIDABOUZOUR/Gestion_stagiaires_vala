package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.entities.Stage;

import java.util.List;

public interface StageService {
    List<StageDto> getStages();
    Stage addStage(StageDto stageDto);

    List<StageDto> searchStage(String keyword);
    void deleteStage(Long id);
    Stage updateStage(StageDto stageDto, Long id);
    StageDto getStageById(Long id);
}
