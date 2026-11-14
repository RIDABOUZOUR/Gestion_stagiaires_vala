package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;

import java.util.List;

public interface DepartementService {
    List<DepartementDto> getDepartement();
    DepartementDto addDepartement(DepartementDto departementDto);

    List<DepartementDto> searchDepartement(String keyword);
    void deleteDepartement(Long id);
    DepartementDto updateDepartement(DepartementDto departementDto,Long id);
    DepartementDto getDepartementById(Long id);
}
