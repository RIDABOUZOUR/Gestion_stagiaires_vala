package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.EncadrantMapper;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.StageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class StageServiceImpl implements StageService{
    @Autowired
    private StageRepository stageRepository;
    @Autowired
    private StageMapper stageMapper;
    @Autowired
    private EncadrantMapper encadrantMapper;
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

    @Override
    public void deleteStage(Long id) {
        stageRepository.deleteById(id);
    }

    @Override
    public Stage updateStage(StageDto stageDto, Long id) {
        Stage existingStage = stageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stage non trouvé avec id: " + id));

        existingStage.setSujet(stageDto.getSujet());
        existingStage.setEncadrant(encadrantMapper.fromEncadrantDto(stageDto.getEncadrant()));
        existingStage.setDateDebut(stageDto.getDateDebut());
        existingStage.setDateFin(stageDto.getDateFin());
        existingStage.setStatus(stageDto.getStatus());
        return stageRepository.save(existingStage);
    }

    @Override
    public StageDto getStageById(Long id) {
       return stageMapper.fromStage(stageRepository.findById(id).orElseThrow());
    }

}
