package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Stagiaire;

import java.util.List;

public interface StagiaireService {
    List<StagiaireDto> getAllStagiares();
    Stagiaire addStagiare(StagiaireDto stagiaireDto);
    void deleteStagiaire(Long id);
    Stagiaire editStagiaire(Long id, StagiaireDto updatedStagiare);
    StagiaireDto getStagiaire(Long id);

    List<StagiaireDto> searchStagiaire(String keyword);

}
