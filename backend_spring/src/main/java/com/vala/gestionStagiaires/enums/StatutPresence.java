package com.vala.gestionStagiaires.enums;

public enum StatutPresence {
    PRESENT,
    RETARD,
    ABSENT,
    ABSENCE_JUSTIFIEE;

    /** Le stagiaire était là ce jour-là (à l'heure ou en retard). */
    public boolean estPresent() {
        return this == PRESENT || this == RETARD;
    }
}
