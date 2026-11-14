package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.DepartementDto;
import com.vala.gestionStagiaires.DTOs.StageDto;
import com.vala.gestionStagiaires.DTOs.StagiaireDto;
import com.vala.gestionStagiaires.Mappers.DepartementMapper;
import com.vala.gestionStagiaires.Mappers.StageMapper;
import com.vala.gestionStagiaires.Mappers.StagiaireMapper;
import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.exceptions.StagiaireNotFoundException;
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
    private DepartementMapper departementMapper;

    @Mock
    private StageMapper stageMapper;

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
    @DisplayName("addStagiare() mappe, sauvegarde et retourne le DTO")
    void addStagiare_shouldSaveAndReturnDto() {
        StagiaireDto input = dto(null, "Rida", "Bouzour");
        Stagiaire toSave = entity(null, "Rida", "Bouzour");
        Stagiaire saved = entity(1L, "Rida", "Bouzour");
        StagiaireDto output = dto(1L, "Rida", "Bouzour");
        when(mapper.fromStagiaireDto(input)).thenReturn(toSave);
        when(stagiaireRepository.save(toSave)).thenReturn(saved);
        when(mapper.fromStagiaire(saved)).thenReturn(output);

        assertThat(stagiaireService.addStagiare(input)).isEqualTo(output);
        verify(stagiaireRepository).save(toSave);
    }

    @Test
    @DisplayName("deleteStagiaire() délègue au repository")
    void deleteStagiaire_shouldDelegateToRepository() {
        stagiaireService.deleteStagiaire(3L);

        verify(stagiaireRepository).deleteById(3L);
    }

    @Test
    @DisplayName("updateStagiaire() met à jour l'entité existante")
    void updateStagiaire_shouldUpdateExistingEntity() {
        DepartementDto depDto = new DepartementDto();
        depDto.setId(1L);
        StageDto stageDto = new StageDto();
        stageDto.setId(1L);

        StagiaireDto input = new StagiaireDto();
        input.setNom("Rida");
        input.setPrenom("Bouzour");
        input.setSexe(Sexe.HOMME);
        input.setDepartement(depDto);
        input.setStage(stageDto);

        Stagiaire existing = Stagiaire.builder().id(1L).nom("Ancien").prenom("Nom").build();
        Departement dep = Departement.builder().id(1L).build();
        Stage stage = Stage.builder().id(1L).build();
        Stagiaire saved = entity(1L, "Rida", "Bouzour");
        StagiaireDto output = dto(1L, "Rida", "Bouzour");

        when(stagiaireRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departementMapper.fromDepartementDto(depDto)).thenReturn(dep);
        when(stageMapper.fromStageDto(stageDto)).thenReturn(stage);
        when(stagiaireRepository.save(existing)).thenReturn(saved);
        when(mapper.fromStagiaire(saved)).thenReturn(output);

        assertThat(stagiaireService.updateStagiaire(input, 1L)).isEqualTo(output);
        assertThat(existing.getNom()).isEqualTo("Rida");
        assertThat(existing.getPrenom()).isEqualTo("Bouzour");
        assertThat(existing.getSexe()).isEqualTo(Sexe.HOMME);
        assertThat(existing.getDepartement()).isEqualTo(dep);
        assertThat(existing.getStage()).isEqualTo(stage);
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
