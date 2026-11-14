package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.entities.Encadrant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EncadrantMapper {

    EncadrantDto fromEncadrant(Encadrant encadrant);

    @Mapping(target = "stages", ignore = true)
    Encadrant fromEncadrantDto(EncadrantDto encadrantDto);
}
