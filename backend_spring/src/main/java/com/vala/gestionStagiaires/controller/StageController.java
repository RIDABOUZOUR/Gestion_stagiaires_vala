package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.suivi.StageHistoriqueDto;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.securite.SecuriteService;
import com.vala.gestionStagiaires.service.StageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StageController {
    @Autowired
    private StageService stageService;

    @Autowired
    private SecuriteService securite;

    private static final Sort TRI_PAR_DEBUT = Sort.by(Sort.Direction.DESC, "dateDebut");

    @GetMapping("/stages")
    public ResponseEntity<List<StageDto>> getStages(){
        // Un encadrant ne voit que ses stages.
        return securite.encadrantRestreint()
                .map(enc -> ResponseEntity.ok(stagesDe(new StageFiltre(null, null, enc))))
                .orElseGet(() -> ResponseEntity.ok(stageService.getStages()));
    }

    @PostMapping("/stages")
    public ResponseEntity<StageDto> addStage(@Valid @RequestBody StageDto stageDto){
        StageDto created = stageService.addStage(stageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Recherche multicritère paginée, ex. :
     * {@code /stages/page?keyword=ia&status=EN_COURS&encadrantId=1&page=0&size=20&sort=dateDebut,desc}
     */
    @GetMapping("/stages/page")
    public ResponseEntity<PageResponse<StageDto>> rechercherStages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StageStatus status,
            @RequestParam(required = false) Long encadrantId,
            @PageableDefault(size = 20) @SortDefault(sort = "dateDebut", direction = Sort.Direction.DESC) Pageable pageable) {
        Long encadrant = securite.encadrantRestreint().orElse(encadrantId);
        return ResponseEntity.ok(stageService.rechercherStages(new StageFiltre(keyword, status, encadrant), pageable));
    }

    @GetMapping("/stages/search")
    public ResponseEntity<List<StageDto>> searchStage(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return securite.encadrantRestreint()
                .map(enc -> ResponseEntity.ok(stagesDe(new StageFiltre(keyword, null, enc))))
                .orElseGet(() -> ResponseEntity.ok(stageService.searchStage(keyword)));
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStagiaire(@PathVariable Long id) {
        stageService.deleteStage(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/stages/{id}")
    public ResponseEntity<StageDto> updateEncadrant(@Valid @RequestBody StageDto stageDto, @PathVariable Long id){
        return ResponseEntity.ok(stageService.updateStage(stageDto,id));
    }

    @GetMapping("/stages/{id}")
    @PreAuthorize("@securite.peutAccederStage(#id)")
    public ResponseEntity<StageDto> getStageById(@PathVariable Long id){
        return ResponseEntity.ok(stageService.getStageById(id));
    }

    // ------------------------------------------------------------------
    // Actions sur le cycle de vie (PLANIFIE → EN_COURS → TERMINE, ou ANNULE)
    // ------------------------------------------------------------------

    @GetMapping("/stages/{id}/historique")
    @PreAuthorize("@securite.peutAccederStage(#id)")
    public ResponseEntity<List<StageHistoriqueDto>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.getHistorique(id));
    }

    @PatchMapping("/stages/{id}/demarrer")
    public ResponseEntity<StageDto> demarrerStage(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.changerStatut(id, StageStatus.EN_COURS));
    }

    @PatchMapping("/stages/{id}/terminer")
    public ResponseEntity<StageDto> terminerStage(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.changerStatut(id, StageStatus.TERMINE));
    }

    @PatchMapping("/stages/{id}/annuler")
    public ResponseEntity<StageDto> annulerStage(@PathVariable Long id) {
        return ResponseEntity.ok(stageService.changerStatut(id, StageStatus.ANNULE));
    }

    private List<StageDto> stagesDe(StageFiltre filtre) {
        return stageService.rechercherStages(filtre, Pageable.unpaged(TRI_PAR_DEBUT)).content();
    }
}
