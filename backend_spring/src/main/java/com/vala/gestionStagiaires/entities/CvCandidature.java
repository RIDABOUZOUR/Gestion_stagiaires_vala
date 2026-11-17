package com.vala.gestionStagiaires.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * CV (PDF) joint à une candidature. Table séparée : le fichier n'est chargé que
 * lorsqu'on le télécharge, jamais avec les listes de candidatures.
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CvCandidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidature_id", unique = true)
    private Candidature candidature;

    /** Nom d'origine, assaini (sans chemin ni caractère spécial). */
    @Column(nullable = false, length = 120)
    private String nomFichier;

    @Column(nullable = false)
    private long taille;

    /** Contenu du PDF ; la longueur fait choisir à Hibernate un MEDIUMBLOB sur MySQL (16 Mo). */
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(nullable = false, length = 6_000_000)
    private byte[] contenu;

    @Column(nullable = false)
    private Instant dateDepot;
}
