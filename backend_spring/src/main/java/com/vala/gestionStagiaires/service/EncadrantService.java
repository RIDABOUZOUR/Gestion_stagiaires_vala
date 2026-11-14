package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.entities.Encadrant;

import java.util.List;

public interface EncadrantService {
    List<EncadrantDto> getEncadrant();
    Encadrant addEncadrant(EncadrantDto encadrantDto);

    List<EncadrantDto> searchEncadrant(String keyword);
    void deleteEncadrant(Long id);
    Encadrant updateEncadrant(EncadrantDto encadrantDto, Long id);
    EncadrantDto getEncadrantById(Long id);
}
