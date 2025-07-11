package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.service.StagiaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
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
    @DeleteMapping("/stagiaires/id/{id}")
    public void deleteStagiaire(@PathVariable Long id){
        stagiaireService.deleteStagiaire(id);
    }

}
