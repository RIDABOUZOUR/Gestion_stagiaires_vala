package com.vala.gestionStagiaires.DTOs.suivi;

import com.vala.gestionStagiaires.entities.StageHistorique;
import com.vala.gestionStagiaires.enums.OrigineChangement;
import com.vala.gestionStagiaires.enums.StageStatus;

import java.time.Instant;

public record StageHistoriqueDto(
        StageStatus ancienStatut,
        StageStatus nouveauStatut,
        Instant dateChangement,
        OrigineChangement origine,
        String auteur) {

    public static StageHistoriqueDto of(StageHistorique h) {
        return new StageHistoriqueDto(h.getAncienStatut(), h.getNouveauStatut(),
                h.getDateChangement(), h.getOrigine(), h.getAuteur());
    }
}
