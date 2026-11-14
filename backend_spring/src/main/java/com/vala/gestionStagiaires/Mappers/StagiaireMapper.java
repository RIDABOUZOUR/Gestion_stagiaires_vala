package com.vala.gestionStagiaires.Mappers;


import com.vala.gestionStagiaires.DTOs.*;
import com.vala.gestionStagiaires.entities.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StagiaireMapper {
    @Autowired private StageMapper stageMapper;
    @Autowired private EncadrantMapper encadrantMapper;
    @Autowired private DepartementMapper departementMapper;
    public StagiaireDto fromStagiaire(Stagiaire stagiaire){
        StagiaireDto stagiaireDto = new StagiaireDto();
        BeanUtils.copyProperties(stagiaire,stagiaireDto);
        stagiaireDto.setStage(stageMapper.fromStage(stagiaire.getStage()));
        stagiaireDto.setDepartement(departementMapper.fromDepartement(stagiaire.getDepartement()));

        return stagiaireDto;
    }
    public Stagiaire fromStagiaireDto(StagiaireDto stagiaireDto){
        Stagiaire stagiaire = new Stagiaire();
        BeanUtils.copyProperties(stagiaireDto,stagiaire);
        stagiaire.setStage(stageMapper.fromStageDto(stagiaireDto.getStage()));
        stagiaire.setDepartement(departementMapper.fromDepartementDto(stagiaireDto.getDepartement()));
        return stagiaire;
    }

}
