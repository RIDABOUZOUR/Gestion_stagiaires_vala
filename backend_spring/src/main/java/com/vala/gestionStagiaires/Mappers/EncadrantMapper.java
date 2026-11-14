package com.vala.gestionStagiaires.Mappers;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.entities.Encadrant;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class EncadrantMapper {
    public EncadrantDto fromEncadrant(Encadrant encadrant){
        EncadrantDto encadrantDto = new EncadrantDto();
        BeanUtils.copyProperties(encadrant,encadrantDto);
        return encadrantDto;
    }
    public Encadrant fromEncadrantDto(EncadrantDto encadrantDto){
        Encadrant encadrant = new Encadrant();
        BeanUtils.copyProperties( encadrantDto,encadrant);
        return encadrant;
    }
}
