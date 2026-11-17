package com.vala.gestionStagiaires.entities;

import com.vala.gestionStagiaires.enums.StatutPresence;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** Présence (ou absence) d'un stagiaire un jour donné. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_presence_stagiaire_date",
        columnNames = {"stagiaire_id", "date_presence"}))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Presence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stagiaire_id")
    private Stagiaire stagiaire;

    @Column(name = "date_presence", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPresence statut;

    @Column(length = 500)
    private String commentaire;
}
