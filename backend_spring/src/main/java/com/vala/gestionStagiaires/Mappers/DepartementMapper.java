package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.entities.Departement;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepartementMapper {

    @Autowired private EncadrantMapper encadrantMapper;
    public DepartementDto fromDepartement(Departement departement){
        DepartementDto departementDto = new DepartementDto();
        BeanUtils.copyProperties(departement,departementDto);
        return departementDto;
    }
    public Departement fromDepartementDto(DepartementDto departementDto){
        Departement departement = new Departement();
        BeanUtils.copyProperties(departementDto,departement);
        return departement;
    }
}
