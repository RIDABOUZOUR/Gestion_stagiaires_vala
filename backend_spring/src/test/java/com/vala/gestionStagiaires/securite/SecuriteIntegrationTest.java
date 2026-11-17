package com.vala.gestionStagiaires.securite;

import com.vala.gestionStagiaires.entities.Departement;
import com.vala.gestionStagiaires.entities.Encadrant;
import com.vala.gestionStagiaires.entities.Stage;
import com.vala.gestionStagiaires.entities.Stagiaire;
import com.vala.gestionStagiaires.entities.Utilisateur;
import com.vala.gestionStagiaires.enums.Role;
import com.vala.gestionStagiaires.enums.Sexe;
import com.vala.gestionStagiaires.enums.StageStatus;
import com.vala.gestionStagiaires.repository.DepartementRepository;
import com.vala.gestionStagiaires.repository.EncadrantRepository;
import com.vala.gestionStagiaires.repository.StageRepository;
import com.vala.gestionStagiaires.repository.StagiaireRepository;
import com.vala.gestionStagiaires.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Matrice des droits de bout en bout : vraie chaîne de filtres, vrais jetons
 * émis par {@link JwtService}, base H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Sécurité (intégration)")
class SecuriteIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UtilisateurRepository utilisateurRepository;
    @Autowired private EncadrantRepository encadrantRepository;
    @Autowired private DepartementRepository departementRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private StagiaireRepository stagiaireRepository;

    private String jetonAdmin;
    private String jetonRh;
    private String jetonEncadrant;
    private Stagiaire stagiaireDuMartin;
    private Stagiaire stagiaireDuBernard;
    private Stage stageDuBernard;

    @BeforeEach
    void setUp() {
        Departement dep = departementRepository.save(Departement.builder().nom("Informatique").responsable("Durand").build());
        Encadrant martin = encadrantRepository.save(Encadrant.builder().nom("Martin").prenom("Paul").email("paul@vala.ma").build());
        Encadrant bernard = encadrantRepository.save(Encadrant.builder().nom("Bernard").prenom("Lucie").email("lucie@vala.ma").build());
        Stage stageDuMartin = stageRepository.save(stage("IA", martin));
        stageDuBernard = stageRepository.save(stage("Web", bernard));
        stagiaireDuMartin = stagiaireRepository.save(stagiaire("sara@mail.com", stageDuMartin, dep));
        stagiaireDuBernard = stagiaireRepository.save(stagiaire("omar@mail.com", stageDuBernard, dep));

        jetonAdmin = jeton(compte("admin@vala.ma", Role.ADMIN, null));
        jetonRh = jeton(compte("rh@vala.ma", Role.RH, null));
        jetonEncadrant = jeton(compte("paul.compte@vala.ma", Role.ENCADRANT, martin));
    }

    private static Stage stage(String sujet, Encadrant encadrant) {
        return Stage.builder().sujet(sujet).status(StageStatus.EN_COURS)
                .dateDebut(LocalDate.now().minusMonths(1)).dateFin(LocalDate.now().plusMonths(2)).encadrant(encadrant).build();
    }

    private static Stagiaire stagiaire(String email, Stage stage, Departement dep) {
        return Stagiaire.builder().nom("Nom").prenom("Prenom").email(email).sexe(Sexe.FEMME).stage(stage).departement(dep).build();
    }

    private Utilisateur compte(String email, Role role, Encadrant encadrant) {
        return utilisateurRepository.save(Utilisateur.builder().email(email)
                .motDePasse(passwordEncoder.encode("secret123")).role(role).encadrant(encadrant).actif(true).build());
    }

    private String jeton(Utilisateur u) {
        return "Bearer " + jwtService.emettre(u).valeur();
    }

    private static MockHttpServletRequestBuilder avec(MockHttpServletRequestBuilder requete, String jeton) {
        return requete.header(HttpHeaders.AUTHORIZATION, jeton);
    }

    @Nested
    @DisplayName("Authentification")
    class Authentification {

        @Test
        @DisplayName("sans jeton : 401 en JSON avec WWW-Authenticate")
        void sansJeton() throws Exception {
            mockMvc.perform(get("/stagiaires"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Authentification requise."));
        }

        @Test
        @DisplayName("jeton falsifié : 401 « jeton invalide »")
        void jetonFalsifie() throws Exception {
            mockMvc.perform(avec(get("/stagiaires"), jetonRh.substring(0, jetonRh.length() - 4) + "abcd"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Session expirée ou jeton invalide : reconnectez-vous."));
        }

        @Test
        @DisplayName("POST /auth/login : jeton valable pour GET /auth/me ; mauvais mot de passe → 401")
        void login() throws Exception {
            String reponse = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"RH@vala.ma\",\"motDePasse\":\"secret123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("RH"))
                    .andReturn().getResponse().getContentAsString();
            String token = reponse.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

            mockMvc.perform(avec(get("/auth/me"), "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("rh@vala.ma"))
                    .andExpect(jsonPath("$.role").value("RH"));

            mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"rh@vala.ma\",\"motDePasse\":\"mauvais\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect."));
        }

        @Test
        @DisplayName("POST /candidatures est public")
        void candidaturePublique() throws Exception {
            mockMvc.perform(post("/candidatures").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"Doukkali\",\"prenom\":\"Lina\",\"email\":\"lina@mail.com\",\"sexe\":\"FEMME\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("pré-requête CORS (OPTIONS) acceptée sans jeton")
        void preRequeteCors() throws Exception {
            mockMvc.perform(options("/stagiaires")
                            .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
        }
    }

    @Nested
    @DisplayName("Rôles")
    class Roles {

        @Test
        @DisplayName("statistiques, exports, candidatures : RH oui, ENCADRANT non")
        void gestionReserveeAuxRh() throws Exception {
            mockMvc.perform(avec(get("/statistiques/resume"), jetonRh)).andExpect(status().isOk());
            mockMvc.perform(avec(get("/stagiaires/export.csv"), jetonRh)).andExpect(status().isOk());
            mockMvc.perform(avec(get("/candidatures/page"), jetonRh)).andExpect(status().isOk());

            mockMvc.perform(avec(get("/statistiques/resume"), jetonEncadrant))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
            mockMvc.perform(avec(get("/stagiaires/export.csv"), jetonEncadrant)).andExpect(status().isForbidden());
            mockMvc.perform(avec(get("/candidatures/page"), jetonEncadrant)).andExpect(status().isForbidden());
            mockMvc.perform(avec(get("/stagiaires/" + stagiaireDuMartin.getId() + "/attestation"), jetonEncadrant))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("comptes utilisateurs : ADMIN uniquement")
        void utilisateursReservesAdmin() throws Exception {
            mockMvc.perform(avec(get("/utilisateurs"), jetonAdmin)).andExpect(status().isOk());
            mockMvc.perform(avec(get("/utilisateurs"), jetonRh)).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("un encadrant ne peut ni créer ni supprimer les données de référence")
        void encadrantEnLectureSeule() throws Exception {
            mockMvc.perform(avec(delete("/stagiaires/" + stagiaireDuMartin.getId()), jetonEncadrant))
                    .andExpect(status().isForbidden());
            mockMvc.perform(avec(post("/departements"), jetonEncadrant).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nom\":\"Finance\",\"responsable\":\"Roux\"}"))
                    .andExpect(status().isForbidden());
            mockMvc.perform(avec(get("/departements"), jetonEncadrant)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("route inconnue : refusée même authentifié")
        void routeInconnue() throws Exception {
            mockMvc.perform(avec(get("/inconnue"), jetonAdmin)).andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Encadrant limité à ses stagiaires")
    class Propriete {

        @Test
        @DisplayName("listes filtrées automatiquement")
        void listesFiltrees() throws Exception {
            mockMvc.perform(avec(get("/stagiaires"), jetonEncadrant))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].email").value("sara@mail.com"));
            mockMvc.perform(avec(get("/stagiaires/page"), jetonEncadrant))
                    .andExpect(jsonPath("$.totalElements").value(1));
            mockMvc.perform(avec(get("/stages/page").param("encadrantId", stageDuBernard.getEncadrant().getId().toString()),
                            jetonEncadrant))
                    .andExpect(jsonPath("$.content[0].sujet").value("IA")) // le filtre demandé est ignoré
                    .andExpect(jsonPath("$.totalElements").value(1));
            mockMvc.perform(avec(get("/stagiaires"), jetonRh)).andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("fiche, stage et suivi d'un autre encadrant : 403 ; les siens : 200")
        void accesParIdentifiant() throws Exception {
            mockMvc.perform(avec(get("/stagiaires/" + stagiaireDuMartin.getId()), jetonEncadrant)).andExpect(status().isOk());
            mockMvc.perform(avec(get("/stagiaires/" + stagiaireDuBernard.getId()), jetonEncadrant)).andExpect(status().isForbidden());
            mockMvc.perform(avec(get("/stages/" + stageDuBernard.getId()), jetonEncadrant)).andExpect(status().isForbidden());
            mockMvc.perform(avec(get("/stagiaires/" + stagiaireDuBernard.getId() + "/presences"), jetonEncadrant))
                    .andExpect(status().isForbidden());
            mockMvc.perform(avec(put("/stagiaires/" + stagiaireDuMartin.getId() + "/presences/" + LocalDate.now()), jetonEncadrant)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"statut\":\"PRESENT\"}"))
                    .andExpect(status().isOk());
            mockMvc.perform(avec(get("/stagiaires/" + stagiaireDuBernard.getId()), jetonRh)).andExpect(status().isOk());
        }

        @Test
        @DisplayName("le changement de statut est attribué à l'utilisateur connecté dans l'historique")
        void auteurDansHistorique() throws Exception {
            mockMvc.perform(avec(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .patch("/stages/" + stageDuBernard.getId() + "/annuler"), jetonRh))
                    .andExpect(status().isOk());

            mockMvc.perform(avec(get("/stages/" + stageDuBernard.getId() + "/historique"), jetonRh))
                    .andExpect(jsonPath("$[0].auteur").value("rh@vala.ma"))
                    .andExpect(jsonPath("$[0].nouveauStatut").value("ANNULE"))
                    .andExpect(jsonPath("$[0].auteur", not("admin@vala.ma")));
        }
    }
}
