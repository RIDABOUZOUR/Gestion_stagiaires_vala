package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.entities.Stage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EncadrantMapper.class})
public interface StageMapper {

    StageDto fromStage(Stage stage);

    @Mapping(target = "stagiaire", ignore = true)
    Stage fromStageDto(StageDto stageDto);
}
