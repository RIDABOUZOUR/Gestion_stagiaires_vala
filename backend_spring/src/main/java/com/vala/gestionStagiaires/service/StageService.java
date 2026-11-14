package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StageDto;

import java.util.List;

public interface StageService {
    List<StageDto> getStages();
    StageDto addStage(StageDto stageDto);

    List<StageDto> searchStage(String keyword);
    void deleteStage(Long id);
    StageDto updateStage(StageDto stageDto, Long id);
    StageDto getStageById(Long id);
}
