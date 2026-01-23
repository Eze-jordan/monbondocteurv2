package com.esiitech.monbondocteurv2.model;

public enum StatutRendezVous {
    EN_ATTENTE,   // créé par patient pour une structure, pas encore assigné
    CONFIRME,     // assigné à un médecin + créneau validé
    ANNULE
}
