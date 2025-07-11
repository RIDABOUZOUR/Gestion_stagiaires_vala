//package com.vala.gestionStagiaires;
//
//import com.vala.gestionStagiaires.entities.*;
//
//import com.vala.gestionStagiaires.enums.Sexe;
//import com.vala.gestionStagiaires.enums.StageStaus;
//import com.vala.gestionStagiaires.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//
//import org.springframework.stereotype.Component;
//
//
//import java.util.Date;
//
//@Component
//public class DataInitializer implements CommandLineRunner {
//    @Autowired
//    private StagiaireRepository stagiaireRepository;
//    @Autowired
//    private StageRepository stageRepository;
//    @Autowired
//    private EncadrantRepository encadrantRepository;
//    @Autowired
//    private DepartementRepository departementRepository;
//
//
//    @Override
//
//    public void run(String... args) throws Exception {
//
//        Departement departement = Departement.builder()
//                .nom("informatique")
//                .responsable("Mr xyz")
//                .build();
//        departementRepository.save(departement);
//
//        Encadrant encadrant = Encadrant.builder()
//                .nom("abc")
//                .prenom("defg")
//                .email("abc@gmail.com")
//                .telephone("0612345678")
//                .build();
//        encadrantRepository.save(encadrant);
//
////        Stage stage = Stage.builder()
////                .dateDebut(new Date())
////                .dateFin(new Date())
////                .sujet("gestion Stagiaires")
////                .status(StageStaus.EN_COURS)
////                .encadrant(encadrant)
////                .build();
////        stageRepository.save(stage);
//
//
//        for (int i = 0; i < 3; i++) {
//
//            Stagiaire stagiaire = Stagiaire.builder()
//                    .nom("rida")
//                    .prenom("anas")
//                    .adresse("Agadir")
//                    .dateNaissance(new Date())
//                    .sexe(Sexe.HOMME)
//                    .organisme("ENSA")
//                    .email("rida@gmail.com")
//                    .telephone("0666666666")
//                    .filiere("Developpement")
//                    .niveau("bac+3")
//                    .departement(departement)
//                    .stage(stageRepository.getReferenceById(1L))
//                    .build();
//            stagiaireRepository.save(stagiaire);
//
//
//        }
//
//    }
//}
