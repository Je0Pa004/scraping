package com.projet.scraping.security.api;

import com.projet.scraping.DtoResponse.AuthenticationResponse;
import com.projet.scraping.security.dto.HistoryReponse;
import com.projet.scraping.security.dto.LoginDTO;
import com.projet.scraping.security.dto.PasswordDTO;
import com.projet.scraping.security.dto.RoleDTO;
import com.projet.scraping.security.dto.UserDTO;
import com.projet.scraping.security.dto.UserRoleReponse;
import com.projet.scraping.security.repository.UserRepository;
import com.projet.scraping.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Gestion des utilisateurs", description = "Point d'entrée des utilisateurs")
@RestController
@RequestMapping("/api/v1")
public class UserApi {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserApi(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(
            description = "Inscription publique d'un utilisateur sécurité",
            summary = "Register",
            responses = {
                    @ApiResponse(
                            description = "Created",
                            responseCode = "201"
                    ),
                    @ApiResponse(
                            description = "Conflit - Utilisateur existe déjà",
                            responseCode = "409"
                    )
            }
    )
    public ResponseEntity<UserDTO> register(@RequestBody @Valid UserDTO userDTO) {
        try {
            var created = userService.saveUser(userDTO);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("existe déjà")) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    @Operation(
            description = "Authentification d'un utilisateur",
            summary = "Login",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Unauthorized",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid LoginDTO loginDTO) {
        try {
            var authResponse = (AuthenticationResponse) userService.authenticate(loginDTO);
            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Save new user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<UserDTO> saveUsers(@RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(userService.saveUser(userDTO), HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Retrieve all user",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<List<UserRoleReponse>> getAllUser() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPENSATEUR')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Retrieve a user by Id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<UserRoleReponse> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Update a user by Id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<UserDTO> updateUsers(@RequestBody UserDTO userDTO,@PathVariable("id")  UUID id){
        return ResponseEntity.ok(userService.updateUser(userDTO,id));
    }

    @PutMapping("/users/change_password/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPENSATEUR','MENTOR','USER')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Change password a user by Id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<UserDTO> updatePassword(@PathVariable("id") UUID id, @RequestBody PasswordDTO passwordDTO){
        return ResponseEntity.ok(userService.updatePassword(id,passwordDTO));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "History  all",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<List<HistoryReponse>> getAllHistory() {
        return ResponseEntity.ok(userService.getAllHistory());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "Delete a user by Id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<Void>  deleteUserById(@PathVariable("id") UUID id) {
        this.userService.deleteUserById(id);
        return ResponseEntity.status(204).build();
    }
    @GetMapping("/user-enable-true/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(
            description = "Ce point de terminaison ne nécessite pas de JWT valide",
            summary = "enable true  a user by Id",
            responses = {
                    @ApiResponse(
                            description = "Success",
                            responseCode = "200"
                    ),
                    @ApiResponse(
                            description = "Jeton non autorisé / invalide",
                            responseCode = "401"
                    )
            }
    )
    public ResponseEntity<Void>  enableUserById(@PathVariable("id") UUID id) {
        this.userService.enableUserById(id);
        return ResponseEntity.status(204).build();
    }




}
