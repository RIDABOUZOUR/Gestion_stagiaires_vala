package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.Mappers.EncadrantMapper;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class EncadrantServiceImpl implements EncadrantService {
    @Autowired
    private EncadrantRepository encadrantRepository;
    @Autowired
    private EncadrantMapper encadrantMapper;
    @Override
    public List<EncadrantDto> getEncadrant() {
        List<Encadrant> encadrants = encadrantRepository.findAll();
        List<EncadrantDto> encadrantDtos = new ArrayList<>();
        for (Encadrant encadrant : encadrants) {
            EncadrantDto encadrantDto = encadrantMapper.fromEncadrant(encadrant);
            encadrantDtos.add(encadrantDto);
        }
        return encadrantDtos;
    }

    @Override
    public Encadrant addEncadrant(EncadrantDto encadrantDto) {
        Encadrant encadrant = encadrantMapper.fromEncadrantDto(encadrantDto);
        return encadrantRepository.save(encadrant);

    }

    @Override
    public List<EncadrantDto> searchEncadrant(String keyword) {
        List<Encadrant> encadrants = encadrantRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(keyword,keyword);
        List<EncadrantDto> encadrantDtos = new ArrayList<>();
        for (Encadrant encadrant : encadrants) {
            EncadrantDto encadrantDto = encadrantMapper.fromEncadrant(encadrant);
            encadrantDtos.add(encadrantDto);
        }
        return encadrantDtos;
    }
}
