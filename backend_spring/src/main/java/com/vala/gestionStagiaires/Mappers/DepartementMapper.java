package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.entities.Departement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartementMapper {

    DepartementDto fromDepartement(Departement departement);

    @Mapping(target = "stagiaires", ignore = true)
    Departement fromDepartementDto(DepartementDto departementDto);
}
