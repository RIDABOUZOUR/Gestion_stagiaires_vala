package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import org.springframework.data.domain.Pageable;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;

import java.util.List;

public interface StagiaireService {
    /** Recherche multicritère paginée (triable via {@code sort}). */
    PageResponse<StagiaireDto> rechercherStagiaires(StagiaireFiltre filtre, Pageable pageable);
    List<StagiaireDto> getAllStagiares();
    StagiaireDto addStagiare(StagiaireDto stagiaireDto);
    void deleteStagiaire(Long id);
    StagiaireDto updateStagiaire( StagiaireDto updatedStagiare,Long id);
    StagiaireDto getStagiaire(Long id);
    List<StagiaireDto> searchStagiaire(String keyword);

}
