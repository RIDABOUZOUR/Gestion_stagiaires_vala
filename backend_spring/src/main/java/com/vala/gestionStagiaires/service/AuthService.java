package com.vala.gestionStagiaires.service;

import com.vala.gestionStagiaires.DTOs.auth.LoginRequest;
import com.vala.gestionStagiaires.DTOs.auth.LoginResponse;

public interface AuthService {

    /** Vérifie les identifiants et émet un jeton ; {@code BadCredentialsException} sinon. */
    LoginResponse connecter(LoginRequest request);
}
