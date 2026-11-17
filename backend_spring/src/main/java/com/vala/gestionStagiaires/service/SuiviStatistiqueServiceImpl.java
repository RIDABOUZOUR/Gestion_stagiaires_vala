package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStagiaire;
import com.vala.gestionStagiaires.DTOs.suivi.AssiduiteStatistiques;
import com.vala.gestionStagiaires.DTOs.suivi.RapportEnRetard;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.enums.StatutPresence;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptagePresence;
import com.vala.gestionStagiaires.repository.projection.DernierRapport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SuiviStatistiqueServiceImpl implements SuiviStatistiqueService {

    private final StagiaireRepository stagiaireRepository;
    private final PresenceRepository presenceRepository;
    private final RapportHebdomadaireRepository rapportRepository;
    private final Clock clock;

    @Override
    public AssiduiteStatistiques assiduite() {
        List<Stagiaire> actifs = stagiaireRepository.findByStageStatus(StageStatus.EN_COURS);

        Map<Long, Map<StatutPresence, Long>> comptages = new HashMap<>();
        for (ComptagePresence c : presenceRepository.compterParStagiaireEtStatut(StageStatus.EN_COURS)) {
            comptages.computeIfAbsent(c.stagiaireId(), id -> new EnumMap<>(StatutPresence.class))
                    .merge(c.statut(), c.nombre(), Long::sum);
        }

        List<AssiduiteStagiaire> details = new ArrayList<>();
        Map<String, long[]> parDepartement = new HashMap<>(); // libellé → {présents, saisis}
        long presentsTotal = 0;
        long saisisTotal = 0;

        for (Stagiaire s : actifs) {
            Map<StatutPresence, Long> c = comptages.getOrDefault(s.getId(), Map.of());
            long presents = c.getOrDefault(StatutPresence.PRESENT, 0L);
            long retards = c.getOrDefault(StatutPresence.RETARD, 0L);
            long absences = c.getOrDefault(StatutPresence.ABSENT, 0L);
            long justifiees = c.getOrDefault(StatutPresence.ABSENCE_JUSTIFIEE, 0L);
            long saisis = presents + retards + absences + justifiees;
            String departement = s.getDepartement() != null ? s.getDepartement().getNom() : StatistiqueService.NON_RENSEIGNE;

            details.add(new AssiduiteStagiaire(s.getId(), s.getNom(), s.getPrenom(), departement,
                    saisis, presents, retards, absences, justifiees, pourcentage(presents + retards, saisis)));

            if (saisis > 0) {
                long[] cumul = parDepartement.computeIfAbsent(departement, d -> new long[2]);
                cumul[0] += presents + retards;
                cumul[1] += saisis;
            }
            presentsTotal += presents + retards;
            saisisTotal += saisis;
        }

        details.sort(Comparator.comparing(AssiduiteStagiaire::tauxPresence, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(AssiduiteStagiaire::nom, Comparator.nullsLast(Comparator.naturalOrder())));

        Map<String, Double> tauxParDepartement = parDepartement.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), pourcentage(e.getValue()[0], e.getValue()[1])))
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        return new AssiduiteStatistiques(pourcentage(presentsTotal, saisisTotal), tauxParDepartement, details);
    }

    @Override
    public List<RapportEnRetard> rapportsEnRetard(int jours) {
        LocalDate today = LocalDate.now(clock);
        LocalDate seuil = today.minusDays(Math.max(1, Math.min(jours, 365)));

        Map<Long, LocalDate> derniers = rapportRepository.derniereSemaineParStagiaire(StageStatus.EN_COURS).stream()
                .collect(Collectors.toMap(DernierRapport::stagiaireId, DernierRapport::semaine));

        List<RapportEnRetard> enRetard = new ArrayList<>();
        for (Stagiaire s : stagiaireRepository.findByStageStatus(StageStatus.EN_COURS)) {
            LocalDate dernier = derniers.get(s.getId());
            // Sans rapport, on compte depuis le début du stage.
            LocalDate reference = dernier != null ? dernier : s.getStage().getDateDebut();
            if (reference != null && reference.isBefore(seuil)) {
                enRetard.add(new RapportEnRetard(s.getId(), s.getNom(), s.getPrenom(), s.getEmail(),
                        s.getStage().getSujet(), nomComplet(s.getStage().getEncadrant()), dernier,
                        ChronoUnit.DAYS.between(reference, today)));
            }
        }
        enRetard.sort(Comparator.comparingLong(RapportEnRetard::joursSansRapport).reversed());
        return enRetard;
    }

    // ------------------------------------------------------------------

    static Double pourcentage(long numerateur, long denominateur) {
        return StatistiquesUtils.pourcentage(numerateur, denominateur);
    }

    private static String nomComplet(Encadrant e) {
        return e == null ? null : (e.getNom() + " " + e.getPrenom()).trim();
    }
}
