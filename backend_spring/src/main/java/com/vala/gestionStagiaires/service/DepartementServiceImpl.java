package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.Mappers.DepartementMapper;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class DepartementServiceImpl implements DepartementService{
    @Autowired
    private DepartementRepository departementRepository;
    @Autowired
    private DepartementMapper departementMapper;
    @Override
    public List<DepartementDto> getDepartement() {
        List<Departement> departements = departementRepository.findAll();
        List<DepartementDto> departementDtos = new ArrayList<>();
        for (Departement departement : departements) {
            DepartementDto departementDto = departementMapper.fromDepartement(departement);
            departementDtos.add(departementDto);
        }
        return departementDtos;
    }

    @Override
    public Departement addDepartement(DepartementDto departementDto) {
        Departement departement = departementMapper.fromDepartementDto(departementDto);
        return departementRepository.save(departement);
    }

    @Override
    public List<DepartementDto> searchDepartement(String keyword) {
        List<Departement> departements = departementRepository.findByNomIgnoreCaseContaining(keyword);
        List<DepartementDto> departementDtos = new ArrayList<>();
        for (Departement departement : departements) {
            DepartementDto departementDto = departementMapper.fromDepartement(departement);
            departementDtos.add(departementDto);
        }
        return departementDtos;
    }
}
