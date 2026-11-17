package com.vala.gestionStagiaires.DTOs.suivi;

import com.vala.gestionStagiaires.entities.Presence;
import com.vala.gestionStagiaires.enums.StatutPresence;

import java.time.LocalDate;

public record PresenceDto(Long id, Long stagiaireId, LocalDate date, StatutPresence statut, String commentaire) {

    public static PresenceDto of(Presence p) {
        return new PresenceDto(p.getId(), p.getStagiaire().getId(), p.getDate(), p.getStatut(), p.getCommentaire());
    }
}
