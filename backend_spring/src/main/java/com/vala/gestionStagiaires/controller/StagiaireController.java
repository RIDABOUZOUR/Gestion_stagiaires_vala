package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.service.StagiaireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StagiaireController {
    @Autowired
    StagiaireService stagiaireService;

    @GetMapping("/stagiaires")
    public ResponseEntity<List<StagiaireDto>> getAllStagiaires(){
        return ResponseEntity.ok(stagiaireService.getAllStagiares());
    }

    /**
     * Recherche multicritère paginée, ex. :
     * {@code /stagiaires/page?keyword=ali&departementId=2&status=EN_COURS&page=0&size=20&sort=nom,asc}
     */
    @GetMapping("/stagiaires/page")
    public ResponseEntity<PageResponse<StagiaireDto>> rechercherStagiaires(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) StageStatus status,
            @RequestParam(required = false) String organisme,
            @PageableDefault(size = 20) @SortDefault(sort = {"nom", "prenom"}) Pageable pageable) {
        StagiaireFiltre filtre = new StagiaireFiltre(keyword, departementId, stageId, status, organisme);
        return ResponseEntity.ok(stagiaireService.rechercherStagiaires(filtre, pageable));
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
}
