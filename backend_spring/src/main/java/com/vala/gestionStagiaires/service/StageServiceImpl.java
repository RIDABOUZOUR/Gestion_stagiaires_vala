package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.repository.StageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class StageServiceImpl implements StageService{
    @Autowired
    private StageRepository stageRepository;
    @Autowired
    private StageMapper stageMapper;
    @Override
    public List<StageDto> getStages() {
        List<Stage> stages = stageRepository.findAll();
        return stages.stream()
                .map(stageMapper::fromStage)
                .toList();
    }

    @Override
    public Stage addStage(StageDto stageDto) {
        Stage stage = stageMapper.fromStageDto(stageDto);
        return stageRepository.save(stage);
    }

    @Override
    public List<StageDto> searchStage(String keyword) {
        List<Stage> stages = stageRepository.findBySujetIgnoreCaseContaining(keyword);
        List<StageDto> stageDtos = new ArrayList<>();
        for (Stage stage : stages) {
            StageDto stageDto = stageMapper.fromStage(stage);
            stageDtos.add(stageDto);
        }
        return stageDtos;
    }
}
