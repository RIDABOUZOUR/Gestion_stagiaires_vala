package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;

import java.util.List;

public interface EncadrantService {
    List<EncadrantDto> getEncadrant();
    EncadrantDto addEncadrant(EncadrantDto encadrantDto);

    List<EncadrantDto> searchEncadrant(String keyword);
    void deleteEncadrant(Long id);
    EncadrantDto updateEncadrant(EncadrantDto encadrantDto, Long id);
    EncadrantDto getEncadrantById(Long id);
}
