package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.Mappers.DepartementMapper;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.Mappers.StagiaireMapper;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StagiaireServiceImpl implements StagiaireService {

    @Autowired
    private StagiaireRepository stagiaireRepository;
    @Autowired
    private StagiaireMapper mapper;
    @Autowired
    private DepartementMapper departementMapper;
    @Autowired
    private StageMapper stageMapper;

    @Override
    public List<StagiaireDto> getAllStagiares() {
        List<Stagiaire> stagiaires = stagiaireRepository.findAll();
        List<StagiaireDto> stagiaireDtos = new ArrayList<>();
        for (Stagiaire stagiaire : stagiaires) {
            StagiaireDto stagiaireDto = mapper.fromStagiaire(stagiaire);
            stagiaireDtos.add(stagiaireDto);
        }
        return stagiaireDtos;
    }

    @Override
    public Stagiaire addStagiare(StagiaireDto stagiaireDto) {
        Stagiaire stagiaire = mapper.fromStagiaireDto(stagiaireDto);
        return stagiaireRepository.save(stagiaire);

    }

    @Override
    public void deleteStagiaire(Long id) {

        stagiaireRepository.deleteById(id);
    }


    @Override
    public Stagiaire updateStagiaire(StagiaireDto updatedStagiare, Long id) {
        Stagiaire existingStagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé avec id: " + id));

        existingStagiaire.setDateNaissance(updatedStagiare.getDateNaissance());
        existingStagiaire.setTelephone(updatedStagiare.getTelephone());
        existingStagiaire.setPrenom(updatedStagiare.getPrenom());
        existingStagiaire.setNom(updatedStagiare.getNom());
        existingStagiaire.setSexe(updatedStagiare.getSexe());
        existingStagiaire.setEmail(updatedStagiare.getEmail());
        existingStagiaire.setAdresse(updatedStagiare.getAdresse());
        existingStagiaire.setNiveau(updatedStagiare.getNiveau());
        existingStagiaire.setDepartement(departementMapper.fromDepartementDto(updatedStagiare.getDepartement()));
        existingStagiaire.setStage(stageMapper.fromStageDto(updatedStagiare.getStage()));
        return stagiaireRepository.save(existingStagiaire);
    }
    
    @Override
    public StagiaireDto getStagiaire(Long id) {
        Stagiaire stagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new StagiaireNotFoundException(id));
        return mapper.fromStagiaire(stagiaire);
    }

    @Override
    public List<StagiaireDto> searchStagiaire(String keyword) {
        List<Stagiaire> stagiaires = stagiaireRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(keyword,keyword);
        List<StagiaireDto> stagiaireDtos = new ArrayList<>();
        for (Stagiaire stagiaire : stagiaires) {
            StagiaireDto stagiaireDto = mapper.fromStagiaire(stagiaire);
            stagiaireDtos.add(stagiaireDto);
        }
        return stagiaireDtos;
    }


}
