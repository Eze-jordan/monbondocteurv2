package com.esiitech.monbondocteurv2.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String token; // UUID aléatoire

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id", nullable = false)
    private StructureSanitaire structureSanitaire;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    // Getters/Setters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public StructureSanitaire getStructureSanitaire() { return structureSanitaire; }
    public void setStructureSanitaire(StructureSanitaire s) { this.structureSanitaire = s; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }


    public void setId(Long id) {
        this.id = id;
    }
}
