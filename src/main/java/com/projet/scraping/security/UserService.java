package com.projet.scraping.security;

/**
 * Service pour la gestion des utilisateurs.
 * Cette classe gère l'authentification, la création de comptes utilisateurs,
 * et la mise à jour du statut des utilisateurs.
 */
import com.projet.scraping.security.dto.UserDTO;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@Transactional
@AllArgsConstructor

/**
 * Charge les détails d'un utilisateur à partir de son nom d'utilisateur.
 * Cette méthode est utilisée par Spring Security lors de l'authentification.
 *
 * @param username le nom d'utilisateur à charger
 * @return un objet UserDetails qui sera utilisé par Spring Security
 * @throws UsernameNotFoundException si l'utilisateur n'est pas trouvé
 */
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));



        return UserDetailsImpl.build(user);
    }

    // Business methods
    /**
     * Crée un nouveau compte utilisateur.
     * Le mot de passe est encodé, le statut est défini à ACTIVE, et un publicId est généré.
     * @param dto les données du nouvel utilisateur
     * @return l'utilisateur créé
     */
    public User createAccount(UserDTO dto) {
        User user = new User();
        user.setNom(dto.getNom());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getMotDePasse()));
        user.setTypeCompte(User.AccountType.valueOf(dto.getTypeCompte()));
        user.setStatut(User.UserStatus.ACTIVE);
        user.setEnable(true);
        user.setPublicId(UUID.randomUUID());
        user.setRoles("USER");
        return userRepository.save(user);
    }

    /**
     * Met à jour le statut d'un utilisateur.
     * Le champ enable est également mis à jour en fonction du statut.
     * @param id l'identifiant de l'utilisateur
     * @param status le nouveau statut
     * @return l'utilisateur mis à jour
     * @throws IllegalArgumentException si l'utilisateur n'existe pas
     */
    public User updateUserStatus(Long id, User.UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setStatut(status);
        user.setEnable(status == User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }


}
