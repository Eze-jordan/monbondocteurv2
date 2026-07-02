package com.esiitech.monbondocteurv2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestimonialDto {
    private Long id;
    private String nom;        // "Marie D."
    private String prenom;     // "Marie"
    private String avatar;     // photo de profil (optionnel)
    private String role;       // "Patient"
    private Integer rating;    // 1-5 étoiles
    private String content;    // commentaire
    private String date;       // "15/03/2024"
    private String consultationType; // "Consultation confirmée"
}