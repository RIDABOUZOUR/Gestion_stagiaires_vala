package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Stagiaire;

import java.util.List;
import java.util.Map;

public interface StagiaireService {
    List<StagiaireDto> getAllStagiares();
    Stagiaire addStagiare(StagiaireDto stagiaireDto);
    void deleteStagiaire(Long id);
    Stagiaire updateStagiaire( StagiaireDto updatedStagiare,Long id);
    StagiaireDto getStagiaire(Long id);
    Map<String, Long> countStagiairesParDepartement();
    List<StagiaireDto> searchStagiaire(String keyword);
    Map<String, Long> getNombreStagiairesParFiliere();
    public Map<String, Long> getStagiairesParOrganisme();
    public Map<String, Long> getStagiairesParEncadrant();

}
