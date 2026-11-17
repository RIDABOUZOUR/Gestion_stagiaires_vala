package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.EncadrantDto;
import com.vala.gestionStagiaires.Mappers.EncadrantMapper;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.exceptions.BusinessRuleException;
import com.vala.gestionStagiaires.exceptions.ResourceNotFoundException;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EncadrantServiceImpl")
class EncadrantServiceImplTest {

    @Mock
    private EncadrantRepository encadrantRepository;

    @Mock
    private StageRepository stageRepository;

    @Mock
    private com.vala.gestionStagiaires.repository.UtilisateurRepository utilisateurRepository;

    @Mock
    private EncadrantMapper encadrantMapper;

    @InjectMocks
    private EncadrantServiceImpl encadrantService;

    private EncadrantDto dto(Long id, String nom, String prenom, String email, String telephone) {
        EncadrantDto d = new EncadrantDto();
        d.setId(id);
        d.setNom(nom);
        d.setPrenom(prenom);
        d.setEmail(email);
        d.setTelephone(telephone);
        return d;
    }

    private Encadrant entity(Long id, String nom, String prenom, String email, String telephone) {
        return Encadrant.builder().id(id).nom(nom).prenom(prenom).email(email).telephone(telephone).build();
    }

    @Test
    @DisplayName("getEncadrant() retourne la liste mappée")
    void getEncadrant_shouldReturnMappedList() {
        Encadrant e1 = entity(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        EncadrantDto dto1 = dto(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        when(encadrantRepository.findAll()).thenReturn(List.of(e1));
        when(encadrantMapper.fromEncadrant(e1)).thenReturn(dto1);

        assertThat(encadrantService.getEncadrant()).containsExactly(dto1);
    }

    @Test
    @DisplayName("addEncadrant() mappe, sauvegarde et retourne le DTO")
    void addEncadrant_shouldSaveAndReturnDto() {
        EncadrantDto input = dto(null, "Martin", "Paul", "paul@test.com", "0612345678");
        Encadrant toSave = entity(null, "Martin", "Paul", "paul@test.com", "0612345678");
        Encadrant saved = entity(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        EncadrantDto output = dto(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        when(encadrantMapper.fromEncadrantDto(input)).thenReturn(toSave);
        when(encadrantRepository.save(toSave)).thenReturn(saved);
        when(encadrantMapper.fromEncadrant(saved)).thenReturn(output);

        assertThat(encadrantService.addEncadrant(input)).isEqualTo(output);
        verify(encadrantRepository).save(toSave);
    }

    @Test
    @DisplayName("searchEncadrant() recherche sur nom et prénom")
    void searchEncadrant_shouldReturnMappedList() {
        Encadrant e1 = entity(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        EncadrantDto dto1 = dto(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        when(encadrantRepository.findByNomIgnoreCaseContainingOrPrenomIgnoreCaseContaining("mar", "mar"))
                .thenReturn(List.of(e1));
        when(encadrantMapper.fromEncadrant(e1)).thenReturn(dto1);

        assertThat(encadrantService.searchEncadrant("mar")).containsExactly(dto1);
    }

    @Test
    @DisplayName("deleteEncadrant() supprime si rien n'y est rattaché")
    void deleteEncadrant_shouldDeleteWhenUnused() {
        when(encadrantRepository.existsById(5L)).thenReturn(true);
        when(stageRepository.existsByEncadrantId(5L)).thenReturn(false);

        encadrantService.deleteEncadrant(5L);

        verify(encadrantRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteEncadrant() lève ResourceNotFoundException si absent")
    void deleteEncadrant_shouldThrowWhenNotFound() {
        when(encadrantRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> encadrantService.deleteEncadrant(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(encadrantRepository, never()).deleteById(99L);
    }

    @Test
    @DisplayName("deleteEncadrant() refuse la suppression si des éléments y sont rattachés")
    void deleteEncadrant_shouldRefuseWhenStillReferenced() {
        when(encadrantRepository.existsById(5L)).thenReturn(true);
        when(stageRepository.existsByEncadrantId(5L)).thenReturn(true);

        assertThatThrownBy(() -> encadrantService.deleteEncadrant(5L))
                .isInstanceOf(BusinessRuleException.class);
        verify(encadrantRepository, never()).deleteById(5L);
    }

    @Test
    @DisplayName("updateEncadrant() met à jour l'entité existante")
    void updateEncadrant_shouldUpdateExistingEntity() {
        EncadrantDto input = dto(null, "Martin", "Paul", "new@test.com", "0699999999");
        Encadrant existing = entity(1L, "Martin", "Paul", "old@test.com", "0611111111");
        Encadrant saved = entity(1L, "Martin", "Paul", "new@test.com", "0699999999");
        EncadrantDto output = dto(1L, "Martin", "Paul", "new@test.com", "0699999999");
        when(encadrantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(encadrantRepository.save(existing)).thenReturn(saved);
        when(encadrantMapper.fromEncadrant(saved)).thenReturn(output);

        assertThat(encadrantService.updateEncadrant(input, 1L)).isEqualTo(output);
        assertThat(existing.getEmail()).isEqualTo("new@test.com");
        assertThat(existing.getTelephone()).isEqualTo("0699999999");
    }

    @Test
    @DisplayName("updateEncadrant() lève ResourceNotFoundException si absent")
    void updateEncadrant_shouldThrowWhenNotFound() {
        EncadrantDto input = dto(null, "Martin", "Paul", "new@test.com", "0699999999");
        when(encadrantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> encadrantService.updateEncadrant(input, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getEncadrantById() retourne le DTO")
    void getEncadrantById_shouldReturnDto() {
        Encadrant e = entity(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        EncadrantDto dto = dto(1L, "Martin", "Paul", "paul@test.com", "0612345678");
        when(encadrantRepository.findById(1L)).thenReturn(Optional.of(e));
        when(encadrantMapper.fromEncadrant(e)).thenReturn(dto);

        assertThat(encadrantService.getEncadrantById(1L)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getEncadrantById() lève ResourceNotFoundException si absent")
    void getEncadrantById_shouldThrowWhenNotFound() {
        when(encadrantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> encadrantService.getEncadrantById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("addEncadrant() refuse un email déjà utilisé")
    void addEncadrant_shouldRefuseDuplicateEmail() {
        EncadrantDto input = dto(null, "Martin", "Paul", "paul@test.com", "0612345678");
        when(encadrantRepository.existsByEmailIgnoreCase("paul@test.com")).thenReturn(true);

        assertThatThrownBy(() -> encadrantService.addEncadrant(input))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("paul@test.com");
        verify(encadrantRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("updateEncadrant() refuse l'email d'un autre encadrant")
    void updateEncadrant_shouldRefuseEmailOfAnotherEncadrant() {
        EncadrantDto input = dto(null, "Martin", "Paul", "pris@test.com", "0612345678");
        when(encadrantRepository.findById(1L))
                .thenReturn(Optional.of(entity(1L, "Martin", "Paul", "old@test.com", "0611111111")));
        when(encadrantRepository.existsByEmailIgnoreCaseAndIdNot("pris@test.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> encadrantService.updateEncadrant(input, 1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("deleteEncadrant() refuse la suppression d'un encadrant qui a un compte utilisateur")
    void deleteEncadrant_shouldRefuseWhenUserAccountLinked() {
        when(encadrantRepository.existsById(5L)).thenReturn(true);
        when(stageRepository.existsByEncadrantId(5L)).thenReturn(false);
        when(utilisateurRepository.existsByEncadrantId(5L)).thenReturn(true);

        assertThatThrownBy(() -> encadrantService.deleteEncadrant(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("compte utilisateur");
        verify(encadrantRepository, never()).deleteById(5L);
    }
}
