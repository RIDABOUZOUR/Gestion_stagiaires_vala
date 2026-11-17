package com.vala.gestionStagiaires.controller;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques;
import com.vala.gestionStagiaires.DTOs.stats.DureesEtTaux;
import com.vala.gestionStagiaires.DTOs.stats.ProfilStagiaires;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationStatistiques;
import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStatistiques;
import com.vala.gestionStagiaires.DTOs.suivi.RapportEnRetard;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.service.CandidatureService;
import com.vala.gestionStagiaires.service.EvaluationService;
import com.vala.gestionStagiaires.service.StatistiqueService;
import com.vala.gestionStagiaires.service.SuiviStatistiqueService;
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
    private final SuiviStatistiqueService suiviStatistiqueService;
    private final EvaluationService evaluationService;
    private final CandidatureService candidatureService;

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

    /** Durée moyenne, taux d'achèvement et d'annulation des stages : global, par encadrant, par département. */
    @GetMapping("/stages/durees-et-taux")
    public ResponseEntity<DureesEtTaux> dureesEtTaux() {
        return ResponseEntity.ok(statistiqueService.dureesEtTaux());
    }

    /** Profil des stagiaires (sexe, niveau, filière, croisements par département), éventuellement d'un département. */
    @GetMapping("/stagiaires/profil")
    public ResponseEntity<ProfilStagiaires> profil(@RequestParam(required = false) Long departementId) {
        return ResponseEntity.ok(statistiqueService.profil(departementId));
    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeStatistiques> resume() {
        return ResponseEntity.ok(statistiqueService.resume());
    }

    // ---- Suivi ----------------------------------------------------------

    /** Taux de présence des stagiaires en cours : global, par département et par stagiaire. */
    @GetMapping("/suivi/assiduite")
    public ResponseEntity<AssiduiteStatistiques> assiduite() {
        return ResponseEntity.ok(suiviStatistiqueService.assiduite());
    }

    /** Stagiaires en cours sans rapport depuis plus de {@code jours} jours (14 par défaut). */
    @GetMapping("/suivi/rapports-en-retard")
    public ResponseEntity<List<RapportEnRetard>> rapportsEnRetard(
            @RequestParam(defaultValue = "" + SuiviStatistiqueService.JOURS_SANS_RAPPORT_PAR_DEFAUT) int jours) {
        return ResponseEntity.ok(suiviStatistiqueService.rapportsEnRetard(jours));
    }

    // ---- Évaluations ----------------------------------------------------

    /** Notes moyennes (globales, par compétence, département, encadrant, filière), tranches et recommandations. */
    @GetMapping("/evaluations")
    public ResponseEntity<EvaluationStatistiques> evaluations() {
        return ResponseEntity.ok(evaluationService.statistiques());
    }

    // ---- Candidatures ---------------------------------------------------

    /** Statuts, candidatures par mois (12 mois), taux d'acceptation, délai de décision, organismes. */
    @GetMapping("/candidatures")
    public ResponseEntity<CandidatureStatistiques> candidatures() {
        return ResponseEntity.ok(candidatureService.statistiques());
    }
}
