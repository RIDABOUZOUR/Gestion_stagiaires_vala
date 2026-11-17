package com.vala.gestionStagiaires;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GestionStagiairesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionStagiairesApplication.class, args);
    }
}
