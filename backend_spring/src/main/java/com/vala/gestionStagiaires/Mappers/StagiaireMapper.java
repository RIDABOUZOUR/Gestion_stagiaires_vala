package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Stagiaire;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {StageMapper.class, DepartementMapper.class})
public interface StagiaireMapper {

    StagiaireDto fromStagiaire(Stagiaire stagiaire);

    Stagiaire fromStagiaireDto(StagiaireDto stagiaireDto);
}
