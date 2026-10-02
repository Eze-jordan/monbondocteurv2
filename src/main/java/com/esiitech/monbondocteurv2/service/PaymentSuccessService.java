package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ActivationFormuleStructureRequest;
import com.esiitech.monbondocteurv2.enums.PaymentStatus;
import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.model.PaymentTransaction;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentSuccessService {

    private final PaymentTransactionRepository paymentRepository;
    private final AbonnementStructureService abonnementStructureService;
    private final NotificationService notificationService;

    public PaymentSuccessService(
            PaymentTransactionRepository paymentRepository,
            AbonnementStructureService abonnementStructureService,
            NotificationService notificationService
    ) {
        this.paymentRepository = paymentRepository;
        this.abonnementStructureService = abonnementStructureService;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentTransaction traiterPaiementReussi(
            PaymentTransaction tx,
            String providerStatus
    ) {

        // =====================================================
        // IDEMPOTENCE
        // Le paiement a déjà été traité.
        // On ne prolonge pas et on ne renvoie pas le mail.
        // =====================================================

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return tx;
        }

        // =====================================================
        // PASSAGE DU PAIEMENT EN SUCCESS
        // =====================================================

        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setCallbackRawStatus(providerStatus);
        tx.setProviderMessage(
                "Paiement confirmé par le provider"
        );

        paymentRepository.save(tx);

        // =====================================================
        // ACTIVATION / PROLONGATION ABONNEMENT
        // =====================================================

        ActivationFormuleStructureRequest activationRequest =
                new ActivationFormuleStructureRequest();

        activationRequest.setStructureId(
                tx.getStructureId()
        );

        activationRequest.setFormuleId(
                tx.getFormuleId()
        );

        StructureSanitaire structure =
                abonnementStructureService.activerOuProlonger(
                        activationRequest
                );

        // =====================================================
        // FORMULE ACTIVÉE
        // =====================================================

        Formules formule =
                structure.getFormuleAbonnement();

        // =====================================================
        // MAIL DE CONFIRMATION
        // =====================================================

        if (structure.getEmail() != null
                && !structure.getEmail().isBlank()) {

            notificationService.envoyerConfirmationPaiementStructure(
                    structure.getEmail(),
                    structure.getNomStructureSanitaire(),
                    formule.getNomFormule(),
                    tx.getAmount().toPlainString(),
                    tx.getReference(),
                    structure.getDateDebutAbonnement().toString(),
                    structure.getDateFinAbonnement().toString()
            );
        }

        return tx;
    }
}