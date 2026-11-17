package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.Mappers.StagiaireMapper;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StagiaireServiceImpl")
class StagiaireServiceImplTest {

    @Mock
    private StagiaireRepository stagiaireRepository;

    @Mock
    private StagiaireMapper mapper;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private DepartementRepository departementRepository;

    @InjectMocks
    private StagiaireServiceImpl stagiaireService;

    private StagiaireDto dto(Long id, String nom, String prenom) {
        StagiaireDto d = new StagiaireDto();
        d.setId(id);
        d.setNom(nom);
        d.setPrenom(prenom);
        d.setSexe(Sexe.HOMME);
        return d;
    }

    private Stagiaire entity(Long id, String nom, String prenom) {
        return Stagiaire.builder().id(id).nom(nom).prenom(prenom).sexe(Sexe.HOMME).build();
    }

    private static StageDto stageRef(Long id) {
        StageDto d = new StageDto();
        d.setId(id);
        return d;
    }

    private static DepartementDto departementRef(Long id) {
        DepartementDto d = new DepartementDto();
        d.setId(id);
        return d;
    }

    private final Stage stageEnCours = Stage.builder().id(1L).status(StageStatus.EN_COURS).build();
    private final Departement departement = Departement.builder().id(1L).build();

    @Test
    @DisplayName("getAllStagiares() retourne la liste mappée")
    void getAllStagiares_shouldReturnMappedList() {
        Stagiaire s1 = entity(1L, "Rida", "Bouzour");
        Stagiaire s2 = entity(2L, "Sara", "Alami");
        StagiaireDto d1 = dto(1L, "Rida", "Bouzour");
        StagiaireDto d2 = dto(2L, "Sara", "Alami");
        when(stagiaireRepository.findAll()).thenReturn(List.of(s1, s2));
        when(mapper.fromStagiaire(s1)).thenReturn(d1);
        when(mapper.fromStagiaire(s2)).thenReturn(d2);

        assertThat(stagiaireService.getAllStagiares()).containsExactly(d1, d2);
    }

    @Test
    @DisplayName("addStagiare() charge stage et département en base puis sauvegarde")
    void addStagiare_shouldLoadRelationsAndSave() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        input.setStage(stageRef(1L));
        input.setDepartement(departementRef(1L));
        Stagiaire toSave = entity(null, "Rida", "Bouzour");
        Stagiaire saved = entity(1L, "Rida", "Bouzour");
        StagiaireDto output = dto(1L, "Rida", "Bouzour");
        when(mapper.fromStagiaireDto(input)).thenReturn(toSave);
        when(stageRepository.findById(1L)).thenReturn(Optional.of(stageEnCours));
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(stagiaireRepository.save(toSave)).thenReturn(saved);
        when(mapper.fromStagiaire(saved)).thenReturn(output);

        assertThat(stagiaireService.addStagiare(input)).isEqualTo(output);
        assertThat(toSave.getStage()).isSameAs(stageEnCours);
        assertThat(toSave.getDepartement()).isSameAs(departement);
    }

    @Test
    @DisplayName("addStagiare() refuse un stage terminé ou annulé")
    void addStagiare_shouldRefuseFinishedStage() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        input.setStage(stageRef(2L));
        input.setDepartement(departementRef(1L));
        when(mapper.fromStagiaireDto(input)).thenReturn(entity(null, "Rida", "Bouzour"));
        when(stageRepository.findById(2L))
                .thenReturn(Optional.of(Stage.builder().id(2L).status(StageStatus.ANNULE).build()));

        assertThatThrownBy(() -> stagiaireService.addStagiare(input))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ANNULE");
        verify(stagiaireRepository, never()).save(any());
    }

    @Test
    @DisplayName("addStagiare() lève ResourceNotFoundException si le département n'existe pas")
    void addStagiare_shouldThrowWhenDepartementNotFound() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        input.setStage(stageRef(1L));
        input.setDepartement(departementRef(9L));
        when(mapper.fromStagiaireDto(input)).thenReturn(entity(null, "Rida", "Bouzour"));
        when(stageRepository.findById(1L)).thenReturn(Optional.of(stageEnCours));
        when(departementRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stagiaireService.addStagiare(input))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteStagiaire() supprime un stagiaire existant")
    void deleteStagiaire_shouldDeleteWhenExists() {
        when(stagiaireRepository.existsById(3L)).thenReturn(true);

        stagiaireService.deleteStagiaire(3L);

        verify(stagiaireRepository).deleteById(3L);
    }

    @Test
    @DisplayName("deleteStagiaire() lève StagiaireNotFoundException si absent")
    void deleteStagiaire_shouldThrowWhenNotFound() {
        when(stagiaireRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> stagiaireService.deleteStagiaire(99L))
                .isInstanceOf(StagiaireNotFoundException.class);
        verify(stagiaireRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("updateStagiaire() met à jour l'entité et recharge les relations")
    void updateStagiaire_shouldUpdateExistingEntity() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        input.setDepartement(departementRef(1L));
        input.setStage(stageRef(1L));

        Stagiaire existing = Stagiaire.builder().id(1L).nom("Ancien").prenom("Nom")
                .stage(Stage.builder().id(5L).build()).build();
        Stagiaire saved = entity(1L, "Rida", "Bouzour");
        StagiaireDto output = dto(1L, "Rida", "Bouzour");

        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(stageRepository.findById(1L)).thenReturn(Optional.of(stageEnCours));
        when(stagiaireRepository.save(existing)).thenReturn(saved);
        when(mapper.fromStagiaire(saved)).thenReturn(output);

        assertThat(stagiaireService.updateStagiaire(input, 1L)).isEqualTo(output);
        assertThat(existing.getNom()).isEqualTo("Rida");
        assertThat(existing.getPrenom()).isEqualTo("Bouzour");
        assertThat(existing.getSexe()).isEqualTo(Sexe.HOMME);
        assertThat(existing.getDepartement()).isSameAs(departement);
        assertThat(existing.getStage()).isSameAs(stageEnCours);
    }

    @Test
    @DisplayName("updateStagiaire() garde un stage terminé si le stagiaire n'en change pas")
    void updateStagiaire_shouldKeepFinishedStageWhenUnchanged() {
        Stage termine = Stage.builder().id(2L).status(StageStatus.TERMINE).build();
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        input.setDepartement(departementRef(1L));
        input.setStage(stageRef(2L));
        Stagiaire existing = Stagiaire.builder().id(1L).stage(termine).build();
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departementRepository.findById(1L)).thenReturn(Optional.of(departement));
        when(stagiaireRepository.save(existing)).thenReturn(existing);

        stagiaireService.updateStagiaire(input, 1L);

        assertThat(existing.getStage()).isSameAs(termine);
        verify(stageRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateStagiaire() lève StagiaireNotFoundException si absent")
    void updateStagiaire_shouldThrowWhenNotFound() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        when(stagiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stagiaireService.updateStagiaire(input, 99L))
                .isInstanceOf(StagiaireNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getStagiaire() retourne le DTO")
    void getStagiaire_shouldReturnDto() {
        Stagiaire s = entity(1L, "Rida", "Bouzour");
        StagiaireDto d = dto(1L, "Rida", "Bouzour");
        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(s));
        when(mapper.fromStagiaire(s)).thenReturn(d);

        assertThat(stagiaireService.getStagiaire(1L)).isEqualTo(d);
    }

    @Test
    @DisplayName("getStagiaire() lève StagiaireNotFoundException si absent")
    void getStagiaire_shouldThrowWhenNotFound() {
        when(stagiaireRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stagiaireService.getStagiaire(99L))
                .isInstanceOf(StagiaireNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("searchStagiaire() recherche sur nom et prénom")
    void searchStagiaire_shouldReturnMappedList() {
        Stagiaire s = entity(1L, "Rida", "Bouzour");
        StagiaireDto d = dto(1L, "Rida", "Bouzour");
        when(stagiaireRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining("ri", "ri"))
                .thenReturn(List.of(s));
        when(mapper.fromStagiaire(s)).thenReturn(d);

        assertThat(stagiaireService.searchStagiaire("ri")).containsExactly(d);
    }

    @Test
    @DisplayName("countStagiairesParDepartement() agrège les résultats")
    void countStagiairesParDepartement_shouldReturnMap() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Informatique", 2L});
        rows.add(new Object[]{"RH", 1L});
        when(stagiaireRepository.countStagiairesParDepartement()).thenReturn(rows);

        assertThat(stagiaireService.countStagiairesParDepartement())
                .containsEntry("Informatique", 2L)
                .containsEntry("RH", 1L)
                .hasSize(2);
    }

    @Test
    @DisplayName("getNombreStagiairesParFiliere() agrège les résultats")
    void getNombreStagiairesParFiliere_shouldReturnMap() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Génie Info", 5L});
        when(stagiaireRepository.countStagiairesByFiliereRaw()).thenReturn(rows);

        assertThat(stagiaireService.getNombreStagiairesParFiliere())
                .containsEntry("Génie Info", 5L);
    }

    @Test
    @DisplayName("getStagiairesParOrganisme() agrège les résultats")
    void getStagiairesParOrganisme_shouldReturnMap() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"ENSA", 3L});
        when(stagiaireRepository.countStagiairesByOrganisme()).thenReturn(rows);

        assertThat(stagiaireService.getStagiairesParOrganisme())
                .containsEntry("ENSA", 3L);
    }

    @Test
    @DisplayName("getStagiairesParEncadrant() agrège les résultats")
    void getStagiairesParEncadrant_shouldReturnMap() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Martin Paul", 4L});
        when(stagiaireRepository.countStagiairesParEncadrant()).thenReturn(rows);

        assertThat(stagiaireService.getStagiairesParEncadrant())
                .containsEntry("Martin Paul", 4L);
    }
}
