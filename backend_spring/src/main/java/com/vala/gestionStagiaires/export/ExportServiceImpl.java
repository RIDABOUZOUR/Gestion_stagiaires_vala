package com.vala.gestionStagiaires.export;

import com.vala.gestionStagiaires.DTOs.StageFiltre;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.specification.CandidatureSpecifications;
import com.vala.gestionStagiaires.repository.specification.StageSpecifications;
import com.vala.gestionStagiaires.repository.specification.StagiaireSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final StagiaireRepository stagiaireRepository;
    private final StageRepository stageRepository;
    private final CandidatureRepository candidatureRepository;
    private final Clock clock;

    @Override
    public ExportCsv exporterStagiaires(StagiaireFiltre filtre, Sort tri) {
        List<Stagiaire> stagiaires = charger(stagiaireRepository, StagiaireSpecifications.avecFiltre(filtre), tri);
        CsvWriter csv = new CsvWriter(List.of("Nom", "Prénom", "Email", "Téléphone", "Sexe", "Date de naissance",
                "Organisme", "Filière", "Niveau", "Département", "Stage", "Statut du stage", "Début du stage",
                "Fin du stage", "Encadrant"), clock.getZone());
        for (Stagiaire s : stagiaires) {
            Stage stage = s.getStage();
            csv.ligne(s.getNom(), s.getPrenom(), s.getEmail(), s.getTelephone(), s.getSexe(), s.getDateNaissance(),
                    s.getOrganisme(), s.getFiliere(), s.getNiveau(),
                    s.getDepartement() != null ? s.getDepartement().getNom() : null,
                    stage != null ? stage.getSujet() : null,
                    stage != null ? stage.getStatus() : null,
                    stage != null ? stage.getDateDebut() : null,
                    stage != null ? stage.getDateFin() : null,
                    stage != null ? nomComplet(stage.getEncadrant()) : null);
        }
        return fichier("stagiaires", csv);
    }

    @Override
    public ExportCsv exporterStages(StageFiltre filtre, Sort tri) {
        List<Stage> stages = charger(stageRepository, StageSpecifications.avecFiltre(filtre), tri);
        CsvWriter csv = new CsvWriter(List.of("Sujet", "Statut", "Début", "Fin", "Durée (jours)", "Encadrant",
                "Email de l'encadrant"), clock.getZone());
        for (Stage s : stages) {
            Long duree = s.getDateDebut() != null && s.getDateFin() != null
                    ? ChronoUnit.DAYS.between(s.getDateDebut(), s.getDateFin()) + 1 : null;
            csv.ligne(s.getSujet(), s.getStatus(), s.getDateDebut(), s.getDateFin(), duree,
                    nomComplet(s.getEncadrant()), s.getEncadrant() != null ? s.getEncadrant().getEmail() : null);
        }
        return fichier("stages", csv);
    }

    @Override
    public ExportCsv exporterCandidatures(CandidatureFiltre filtre, Sort tri) {
        List<Candidature> candidatures = charger(candidatureRepository, CandidatureSpecifications.avecFiltre(filtre), tri);
        CsvWriter csv = new CsvWriter(List.of("Nom", "Prénom", "Email", "Téléphone", "Organisme", "Filière", "Niveau",
                "Département souhaité", "Début souhaité", "Fin souhaitée", "Statut", "Soumise le", "Décision le",
                "Motif du refus", "CV fourni"), clock.getZone());
        for (Candidature c : candidatures) {
            csv.ligne(c.getNom(), c.getPrenom(), c.getEmail(), c.getTelephone(), c.getOrganisme(), c.getFiliere(),
                    c.getNiveau(), c.getDepartementSouhaite() != null ? c.getDepartementSouhaite().getNom() : null,
                    c.getDateDebutSouhaitee(), c.getDateFinSouhaitee(), c.getStatut(), c.getDateSoumission(),
                    c.getDateDecision(), c.getMotifRefus(), c.isCvFourni() ? "Oui" : "Non");
        }
        return fichier("candidatures", csv);
    }

    // ------------------------------------------------------------------

    /** Charge toutes les lignes filtrées (relations comprises, via les @EntityGraph des repositories). */
    private <T> List<T> charger(JpaSpecificationExecutor<T> repository, Specification<T> spec, Sort tri) {
        long total = repository.count(spec);
        if (total > MAX_LIGNES) {
            throw new BusinessRuleException("L'export contiendrait " + total + " lignes (maximum " + MAX_LIGNES
                    + ") : affinez les filtres.");
        }
        if (total == 0) {
            return List.of();
        }
        return repository.findAll(spec, PageRequest.of(0, (int) total, tri)).getContent();
    }

    private ExportCsv fichier(String prefixe, CsvWriter csv) {
        return new ExportCsv(prefixe + "-" + LocalDate.now(clock) + ".csv", csv.toBytes(), csv.nombreLignes());
    }

    private static String nomComplet(Encadrant e) {
        if (e == null) {
            return null;
        }
        return ((e.getNom() == null ? "" : e.getNom()) + " " + (e.getPrenom() == null ? "" : e.getPrenom())).trim();
    }
}
