package com.projet.scraping.security;

import com.projet.scraping.security.jwt.AuthEntryPointJwt;
import com.projet.scraping.security.jwt.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Configuration class for Spring Security.
 *
 * @author davibil
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Utiliser @EnableMethodSecurity et active la securite des methods @PreAuthorize, @PostAuthorize
public class SecurityConfig {

    // implementation de UserDetails Service , charge les infos du user de la base de donnée et retourne UserDetails
    private final UserService userService;
    // point d’entrée d’authentification personnalisé déclenché quand un utilisateur non authentifié tente d’accéder à une ressource sécurisée
    // appele lorsque spring security detecte qu'un user n'est pas authentifié
    private final AuthEntryPointJwt unauthorizedHandler;
    //  un filtre personnalisé qui intercepte chaque requête HTTP entrante pour vérifier si elle contient un token JWT valide
    // extrait le token , le valide
    private final AuthTokenFilter authenticationFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Configure la chaîne de filtres de sécurité HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactive CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // Gsstion des exceptions liées à l'authentification
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        unauthorizedHandler.commence(request, response, authException);
                    });
                })
                // Utilise une gestion sans session (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Autorisations pour les requêtes HTTP
                .authorizeHttpRequests(auth -> auth
                        // Ces chemins sont accessibles à tous
                        .requestMatchers("/","/api/v1/login", "/api/v1/register", "/error", "/csrf", "/resources/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/import/**", "/api/v1/etats/**","/api/entresortie/pointage/**", "/datasource/**")
                        .permitAll()
                        // Routes réservées aux admins
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // User ou Admin
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // Authentification necessaire pour toutes les autres routes
                        .anyRequest().authenticated()
                )
                // Config des en-têtes de sécurité HTTP
                .headers(headers -> headers
                        // Permet l'affichage des frames depuis la même origine (nécessaire pour H2 Console)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin
                        )
                        // Paramètres HSTS (HTTP Strict Transport Security)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000) // Duree 1ans
                                .includeSubDomains(true)
                        )
                        // Protection XSS
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        // Politique Content Security-Policy
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; font-src 'self'")
                        )
                );
        // Ajoute notre fournisseur d'authentification personnalisé
        http.authenticationProvider(authenticationProvider());
        // Filtre personnalisé JWT ajouté avant le filtre standard d'authentification
        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure le fournisseur d'authentification basé sur un DAO (UserDetailsService)
     * Requête HTTP → AuthTokenFilter (vérifie le JWT)
     *                    ↓
     *               Pas de token ? → AuthEntryPointJwt (renvoie 401)
     *                    ↓
     *                Token valide ? → UserService charge l'utilisateur
     *                    ↓
     *            DaoAuthenticationProvider vérifie le mot de passe
     *              Accès autorisé ou refusé selon les rôles
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder); // Use the injected PasswordConfig bean
        return authProvider;
    }

    /**
     * Fournit un AuthenticationManager pour les processus d'authentification globaux
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}