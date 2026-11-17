package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import org.springframework.data.domain.Pageable;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;

import java.util.List;
import java.util.Map;

public interface StagiaireService {
    /** Recherche multicritère paginée (triable via {@code sort}). */
    PageResponse<StagiaireDto> rechercherStagiaires(StagiaireFiltre filtre, Pageable pageable);
    List<StagiaireDto> getAllStagiares();
    StagiaireDto addStagiare(StagiaireDto stagiaireDto);
    void deleteStagiaire(Long id);
    StagiaireDto updateStagiaire( StagiaireDto updatedStagiare,Long id);
    StagiaireDto getStagiaire(Long id);
    Map<String, Long> countStagiairesParDepartement();
    List<StagiaireDto> searchStagiaire(String keyword);
    Map<String, Long> getNombreStagiairesParFiliere();
    public Map<String, Long> getStagiairesParOrganisme();
    public Map<String, Long> getStagiairesParEncadrant();

}
