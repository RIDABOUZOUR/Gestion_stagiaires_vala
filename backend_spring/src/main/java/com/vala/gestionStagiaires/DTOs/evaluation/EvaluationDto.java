package com.vala.gestionStagiaires.DTOs.evaluation;

import com.vala.gestionStagiaires.entities.Evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationDto(
        Long id,
        Long stagiaireId,
        BigDecimal noteGlobale,
        Integer competenceTechnique,
        Integer communication,
        Integer autonomie,
        Integer assiduite,
        String appreciation,
        boolean recommande,
        LocalDate dateEvaluation) {

    public static EvaluationDto of(Evaluation e) {
        return new EvaluationDto(e.getId(), e.getStagiaire().getId(), e.getNoteGlobale(), e.getCompetenceTechnique(),
                e.getCommunication(), e.getAutonomie(), e.getAssiduite(), e.getAppreciation(), e.isRecommande(),
                e.getDateEvaluation());
    }
}
