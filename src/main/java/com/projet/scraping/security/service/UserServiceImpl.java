package com.projet.scraping.security.service;



import com.projet.scraping.DtoResponse.AuthenticationResponse;
import com.projet.scraping.Exeption.AccountDisabledException;
import com.projet.scraping.Exeption.AlreadyExistException;
import com.projet.scraping.Exeption.InvalidCredentialsException;
import com.projet.scraping.Exeption.ResourceNotFoundException;
import com.projet.scraping.security.UserDetailsImpl;
import com.projet.scraping.security.dto.*;
import com.projet.scraping.security.jwt.JwtUtils;
import com.projet.scraping.security.mappers.UserMapper;
import com.projet.scraping.security.model.History;
import com.projet.scraping.security.model.User;
import com.projet.scraping.security.repository.HistoryRepository;
import com.projet.scraping.security.repository.RoleRepository;
import com.projet.scraping.security.repository.UserRepository;
import org.springframework.data.domain.Sort;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final HistoryRepository historyRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsService userDetailsService; // TODO: Remove if not needed

    public UserServiceImpl(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository, UserMapper userMapper, HistoryRepository historyRepository, RoleRepository roleRepository, UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.historyRepository = historyRepository;
        this.roleRepository = roleRepository;
        this.userDetailsService = userDetailsService;
    }
    @Override
    public AuthenticationResponse authenticate(LoginDTO loginDTO) {

        // D'abord vérifier explicitement le statut du compte
        User user = userRepository.findByEmail(loginDTO.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("user not found with username: " + loginDTO.getUsername()));

        if (user != null && !user.isEnable()) {
            throw new AccountDisabledException("Votre compte est désactivé. Contactez l'administrateur.");
        }else{
            try {

                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(),
                                loginDTO.getPassword())
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String token = jwtUtils.generateJwtToken(authentication);

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                System.out.println("Compte enabled: " + userDetails.isEnabled());

                List<String> roles = userDetails.getAuthorities()
                        .stream().map(item -> item.getAuthority()).collect(Collectors.toList());

                createHistory(userDetails.getId());

                return new AuthenticationResponse(token, "Bearer", "Login successful", userDetails.getId(), userDetails.getFullName(), userDetails.getUsername(), roles);

            } catch (BadCredentialsException ex) {
                throw new InvalidCredentialsException("Les paramètres de connexion sont incorrectes");
            }
                /*catch (DisabledException ex){
                    throw new AccountDisabledException("Votre compte est désactivé. Contactez l'administrateur.");
                } catch (Exception ex) {
                    throw new AuthenticationServiceException("Erreur d'authentification", ex);
                }*/
        }


    }

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        checkIfUserExists(userDTO);
        User user = userMapper.mapToUser(userDTO);
        /* System.out.println(userDTO.getDirection());
         *//* Optional<HealthCenter> healthCenter = healthCenterRepository.findById(userDTO.getHealthCenterId());
        if (healthCenter.isEmpty()) {
            throw new ResourceNotFoundException("HealthCenter Not Found");
        }*//*

        //user.setCodeDirection(user.getCodeDirection());
        if(user.getCodeDirection() != null){
            user.setCodeDirection(userDTO.getDirection());
            *//*Optional<Direction> d = directionRepository.findByPublicId(UUID.fromString(userDTO.getDirection()));
            System.out.println(d.get().getPublicId());
            if(d.isPresent()) {
                user.setCodeDirection(d.get().getLibelleCourtDirection());
            } else throw new ResourceNotFoundException("Direction is not exists with given id:" + d.get().toString());*//*
        }else{
            user.setCodeDirection(null);
        }

        user.setCodeMinistere(user.getCodeMinistere());*/
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRoles(userDTO.getRoles() != null ? userDTO.getRoles() : "USER");
        user.setEnable(true);
        user.setStatut(com.projet.scraping.security.model.User.UserStatus.ACTIVE);

        // Set account type - default to USER if not specified
        if (userDTO.getTypeCompte() != null && !userDTO.getTypeCompte().isEmpty()) {
            try {
                user.setTypeCompte(com.projet.scraping.security.model.User.AccountType.valueOf(userDTO.getTypeCompte()));
            } catch (IllegalArgumentException e) {
                user.setTypeCompte(com.projet.scraping.security.model.User.AccountType.USER);
            }
        } else {
            user.setTypeCompte(com.projet.scraping.security.model.User.AccountType.USER);
        }

        //Optional<User> user1 = userRepository.findByPublicId(userDTO.getPublicId());
        User savedUser = userRepository.save(user);
        System.out.println("user1: " + savedUser);
        History history = new History();
        history.setName("Enregistrement de l'utilisateur " + userDTO.getFullName());
        history.setUser(savedUser);
        history.setDateHistory(new Date());

        historyRepository.save(history);



        return userMapper.mapToUserDTO(savedUser);
    }

    @Override
    public List<UserRoleReponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(userMapper::mapToUserRoleDTO)
                .toList();
    }

    @Override
    public UserRoleReponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User is not exists with given id : "+id));

        return userMapper.mapToUserRoleDTO(user);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO, UUID id) {
        User user = userRepository.findByPublicId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User is not exists with given id:" + id));

     /*   Optional<HealthCenter> healthCenter = healthCenterRepository.findById(userDTO.getHealthCenterId());
        if (healthCenter.isEmpty()) {
            throw new ResourceNotFoundException("HealthCenter Not Found");
        }*/
       /* if(user.getCodeDirection() != null){
            user.setCodeDirection(userDTO.getDirection());
           *//* Optional<Direction> d = directionRepository.findByPublicId(UUID.fromString(userDTO.getDirection()));

            if(d.isPresent()) {
                user.setCodeDirection(d.get().getLibelleCourtDirection());
            }else throw new ResourceNotFoundException("Direction is not exists with given id:" + id);
*//*
        }else{
            user.setCodeDirection(null);
        }

        //user.setCodeDirection(userDTO.getDirection());
        user.setCodeMinistere(userDTO.getMinistere().intValue());*/
        user.setNom(userDTO.getFullName());
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        //user.setEnable(userDTO.isEnable()); // a commenter
        user.setRoles(userDTO.getRoles());

        Optional<User> userHistory = userRepository.findByPublicId(userDTO.getPublicId());
        History history = new History();
        history.setName("Modification de l'utilisateur " + userDTO.getFullName());
        history.setUser(userHistory.get());
        history.setDateHistory(new Date());

        historyRepository.save(history);

        User updateUser = userRepository.save(user);

        return userMapper.mapToUserDTO(updateUser);
    }

    @Override
    public void deleteUserById(UUID id) {
        Optional<User> optionalUser = userRepository.findByPublicId(id);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("Utilisateur introuvable!");
        }

        User user = optionalUser.get();
        user.setEnable(false);
        userRepository.save(user);
    }

    @Override
    public void enableUserById(UUID id) {
        Optional<User> optionalUser = userRepository.findByPublicId(id);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("Utilisateur introuvable!");
        }

        User user = optionalUser.get();
        user.setEnable(true);
        userRepository.save(user);
    }

    @Override
    public UserDTO updatePassword(UUID id, PasswordDTO passwordDTO) {

        Optional<User> optionalUser = userRepository.findByPublicId(id);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("Utilisateur introuvable");
        }

        User user = optionalUser.get();

        System.out.println(passwordDTO.getCurrentPassword());
        System.out.println(user.getPassword());

        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Le mot de passe actuel ne correspond pas !");
        }


        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        passwordDTO.setUserId(id);
        /*user.setCodeDirection(user.getCodeDirection());*/
        User updatePassword = userRepository.save(user);

        Optional<User> userHistory = userRepository.findByPublicId(passwordDTO.getUserId());
        History history = new History();
        history.setName("Modification du mot de passe de l'utilisateur " + passwordDTO.getUserId());
        history.setUser(userHistory.get());
        history.setDateHistory(new Date());

        historyRepository.save(history);

        return userMapper.mapToUserDTO(updatePassword);
    }

    @Override
    public List<HistoryReponse> getAllHistory() {
        return historyRepository.findAllByOrderByDateHistoryDesc()
                .stream().map(userMapper::mapToHistoryReponse)
                .toList();
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(userMapper::mapToRoleDTO)
                .toList();
    }


    private void checkIfUserExists(UserDTO userDTO){
        if(userRepository.existsByEmail(userDTO.getUsername())) {
            throw new AlreadyExistException(String.format("Ce nom existe déjà !!!", userDTO.getUsername()));
        }
    }

    private History createHistory(UUID userId) {
        User user = userRepository.findByPublicId(userId).get();
        History history = new History();
        history.setName("Connexion de l'utilisateur " + user.getUsername());
        history.setUser(user);
        history.setDateHistory(new Date());

        return historyRepository.save(history);
    }

    public Optional<User> findByUsername(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String email) {
        return userRepository.existsByEmail(email);
    }
}
