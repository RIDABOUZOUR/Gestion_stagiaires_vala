package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.service.StagiaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StagiaireController {
    @Autowired
    StagiaireService stagiaireService;

    @GetMapping("/stagiaires")
    public List<StagiaireDto> getAllStagiaires(){
        return stagiaireService.getAllStagiares();
    }

    @GetMapping("/stagiaires/{id}")
    public StagiaireDto getStagiaire(@PathVariable(name = "id") Long id)throws StagiaireNotFoundException {
        return stagiaireService.getStagiaire(id);
    }

    @GetMapping("/stagiaires/search")
    public List<StagiaireDto> searchStagiaire( @RequestParam(name = "keyword",defaultValue = "") String keyword){
        return stagiaireService.searchStagiaire(keyword);
    }

    @PostMapping("/stagiaires")
    public Stagiaire saveStagiaire(@RequestBody StagiaireDto stagiaireDto){
        return stagiaireService.addStagiare(stagiaireDto);
    }

    @DeleteMapping("/stagiaires/{id}")
    public void deleteStagiaire(@PathVariable Long id){
        stagiaireService.deleteStagiaire(id);
    }

    @PutMapping("/stagiaires/{id}")
    public Stagiaire updateStagiaire(@RequestBody StagiaireDto stagiaireDto, @PathVariable Long id) {
        return stagiaireService.updateStagiaire(stagiaireDto, id);
    }
    @GetMapping("/statistiques/departements")
    public ResponseEntity<Map<String, Long>> getStatsParDepartement() {
        return ResponseEntity.ok(stagiaireService.countStagiairesParDepartement());
    }
    @GetMapping("/statistiques/filieres")
    public ResponseEntity<Map<String, Long>> getStatistiquesParFiliere() {
        Map<String, Long> result = stagiaireService.getNombreStagiairesParFiliere();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistiques/organismes")
    public ResponseEntity<Map<String, Long>> getStagiairesParOrganisme() {
        return ResponseEntity.ok(stagiaireService.getStagiairesParOrganisme());
    }

    @GetMapping("/statistiques/encadrants")
    public ResponseEntity<Map<String, Long>> getStagiairesParEncadrant() {
        return ResponseEntity.ok(stagiaireService.getStagiairesParEncadrant());
    }
}
