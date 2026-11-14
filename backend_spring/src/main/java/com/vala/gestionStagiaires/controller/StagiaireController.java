package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.service.StagiaireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class StagiaireController {
    @Autowired
    StagiaireService stagiaireService;

    @GetMapping("/stagiaires")
    public ResponseEntity<List<StagiaireDto>> getAllStagiaires(){
        return ResponseEntity.ok(stagiaireService.getAllStagiares());
    }

    @GetMapping("/stagiaires/{id}")
    public ResponseEntity<StagiaireDto> getStagiaire(@PathVariable(name = "id") Long id)throws StagiaireNotFoundException {
        return ResponseEntity.ok(stagiaireService.getStagiaire(id));
    }

    @GetMapping("/stagiaires/search")
    public ResponseEntity<List<StagiaireDto>> searchStagiaire(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return ResponseEntity.ok(stagiaireService.searchStagiaire(keyword));
    }

    @PostMapping("/stagiaires")
    public ResponseEntity<StagiaireDto> saveStagiaire(@Valid @RequestBody StagiaireDto stagiaireDto){
        StagiaireDto created = stagiaireService.addStagiare(stagiaireDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/stagiaires/{id}")
    public ResponseEntity<Void> deleteStagiaire(@PathVariable Long id){
        stagiaireService.deleteStagiaire(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stagiaires/{id}")
    public ResponseEntity<StagiaireDto> updateStagiaire(@Valid @RequestBody StagiaireDto stagiaireDto, @PathVariable Long id) {
        return ResponseEntity.ok(stagiaireService.updateStagiaire(stagiaireDto, id));
    }
    @GetMapping("/statistiques/departements")
    public ResponseEntity<Map<String, Long>> getStatsParDepartement() {
        return ResponseEntity.ok(stagiaireService.countStagiairesParDepartement());
    }
    @GetMapping("/statistiques/filieres")
    public ResponseEntity<Map<String, Long>> getStatistiquesParFiliere() {
        return ResponseEntity.ok(stagiaireService.getNombreStagiairesParFiliere());
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
