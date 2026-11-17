package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Statistiques du tableau de bord.
 * Les répartitions sont des objets JSON {libellé: effectif}, triés par effectif décroissant.
 */
@RestController
@RequestMapping("/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    @GetMapping("/departements")
    public ResponseEntity<Map<String, Long>> parDepartement() {
        return ResponseEntity.ok(statistiqueService.stagiairesParDepartement());
    }

    @GetMapping("/filieres")
    public ResponseEntity<Map<String, Long>> parFiliere() {
        return ResponseEntity.ok(statistiqueService.stagiairesParFiliere());
    }

    @GetMapping("/organismes")
    public ResponseEntity<Map<String, Long>> parOrganisme() {
        return ResponseEntity.ok(statistiqueService.stagiairesParOrganisme());
    }

    @GetMapping("/encadrants")
    public ResponseEntity<Map<String, Long>> parEncadrant() {
        return ResponseEntity.ok(statistiqueService.stagiairesParEncadrant());
    }

    @GetMapping("/stages/statuts")
    public ResponseEntity<Map<StageStatus, Long>> stagesParStatut() {
        return ResponseEntity.ok(statistiqueService.stagesParStatut());
    }

    /** Stages en cours qui se terminent dans les {@code jours} prochains jours (1 à 365, 30 par défaut). */
    @GetMapping("/stages/echeances")
    public ResponseEntity<List<StageDto>> stagesAEcheance(
            @RequestParam(defaultValue = "" + StatistiqueService.JOURS_ECHEANCE_PAR_DEFAUT) int jours) {
        return ResponseEntity.ok(statistiqueService.stagesAEcheance(jours));
    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeStatistiques> resume() {
        return ResponseEntity.ok(statistiqueService.resume());
    }
}
