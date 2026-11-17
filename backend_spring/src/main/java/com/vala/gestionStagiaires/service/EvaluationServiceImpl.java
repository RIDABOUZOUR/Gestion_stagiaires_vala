package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationDto;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationRequest;
import com.vala.gestionStagiaires.DTOs.evaluation.EvaluationStatistiques;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Evaluation;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.EvaluationRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    /** Bornes basses des tranches de notes (la dernière tranche est ouverte). */
    private static final double[] BORNES = {0, 10, 12, 14, 16};
    private static final String[] TRANCHES = {"< 10", "10 – 12", "12 – 14", "14 – 16", "16 et plus"};

    private final EvaluationRepository evaluationRepository;
    private final StagiaireRepository stagiaireRepository;
    private final Clock clock;

    @Override
    public EvaluationDto enregistrer(Long stagiaireId, EvaluationRequest request) {
        Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new StagiaireNotFoundException(stagiaireId));
        StageStatus status = stagiaire.getStage() != null ? stagiaire.getStage().getStatus() : null;
        if (status != StageStatus.TERMINE) {
            throw new BusinessRuleException("L'évaluation n'est possible qu'une fois le stage terminé (statut actuel : "
                    + status + ").");
        }

        Evaluation evaluation = evaluationRepository.findByStagiaireId(stagiaireId)
                .orElseGet(() -> Evaluation.builder().stagiaire(stagiaire).build());
        evaluation.setNoteGlobale(request.noteGlobale());
        evaluation.setCompetenceTechnique(request.competenceTechnique());
        evaluation.setCommunication(request.communication());
        evaluation.setAutonomie(request.autonomie());
        evaluation.setAssiduite(request.assiduite());
        evaluation.setAppreciation(request.appreciation());
        evaluation.setRecommande(Boolean.TRUE.equals(request.recommande()));
        evaluation.setDateEvaluation(LocalDate.now(clock));
        return EvaluationDto.of(evaluationRepository.save(evaluation));
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationDto getEvaluation(Long stagiaireId) {
        return evaluationRepository.findByStagiaireId(stagiaireId)
                .map(EvaluationDto::of)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aucune évaluation pour le stagiaire avec l'identifiant " + stagiaireId + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluationStatistiques statistiques() {
        List<Evaluation> evaluations = evaluationRepository.findAll();
        long recommandes = evaluations.stream().filter(Evaluation::isRecommande).count();

        Map<String, Double> competences = new LinkedHashMap<>();
        competences.put("Compétence technique", StatistiquesUtils.moyenne(valeurs(evaluations, Evaluation::getCompetenceTechnique)));
        competences.put("Communication", StatistiquesUtils.moyenne(valeurs(evaluations, Evaluation::getCommunication)));
        competences.put("Autonomie", StatistiquesUtils.moyenne(valeurs(evaluations, Evaluation::getAutonomie)));
        competences.put("Assiduité", StatistiquesUtils.moyenne(valeurs(evaluations, Evaluation::getAssiduite)));

        return new EvaluationStatistiques(
                evaluations.size(),
                StatistiquesUtils.moyenne(valeurs(evaluations, Evaluation::getNoteGlobale)),
                StatistiquesUtils.pourcentage(recommandes, evaluations.size()),
                evaluationRepository.compterStagiairesSansEvaluation(StageStatus.TERMINE),
                competences,
                repartition(evaluations),
                StatistiquesUtils.moyenneParLibelle(evaluations,
                        e -> e.getStagiaire().getDepartement() != null ? e.getStagiaire().getDepartement().getNom() : null,
                        Evaluation::getNoteGlobale),
                moyenneParEncadrant(evaluations),
                StatistiquesUtils.moyenneParLibelle(evaluations, e -> e.getStagiaire().getFiliere(), Evaluation::getNoteGlobale));
    }

    // ------------------------------------------------------------------

    private static <N extends Number> List<N> valeurs(List<Evaluation> evaluations, Function<Evaluation, N> champ) {
        return evaluations.stream().map(champ).filter(Objects::nonNull).toList();
    }

    /** Toutes les tranches sont présentes (éventuellement à 0), de la plus basse à la plus haute. */
    private static Map<String, Long> repartition(List<Evaluation> evaluations) {
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (String tranche : TRANCHES) {
            resultat.put(tranche, 0L);
        }
        for (Evaluation e : evaluations) {
            resultat.merge(tranche(e.getNoteGlobale()), 1L, Long::sum);
        }
        return resultat;
    }

    static String tranche(BigDecimal note) {
        double n = note.doubleValue();
        for (int i = BORNES.length - 1; i > 0; i--) {
            if (n >= BORNES[i]) {
                return TRANCHES[i];
            }
        }
        return TRANCHES[0];
    }

    /** Moyenne par encadrant ; deux homonymes restent distincts grâce à leur id. */
    private static Map<String, Double> moyenneParEncadrant(List<Evaluation> evaluations) {
        Map<Long, Encadrant> encadrants = new HashMap<>();
        for (Evaluation e : evaluations) {
            Encadrant enc = e.getStagiaire().getStage() != null ? e.getStagiaire().getStage().getEncadrant() : null;
            if (enc != null) {
                encadrants.putIfAbsent(enc.getId(), enc);
            }
        }
        Map<String, Long> occurrences = encadrants.values().stream()
                .collect(Collectors.groupingBy(EvaluationServiceImpl::nomComplet, Collectors.counting()));
        return StatistiquesUtils.moyenneParLibelle(evaluations, e -> {
            Encadrant enc = e.getStagiaire().getStage() != null ? e.getStagiaire().getStage().getEncadrant() : null;
            if (enc == null) {
                return null;
            }
            String nom = nomComplet(enc);
            return occurrences.get(nom) > 1 ? nom + " (#" + enc.getId() + ")" : nom;
        }, Evaluation::getNoteGlobale);
    }

    private static String nomComplet(Encadrant e) {
        return ((e.getNom() == null ? "" : e.getNom()) + " " + (e.getPrenom() == null ? "" : e.getPrenom())).trim();
    }
}
