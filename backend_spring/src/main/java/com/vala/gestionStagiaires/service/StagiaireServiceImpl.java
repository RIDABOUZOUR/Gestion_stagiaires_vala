package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.PageResponse;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.DTOs.StagiaireFiltre;
import com.vala.gestionStagiaires.Mappers.StagiaireMapper;
import com.vala.gestionStagiaires.config.ReglesMetierProperties;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.AttestationRepository;
import com.vala.gestionStagiaires.repository.CandidatureRepository;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EvaluationRepository;
import com.vala.gestionStagiaires.repository.PresenceRepository;
import com.vala.gestionStagiaires.repository.RapportHebdomadaireRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.specification.StagiaireSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class StagiaireServiceImpl implements StagiaireService {

    private final StagiaireRepository stagiaireRepository;
    private final StageRepository stageRepository;
    private final DepartementRepository departementRepository;
    private final StagiaireMapper mapper;
    private final ReglesMetierProperties regles;
    private final PresenceRepository presenceRepository;
    private final RapportHebdomadaireRepository rapportRepository;
    private final EvaluationRepository evaluationRepository;
    private final AttestationRepository attestationRepository;
    private final CandidatureRepository candidatureRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StagiaireDto> getAllStagiares() {
        return stagiaireRepository.findAll().stream()
                .map(mapper::fromStagiaire)
                .toList();
    }

    @Override
    public StagiaireDto addStagiare(StagiaireDto stagiaireDto) {
        verifierEmailDisponible(stagiaireDto.getEmail(), null);
        Stagiaire stagiaire = mapper.fromStagiaireDto(stagiaireDto);
        stagiaire.setId(null);
        stagiaire.setStage(findStageAffectable(stagiaireDto.getStage()));
        stagiaire.setDepartement(findDepartement(stagiaireDto.getDepartement()));
        verifierAgeMinimum(stagiaire.getDateNaissance(), stagiaire.getStage());
        return mapper.fromStagiaire(stagiaireRepository.save(stagiaire));
    }

    @Override
    public void deleteStagiaire(Long id) {
        if (!stagiaireRepository.existsById(id)) {
            throw new StagiaireNotFoundException(id);
        }
        // Une attestation délivrée est un document officiel : on garde sa trace.
        if (attestationRepository.existsByStagiaireId(id)) {
            throw new BusinessRuleException(
                    "Impossible de supprimer ce stagiaire : une attestation de stage lui a été délivrée.");
        }
        // Le suivi (présences, rapports, évaluation) n'a pas de sens sans le stagiaire.
        presenceRepository.supprimerParStagiaire(id);
        evaluationRepository.supprimerParStagiaire(id);
        candidatureRepository.detacherStagiaire(id);
        rapportRepository.supprimerParStagiaire(id);
        stagiaireRepository.deleteById(id);
    }

    @Override
    public StagiaireDto updateStagiaire(StagiaireDto updatedStagiare, Long id) {
        Stagiaire existingStagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new StagiaireNotFoundException(id));
        verifierEmailDisponible(updatedStagiare.getEmail(), id);

        existingStagiaire.setDateNaissance(updatedStagiare.getDateNaissance());
        existingStagiaire.setTelephone(updatedStagiare.getTelephone());
        existingStagiaire.setPrenom(updatedStagiare.getPrenom());
        existingStagiaire.setNom(updatedStagiare.getNom());
        existingStagiaire.setSexe(updatedStagiare.getSexe());
        existingStagiaire.setEmail(updatedStagiare.getEmail());
        existingStagiaire.setAdresse(updatedStagiare.getAdresse());
        existingStagiaire.setNiveau(updatedStagiare.getNiveau());
        existingStagiaire.setOrganisme(updatedStagiare.getOrganisme());
        existingStagiaire.setFiliere(updatedStagiare.getFiliere());
        existingStagiaire.setDepartement(findDepartement(updatedStagiare.getDepartement()));

        // On ne contrôle le statut que si le stagiaire change de stage : un stagiaire
        // dont le stage vient de se terminer doit rester modifiable.
        Long ancienStageId = existingStagiaire.getStage() != null ? existingStagiaire.getStage().getId() : null;
        Long nouveauStageId = updatedStagiare.getStage() != null ? updatedStagiare.getStage().getId() : null;
        if (!Objects.equals(ancienStageId, nouveauStageId)) {
            existingStagiaire.setStage(findStageAffectable(updatedStagiare.getStage()));
        }
        verifierAgeMinimum(existingStagiaire.getDateNaissance(), existingStagiaire.getStage());

        return mapper.fromStagiaire(stagiaireRepository.save(existingStagiaire));
    }

    @Override
    @Transactional(readOnly = true)
    public StagiaireDto getStagiaire(Long id) {
        Stagiaire stagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new StagiaireNotFoundException(id));
        return mapper.fromStagiaire(stagiaire);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StagiaireDto> searchStagiaire(String keyword) {
        return stagiaireRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining(keyword, keyword)
                .stream()
                .map(mapper::fromStagiaire)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StagiaireDto> rechercherStagiaires(StagiaireFiltre filtre, Pageable pageable) {
        return PageResponse.of(stagiaireRepository
                .findAll(StagiaireSpecifications.avecFiltre(filtre), pageable)
                .map(mapper::fromStagiaire));
    }

    // ------------------------------------------------------------------
    // Chargement des relations + règles métier
    // ------------------------------------------------------------------

    /** Charge le stage en base et vérifie qu'il peut encore accueillir un stagiaire. */
    private Stage findStageAffectable(StageDto stageDto) {
        if (stageDto == null || stageDto.getId() == null) {
            throw new BusinessRuleException("Le stagiaire doit être affecté à un stage existant.");
        }
        Long stageId = stageDto.getId();
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage", stageId));
        if (stage.getStatus() != null && stage.getStatus().isFinal()) {
            throw new BusinessRuleException(
                    "Impossible d'affecter un stagiaire à un stage " + stage.getStatus() + ".");
        }
        return stage;
    }

    /** Deux stagiaires ne peuvent pas partager le même email (insensible à la casse). */
    private void verifierEmailDisponible(String email, Long idCourant) {
        if (email == null || email.isBlank()) {
            return;
        }
        boolean pris = idCourant == null
                ? stagiaireRepository.existsByEmailIgnoreCase(email)
                : stagiaireRepository.existsByEmailIgnoreCaseAndIdNot(email, idCourant);
        if (pris) {
            throw new BusinessRuleException("Un stagiaire utilise déjà l'email " + email + ".");
        }
    }

    /** Le stagiaire doit avoir l'âge minimum requis à la date de début de son stage. */
    private void verifierAgeMinimum(LocalDate dateNaissance, Stage stage) {
        int ageMinimum = regles.ageMinimum();
        if (ageMinimum <= 0 || dateNaissance == null || stage == null || stage.getDateDebut() == null) {
            return;
        }
        int age = Period.between(dateNaissance, stage.getDateDebut()).getYears();
        if (age < ageMinimum) {
            throw new BusinessRuleException("Le stagiaire doit avoir au moins " + ageMinimum
                    + " ans au début du stage (il aura " + age + " ans).");
        }
    }

    private Departement findDepartement(DepartementDto departementDto) {
        if (departementDto == null || departementDto.getId() == null) {
            throw new BusinessRuleException("Le stagiaire doit être rattaché à un département existant.");
        }
        Long departementId = departementDto.getId();
        return departementRepository.findById(departementId)
                .orElseThrow(() -> new ResourceNotFoundException("Département", departementId));
    }
}
