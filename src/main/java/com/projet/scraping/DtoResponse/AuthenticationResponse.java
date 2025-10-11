package com.projet.scraping.DtoResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
    private String tokenType; // e.g. Bearer
    private String message;
    private UUID id;
    private String fullName;
    private String username;
    private List<String> roles;
}
