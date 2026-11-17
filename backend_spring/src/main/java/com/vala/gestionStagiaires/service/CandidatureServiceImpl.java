package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.DTOs.candidature.AcceptationRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureDto;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureFiltre;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureRequest;
import com.vala.gestionStagiaires.DTOs.candidature.CandidatureStatistiques;
import com.vala.gestionStagiaires.DTOs.candidature.DepartementPublic;
import com.vala.gestionStagiaires.DTOs.candidature.FichierCv;
import com.vala.gestionStagiaires.entities.Candidature;
import com.vala.gestionStagiaires.entities.CvCandidature;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.enums.CandidatureStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.CvCandidatureRepository;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.projection.ComptageParLibelle;
import com.vala.gestionStagiaires.repository.projection.ComptageParStatutCandidature;
import com.vala.gestionStagiaires.repository.projection.DelaiDecision;
import com.vala.gestionStagiaires.repository.specification.CandidatureSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CandidatureServiceImpl implements CandidatureService {

    private static final Set<CandidatureStatus> DECIDEES = Set.of(CandidatureStatus.ACCEPTEE, CandidatureStatus.REFUSEE);
    private static final int MOIS_HISTORIQUE = 12;

    private final CandidatureRepository candidatureRepository;
    private final DepartementRepository departementRepository;
    private final StagiaireRepository stagiaireRepository;
    private final StagiaireService stagiaireService;
    private final Clock clock;
    private final CvCandidatureRepository cvRepository;

    @Override
    public CandidatureDto soumettre(CandidatureRequest request, FichierCv cv) {
        if (candidatureRepository.existsByEmailIgnoreCaseAndStatutIn(request.email(), CandidatureStatus.ACTIVES)) {
            throw new BusinessRuleException("Une candidature est déjà en cours de traitement pour l'email "
                    + request.email() + ".");
        }
        Candidature candidature = Candidature.builder()
                .statut(CandidatureStatus.SOUMISE)
                .dateSoumission(Instant.now(clock))
                .build();
        remplir(candidature, request);
        Candidature enregistree = candidatureRepository.save(candidature);
        if (cv != null) {
            enregistrerCv(enregistree, cv);
        }
        return CandidatureDto.of(enregistree);
    }

    @Override
    public CandidatureDto remplacerCv(Long id, FichierCv cv) {
        Candidature candidature = findCandidature(id);
        if (!candidature.getStatut().isActive()) {
            throw new BusinessRuleException("Le CV d'une candidature " + candidature.getStatut() + " ne peut plus être modifié.");
        }
        enregistrerCv(candidature, cv);
        return CandidatureDto.of(candidature);
    }

    @Override
    @Transactional(readOnly = true)
    public FichierCv cv(Long id) {
        findCandidature(id);
        return cvRepository.findByCandidatureId(id)
                .map(cv -> new FichierCv(cv.getNomFichier(), cv.getContenu()))
                .orElseThrow(() -> new ResourceNotFoundException("Aucun CV n'est joint à cette candidature."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartementPublic> departementsPublics() {
        return departementRepository.findAll(Sort.by("nom")).stream()
                .map(d -> new DepartementPublic(d.getId(), d.getNom()))
                .toList();
    }

    /** Crée ou remplace le CV et marque la candidature comme « CV fourni ». */
    private void enregistrerCv(Candidature candidature, FichierCv fichier) {
        CvCandidature cv = cvRepository.findByCandidatureId(candidature.getId())
                .orElseGet(() -> CvCandidature.builder().candidature(candidature).build());
        cv.setNomFichier(fichier.nomFichier());
        cv.setTaille(fichier.taille());
        cv.setContenu(fichier.contenu());
        cv.setDateDepot(Instant.now(clock));
        cvRepository.save(cv);
        candidature.setCvFourni(true);
        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureDto getCandidature(Long id) {
        return CandidatureDto.of(findCandidature(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CandidatureDto> rechercher(CandidatureFiltre filtre, Pageable pageable) {
        return PageResponse.of(candidatureRepository
                .findAll(CandidatureSpecifications.avecFiltre(filtre), pageable)
                .map(CandidatureDto::of));
    }

    @Override
    public CandidatureDto modifier(Long id, CandidatureRequest request) {
        Candidature candidature = findCandidature(id);
        if (!candidature.getStatut().isActive()) {
            throw new BusinessRuleException("Une candidature " + candidature.getStatut() + " ne peut plus être modifiée.");
        }
        if (candidatureRepository.existsByEmailIgnoreCaseAndStatutInAndIdNot(request.email(), CandidatureStatus.ACTIVES, id)) {
            throw new BusinessRuleException("Une autre candidature est déjà en cours de traitement pour l'email "
                    + request.email() + ".");
        }
        remplir(candidature, request);
        return CandidatureDto.of(candidatureRepository.save(candidature));
    }

    @Override
    public void supprimer(Long id) {
        Candidature candidature = findCandidature(id);
        if (candidature.getStatut() == CandidatureStatus.ACCEPTEE) {
            throw new BusinessRuleException("Une candidature acceptée ne peut pas être supprimée : elle est liée au stagiaire créé.");
        }
        cvRepository.supprimerParCandidature(id);
        candidatureRepository.delete(candidature);
    }

    @Override
    public CandidatureDto etudier(Long id) {
        Candidature candidature = findCandidature(id);
        changerStatut(candidature, CandidatureStatus.EN_ETUDE);
        return CandidatureDto.of(candidatureRepository.save(candidature));
    }

    @Override
    public CandidatureDto refuser(Long id, String motif) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleException("Le motif du refus est obligatoire.");
        }
        Candidature candidature = findCandidature(id);
        changerStatut(candidature, CandidatureStatus.REFUSEE);
        candidature.setMotifRefus(motif.trim());
        return CandidatureDto.of(candidatureRepository.save(candidature));
    }

    @Override
    public CandidatureDto retirer(Long id) {
        Candidature candidature = findCandidature(id);
        changerStatut(candidature, CandidatureStatus.RETIREE);
        return CandidatureDto.of(candidatureRepository.save(candidature));
    }

    @Override
    public CandidatureDto accepter(Long id, AcceptationRequest request) {
        Candidature candidature = findCandidature(id);
        verifierTransition(candidature.getStatut(), CandidatureStatus.ACCEPTEE);

        Long departementId = request.departementId() != null ? request.departementId()
                : candidature.getDepartementSouhaite() != null ? candidature.getDepartementSouhaite().getId() : null;
        if (departementId == null) {
            throw new BusinessRuleException("Indiquez le département du futur stagiaire : le candidat n'en a pas souhaité.");
        }

        // Toutes les règles de création d'un stagiaire s'appliquent (email unique, stage
        // ni terminé ni annulé, âge minimum…). En cas d'échec, rien n'est enregistré.
        StagiaireDto stagiaire = stagiaireService.addStagiare(versStagiaire(candidature, request.stageId(), departementId));

        changerStatut(candidature, CandidatureStatus.ACCEPTEE);
        candidature.setStagiaire(stagiaireRepository.getReferenceById(stagiaire.getId()));
        return CandidatureDto.of(candidatureRepository.save(candidature));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidatureStatistiques statistiques() {
        Map<CandidatureStatus, Long> parStatut = new EnumMap<>(CandidatureStatus.class);
        for (CandidatureStatus s : CandidatureStatus.values()) {
            parStatut.put(s, 0L);
        }
        for (ComptageParStatutCandidature c : candidatureRepository.compterParStatut()) {
            parStatut.put(c.statut(), c.nombre());
        }
        long total = parStatut.values().stream().mapToLong(Long::longValue).sum();
        long acceptees = parStatut.get(CandidatureStatus.ACCEPTEE);
        long refusees = parStatut.get(CandidatureStatus.REFUSEE);

        return new CandidatureStatistiques(
                total,
                parStatut,
                parMois(),
                StatistiquesUtils.pourcentage(acceptees, acceptees + refusees),
                delaiMoyenEnJours(candidatureRepository.delaisDecision(DECIDEES)),
                StatistiquesUtils.trierDecroissant(candidatureRepository.compterParOrganisme().stream()
                        .collect(Collectors.toMap(c -> StatistiquesUtils.libelle(c.libelle()),
                                ComptageParLibelle::nombre, Long::sum))));
    }

    // ------------------------------------------------------------------

    private static void verifierTransition(CandidatureStatus actuel, CandidatureStatus cible) {
        if (!actuel.canTransitionTo(cible)) {
            String detail = cible == CandidatureStatus.ACCEPTEE && actuel == CandidatureStatus.SOUMISE
                    ? " La candidature doit d'abord être mise à l'étude." : "";
            throw new BusinessRuleException("Transition de candidature interdite : " + actuel + " → " + cible + "." + detail);
        }
    }

    private void changerStatut(Candidature candidature, CandidatureStatus cible) {
        verifierTransition(candidature.getStatut(), cible);
        candidature.setStatut(cible);
        if (!cible.isActive()) {
            candidature.setDateDecision(Instant.now(clock));
        }
    }

    private void remplir(Candidature c, CandidatureRequest r) {
        c.setNom(r.nom().trim());
        c.setPrenom(r.prenom().trim());
        c.setEmail(r.email().trim());
        c.setTelephone(r.telephone());
        c.setDateNaissance(r.dateNaissance());
        c.setSexe(r.sexe());
        c.setOrganisme(r.organisme());
        c.setNiveau(r.niveau());
        c.setFiliere(r.filiere());
        c.setDepartementSouhaite(r.departementSouhaiteId() == null ? null : findDepartement(r.departementSouhaiteId()));
        c.setDateDebutSouhaitee(r.dateDebutSouhaitee());
        c.setDateFinSouhaitee(r.dateFinSouhaitee());
        c.setMotivation(r.motivation());
    }

    private static StagiaireDto versStagiaire(Candidature c, Long stageId, Long departementId) {
        StagiaireDto dto = new StagiaireDto();
        dto.setNom(c.getNom());
        dto.setPrenom(c.getPrenom());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setDateNaissance(c.getDateNaissance());
        dto.setSexe(c.getSexe());
        dto.setOrganisme(c.getOrganisme());
        dto.setNiveau(c.getNiveau());
        dto.setFiliere(c.getFiliere());
        StageDto stage = new StageDto();
        stage.setId(stageId);
        dto.setStage(stage);
        DepartementDto departement = new DepartementDto();
        departement.setId(departementId);
        dto.setDepartement(departement);
        return dto;
    }

    /** Candidatures reçues sur les 12 derniers mois (mois courant inclus), tous les mois présents. */
    private Map<String, Long> parMois() {
        ZoneId zone = clock.getZone();
        YearMonth courant = YearMonth.now(clock);
        YearMonth premier = courant.minusMonths(MOIS_HISTORIQUE - 1L);
        Map<String, Long> resultat = new LinkedHashMap<>();
        for (YearMonth m = premier; !m.isAfter(courant); m = m.plusMonths(1)) {
            resultat.put(m.toString(), 0L);
        }
        Instant depuis = premier.atDay(1).atStartOfDay(zone).toInstant();
        for (Instant date : candidatureRepository.datesSoumissionDepuis(depuis)) {
            resultat.computeIfPresent(YearMonth.from(date.atZone(zone)).toString(), (k, v) -> v + 1);
        }
        return resultat;
    }

    static Double delaiMoyenEnJours(List<DelaiDecision> delais) {
        if (delais.isEmpty()) {
            return null;
        }
        double moyenneHeures = delais.stream()
                .mapToLong(d -> Duration.between(d.dateSoumission(), d.dateDecision()).toHours())
                .average().orElse(0);
        return Math.round(moyenneHeures / 24.0 * 10.0) / 10.0;
    }

    private Candidature findCandidature(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));
    }

    private Departement findDepartement(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Département", id));
    }
}
