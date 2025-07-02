package com.esiitech.monbondocteurv2.securite;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private final String nom;
    private final String role;

    public CustomUserDetails(String email, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String nom, String role) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.nom = nom;
        this.role = role;
    }

    public String getNom() {
        return nom;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
