package com.projet.scraping.security;

import com.projet.scraping.security.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

// Converti User en Objet conforme à UserDetails attendue par spring security
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;
    private UUID id;
    private String fullName;
    private String username;
/*    private Long ministere;
    private String direction;*/
    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(UUID id, String fullName, String username, /*Long ministere, String direction,*/ String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
  /*      this.ministere = ministere ;
        this.direction = direction ;*/
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Parse multiple roles separated by comma
        String roles = user.getRoles();
        if (roles != null && !roles.isEmpty()) {
            String[] roleArray = roles.split(",");
            for (String role : roleArray) {
                String trimmedRole = role.trim();
                // Add ROLE_ prefix if not already present
                if (!trimmedRole.startsWith("ROLE_")) {
                    trimmedRole = "ROLE_" + trimmedRole;
                }
                authorities.add(new SimpleGrantedAuthority(trimmedRole));
            }
        }

/*
        Long ministere = (long) ((user.getCodeMinistere() != 0) ? user.getCodeMinistere() : 0);
*/

        return new UserDetailsImpl(user.getPublicId(), user.getNom(), user.getUsername()/*,ministere , user.getCodeDirection()*/, user.getPassword(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

  /*  public Long getMinistere() {
        return ministere;
    }

    public void setMinistere(Long ministere) {
        this.ministere = ministere;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }*/

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
