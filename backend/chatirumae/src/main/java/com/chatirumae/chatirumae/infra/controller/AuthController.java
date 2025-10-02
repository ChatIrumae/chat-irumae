package com.chatirumae.chatirumae.infra.controller;

import com.chatirumae.chatirumae.core.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO) {
        String portalId = loginDTO.getPortalId();
        String portalPassword = loginDTO.getPortalPassword();

        String token = authService.login(portalId, portalPassword);
        // TODO: 이거보다 나은 솔루션 없나
        return "\"" + token + "\"";
    }
}

class LoginDTO {
    private String portalId;
    private String portalPassword;

    public String getPortalId() {
        return portalId;
    }

    public String getPortalPassword() {
        return portalPassword;
    }

    public LoginDTO(String portalId, String portalPassword) {
        this.portalId = portalId;

        if (portalPassword == null) {
            throw new IllegalArgumentException("Portal password is null.");
        }

        this.portalPassword = portalPassword;

        if (portalId == null) {
            throw new IllegalArgumentException("Portal ID is null.");
        }
    }
}