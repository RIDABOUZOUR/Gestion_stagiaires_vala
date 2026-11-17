package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.export.ExportCsv;
import com.vala.gestionStagiaires.export.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exports CSV. Mêmes paramètres de filtre et de tri ({@code sort}) que les
 * routes {@code /page} correspondantes, sans pagination.
 */
@RestController
@RequiredArgsConstructor
public class ExportController {

    public static final MediaType TEXT_CSV = new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8);

    private final ExportService exportService;

    @GetMapping("/stagiaires/export.csv")
    public ResponseEntity<byte[]> exporterStagiaires(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) Long stageId,
            @RequestParam(required = false) StageStatus status,
            @RequestParam(required = false) String organisme,
            @SortDefault(sort = {"nom", "prenom"}) Sort sort) {
        return reponse(exportService.exporterStagiaires(
                new StagiaireFiltre(keyword, departementId, stageId, status, organisme), sort));
    }

    @GetMapping("/stages/export.csv")
    public ResponseEntity<byte[]> exporterStages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StageStatus status,
            @RequestParam(required = false) Long encadrantId,
            @SortDefault(sort = "dateDebut", direction = Sort.Direction.DESC) Sort sort) {
        return reponse(exportService.exporterStages(new StageFiltre(keyword, status, encadrantId), sort));
    }

    @GetMapping("/candidatures/export.csv")
    public ResponseEntity<byte[]> exporterCandidatures(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CandidatureStatus statut,
            @RequestParam(required = false) Long departementId,
            @SortDefault(sort = "dateSoumission", direction = Sort.Direction.DESC) Sort sort) {
        return reponse(exportService.exporterCandidatures(new CandidatureFiltre(keyword, statut, departementId), sort));
    }

    private static ResponseEntity<byte[]> reponse(ExportCsv export) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(export.nomFichier()).build().toString())
                .body(export.contenu());
    }
}
