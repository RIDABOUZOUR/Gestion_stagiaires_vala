package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.Mappers.DepartementMapper;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class DepartementServiceImpl implements DepartementService{
    private final DepartementRepository departementRepository;
    private final StagiaireRepository stagiaireRepository;
    private final DepartementMapper departementMapper;
    @Override
    @Transactional(readOnly = true)
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
    public DepartementDto addDepartement(DepartementDto departementDto) {
        Departement departement = departementMapper.fromDepartementDto(departementDto);
        return departementMapper.fromDepartement(departementRepository.save(departement));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementDto> searchDepartement(String keyword) {
        List<Departement> departements = departementRepository.findByNomIgnoreCaseContaining(keyword);
        List<DepartementDto> departementDtos = new ArrayList<>();
        for (Departement departement : departements) {
            DepartementDto departementDto = departementMapper.fromDepartement(departement);
            departementDtos.add(departementDto);
        }
        return departementDtos;
    }

    @Override
    public void deleteDepartement(Long id) {
        if (!departementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Département", id);
        }
        if (stagiaireRepository.existsByDepartementId(id)) {
            throw new BusinessRuleException("Impossible de supprimer ce département : des stagiaires y sont encore rattachés.");
        }
        departementRepository.deleteById(id);
    }

    @Override
    public DepartementDto updateDepartement(DepartementDto departementDto, Long id) {
        Departement existingDepartement = departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));
        existingDepartement.setNom(departementDto.getNom());
        existingDepartement.setResponsable(departementDto.getResponsable());
        return departementMapper.fromDepartement(departementRepository.save(existingDepartement));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartementDto getDepartementById(Long id) {
        return departementMapper.fromDepartement(departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id)));
    }
}
