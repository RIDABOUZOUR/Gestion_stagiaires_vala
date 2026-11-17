package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService {

    private static final int JOURS_ECHEANCE_MAX = 365;

    private final StagiaireRepository stagiaireRepository;
    private final StageRepository stageRepository;
    private final EncadrantRepository encadrantRepository;
    private final DepartementRepository departementRepository;
    private final StageMapper stageMapper;
    private final Clock clock;

    @Override
    public Map<String, Long> stagiairesParDepartement() {
        return trier(regrouper(departementRepository.compterStagiairesParDepartement()));
    }

    @Override
    public Map<String, Long> stagiairesParFiliere() {
        return trier(regrouper(stagiaireRepository.compterParFiliere()));
    }

    @Override
    public Map<String, Long> stagiairesParOrganisme() {
        return trier(regrouper(stagiaireRepository.compterParOrganisme()));
    }

    @Override
    public Map<String, Long> stagiairesParEncadrant() {
        List<ComptageParEncadrant> lignes = encadrantRepository.compterStagiairesParEncadrant();

        // Deux encadrants homonymes restent distincts : on ajoute leur id au libellé.
        Map<String, Long> occurrences = lignes.stream()
                .collect(Collectors.groupingBy(StatistiqueServiceImpl::nomComplet, Collectors.counting()));
        Map<String, Long> resultat = new HashMap<>();
        for (ComptageParEncadrant ligne : lignes) {
            String libelle = nomComplet(ligne);
            if (occurrences.get(libelle) > 1) {
                libelle += " (#" + ligne.encadrantId() + ")";
            }
            resultat.put(libelle, ligne.nombre());
        }
        return trier(resultat);
    }

    @Override
    public Map<StageStatus, Long> stagesParStatut() {
        Map<StageStatus, Long> resultat = new EnumMap<>(StageStatus.class);
        for (StageStatus status : StageStatus.values()) {
            resultat.put(status, 0L);
        }
        for (ComptageParStatut ligne : stageRepository.compterParStatut()) {
            if (ligne.status() != null) {
                resultat.put(ligne.status(), ligne.nombre());
            }
        }
        return resultat;
    }

    @Override
    public List<StageDto> stagesAEcheance(int jours) {
        LocalDate today = LocalDate.now(clock);
        return stageRepository
                .findByStatusAndDateFinBetweenOrderByDateFinAsc(StageStatus.EN_COURS, today, today.plusDays(borner(jours)))
                .stream()
                .map(stageMapper::fromStage)
                .toList();
    }

    @Override
    public ResumeStatistiques resume() {
        LocalDate today = LocalDate.now(clock);
        int jours = JOURS_ECHEANCE_PAR_DEFAUT;
        return new ResumeStatistiques(
                stagiaireRepository.count(),
                stagiaireRepository.countByStageStatus(StageStatus.EN_COURS),
                stageRepository.count(),
                stageRepository.countByStatus(StageStatus.EN_COURS),
                stageRepository.countByStatus(StageStatus.PLANIFIE),
                stageRepository.countByStatusAndDateFinBetween(StageStatus.EN_COURS, today, today.plusDays(jours)),
                jours,
                encadrantRepository.count(),
                departementRepository.count());
    }

    // ------------------------------------------------------------------

    private static int borner(int jours) {
        return Math.max(1, Math.min(jours, JOURS_ECHEANCE_MAX));
    }

    private static String nomComplet(ComptageParEncadrant ligne) {
        return libelle((nullToEmpty(ligne.nom()) + " " + nullToEmpty(ligne.prenom())).trim());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String libelle(String valeur) {
        return StatistiquesUtils.libelle(valeur);
    }

    /**
     * Normalise les libellés et additionne les lignes qui deviennent identiques
     * (ex. {@code null} et {@code ""}, ou « ENSA » et « ENSA  »).
     */
    private static Map<String, Long> regrouper(List<ComptageParLibelle> lignes) {
        return lignes.stream().collect(Collectors.toMap(
                ligne -> libelle(ligne.libelle()),
                ComptageParLibelle::nombre,
                Long::sum));
    }

    private static Map<String, Long> trier(Map<String, Long> valeurs) {
        return StatistiquesUtils.trierDecroissant(valeurs);
    }
}
