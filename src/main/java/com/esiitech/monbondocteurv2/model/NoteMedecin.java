package com.esiitech.monbondocteurv2.model;

public enum NoteMedecin {
    UNE_ETOILE(1),
    DEUX_ETOILES(2),
    TROIS_ETOILES(3),
    QUATRE_ETOILES(4),
    CINQ_ETOILES(5);

    private final int valeur;

    NoteMedecin(int valeur) {
        this.valeur = valeur;
    }

    public int getValeur() {
        return valeur;
    }
}

