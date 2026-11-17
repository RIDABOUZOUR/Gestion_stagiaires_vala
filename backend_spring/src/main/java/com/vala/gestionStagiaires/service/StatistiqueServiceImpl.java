package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.ResumeStatistiques;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.stats.DureesEtTaux;
import com.vala.gestionStagiaires.DTOs.stats.IndicateursStages;
import com.vala.gestionStagiaires.DTOs.stats.ProfilStagiaires;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptageParEncadrant;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatut;
import com.vala.gestionStagiaires.repository.projection.ProfilLigne;
import com.vala.gestionStagiaires.repository.projection.StageDepartement;
import com.vala.gestionStagiaires.repository.projection.StageDuree;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
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

    @Override
    public DureesEtTaux dureesEtTaux() {
        List<StageDuree> stages = stageRepository.donneesDurees();

        Map<Long, String> nomsEncadrants = new LinkedHashMap<>();
        for (StageDuree s : stages) {
            if (s.encadrantId() != null) {
                nomsEncadrants.putIfAbsent(s.encadrantId(), (nullToEmpty(s.encadrantNom()) + " " + nullToEmpty(s.encadrantPrenom())).trim());
            }
        }
        Map<Long, String> libellesEncadrants = StatistiquesUtils.libellesUniques(nomsEncadrants);
        Map<String, List<StageDuree>> parEncadrant = new HashMap<>();
        for (StageDuree s : stages) {
            String libelle = s.encadrantId() != null ? libellesEncadrants.get(s.encadrantId()) : NON_RENSEIGNE;
            parEncadrant.computeIfAbsent(libelle, k -> new ArrayList<>()).add(s);
        }

        // Un stage compte dans chacun des départements de ses stagiaires.
        Map<Long, Set<String>> departementsParStage = new HashMap<>();
        for (StageDepartement sd : stagiaireRepository.departementsDesStages()) {
            departementsParStage.computeIfAbsent(sd.stageId(), k -> new TreeSet<>()).add(libelle(sd.departement()));
        }
        Map<String, List<StageDuree>> parDepartement = new HashMap<>();
        for (StageDuree s : stages) {
            for (String dep : departementsParStage.getOrDefault(s.stageId(), Set.of(SANS_STAGIAIRE))) {
                parDepartement.computeIfAbsent(dep, k -> new ArrayList<>()).add(s);
            }
        }

        return new DureesEtTaux(indicateurs(stages), indicateursParGroupe(parEncadrant), indicateursParGroupe(parDepartement));
    }

    @Override
    public ProfilStagiaires profil(Long departementId) {
        List<ProfilLigne> lignes = stagiaireRepository.profils(departementId);
        return new ProfilStagiaires(
                lignes.size(),
                compter(lignes, l -> l.sexe() != null ? l.sexe().name() : null),
                compter(lignes, ProfilLigne::niveau),
                compter(lignes, ProfilLigne::filiere),
                croiser(lignes, l -> l.sexe() != null ? l.sexe().name() : null),
                croiser(lignes, ProfilLigne::niveau));
    }

    // ------------------------------------------------------------------

    /** Libellé des stages qui n'ont aucun stagiaire, dans la répartition par département. */
    static final String SANS_STAGIAIRE = "Sans stagiaire";

    static IndicateursStages indicateurs(Collection<StageDuree> stages) {
        long termines = stages.stream().filter(s -> s.status() == StageStatus.TERMINE).count();
        long annules = stages.stream().filter(s -> s.status() == StageStatus.ANNULE).count();
        List<Long> durees = stages.stream()
                .filter(s -> s.status() == StageStatus.TERMINE && s.dateDebut() != null && s.dateFin() != null)
                .map(s -> ChronoUnit.DAYS.between(s.dateDebut(), s.dateFin()) + 1)
                .toList();
        Double dureeMoyenne = durees.isEmpty() ? null
                : Math.round(durees.stream().mapToLong(Long::longValue).average().orElse(0) * 10.0) / 10.0;
        return new IndicateursStages(stages.size(), termines, annules, dureeMoyenne,
                StatistiquesUtils.pourcentage(termines, termines + annules),
                StatistiquesUtils.pourcentage(annules, stages.size()));
    }

    /** Indicateurs par groupe, triés par nombre de stages décroissant puis par libellé. */
    private static Map<String, IndicateursStages> indicateursParGroupe(Map<String, List<StageDuree>> groupes) {
        return groupes.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), indicateurs(e.getValue())))
                .sorted(Comparator.<Map.Entry<String, IndicateursStages>>comparingLong(e -> e.getValue().nombreStages())
                        .reversed().thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private static Map<String, Long> compter(List<ProfilLigne> lignes, Function<ProfilLigne, String> critere) {
        return trier(lignes.stream().collect(Collectors.groupingBy(l -> libelle(critere.apply(l)), Collectors.counting())));
    }

    /** Pour chaque département (du plus grand au plus petit), la répartition selon le critère. */
    private static Map<String, Map<String, Long>> croiser(List<ProfilLigne> lignes,
                                                          Function<ProfilLigne, String> critere) {
        Map<String, List<ProfilLigne>> parDepartement = lignes.stream()
                .collect(Collectors.groupingBy(l -> libelle(l.departement())));
        return parDepartement.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<ProfilLigne>>>comparingInt(e -> e.getValue().size())
                        .reversed().thenComparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> compter(e.getValue(), critere),
                        (a, b) -> a, LinkedHashMap::new));
    }

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
