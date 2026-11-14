package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.entities.Stage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StageMapper {
    @Autowired private EncadrantMapper encadrantMapper;
    @Autowired private DepartementMapper departementMapper;
    public StageDto fromStage(Stage stage){
        StageDto stageDto = new StageDto();
        BeanUtils.copyProperties(stage,stageDto);
        stageDto.setEncadrant(encadrantMapper.fromEncadrant(stage.getEncadrant()));
        return stageDto;
    }
    public Stage fromStageDto(StageDto stageDto){
        Stage stage = new Stage();
        BeanUtils.copyProperties(stageDto,stage);
        stage.setEncadrant(encadrantMapper.fromEncadrantDto(stageDto.getEncadrant()));
        return stage;
    }
}
