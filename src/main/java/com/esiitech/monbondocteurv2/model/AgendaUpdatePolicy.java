package com.esiitech.monbondocteurv2.model;

public enum AgendaUpdatePolicy {
    SHIFT_TO_NEXT_FREE_WEEK,   // décale jusqu'à semaine sans RDV
    CANCEL_RDV_AND_APPLY,      // annule les RDV de la semaine cible puis applique
    REFUSE_IF_CONFLICT         // refuse si RDV existants
}

