package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.entities.Departement;

import java.util.List;

public interface DepartementService {
    List<DepartementDto> getDepartement();
    Departement addDepartement(DepartementDto departementDto);

    List<DepartementDto> searchDepartement(String keyword);
}
