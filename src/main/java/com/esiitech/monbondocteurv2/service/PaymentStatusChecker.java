package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.enums.PaymentStatus;
import com.esiitech.monbondocteurv2.integration.SolutechPaymentClient;
import com.esiitech.monbondocteurv2.model.PaymentTransaction;
import com.esiitech.monbondocteurv2.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentStatusChecker {

    private final PaymentTransactionRepository paymentRepository;
    private final SolutechPaymentClient paymentClient;
    private final ObjectMapper objectMapper;
    private final PaymentSuccessService paymentSuccessService;

    public PaymentStatusChecker(
            PaymentTransactionRepository paymentRepository,
            SolutechPaymentClient paymentClient,
            ObjectMapper objectMapper,
            PaymentSuccessService paymentSuccessService
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentClient = paymentClient;
        this.objectMapper = objectMapper;
        this.paymentSuccessService = paymentSuccessService;
    }

    @Transactional
    public boolean check(String paymentId) {

        PaymentTransaction tx =
                paymentRepository.findById(paymentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment transaction not found"
                                )
                        );

        // =====================================================
        // DÉJÀ SUCCESS
        // Aucun appel PVIT supplémentaire
        // =====================================================

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return true;
        }

        // =====================================================
        // STATUT TERMINAL
        // =====================================================

        if (tx.getStatus() == PaymentStatus.FAILED
                || tx.getStatus() == PaymentStatus.CANCELLED
                || tx.getStatus() == PaymentStatus.EXPIRED) {

            return true;
        }

        // =====================================================
        // INTERROGATION PVIT
        // PVIT attend PEYREF...
        // =====================================================

        String providerResponse =
                paymentClient.paymentStatus(
                        tx.getAppId(),
                        tx.getReference()
                );

        try {

            JsonNode root =
                    objectMapper.readTree(providerResponse);

            JsonNode statusNode =
                    root.get("status");

            if (statusNode == null
                    || statusNode.asText().isBlank()) {

                throw new IllegalStateException(
                        "Le provider n'a retourné aucun statut"
                );
            }

            String status =
                    statusNode.asText()
                            .trim()
                            .toUpperCase();

            tx.setCallbackRawStatus(status);

            // =================================================
            // SUCCESS
            // Toute la logique est centralisée
            // =================================================

            if ("SUCCESS".equals(status)
                    || "PAID".equals(status)
                    || "COMPLETED".equals(status)) {

                paymentSuccessService.traiterPaiementReussi(
                        tx,
                        status
                );

                return true;
            }

            // =================================================
            // FAILED
            // =================================================

            if ("FAILED".equals(status)) {

                tx.setStatus(PaymentStatus.FAILED);
                tx.setProviderMessage(
                        "Paiement échoué"
                );

                paymentRepository.save(tx);

                return true;
            }

            // =================================================
            // CANCELLED
            // =================================================

            if ("CANCELLED".equals(status)) {

                tx.setStatus(PaymentStatus.CANCELLED);
                tx.setProviderMessage(
                        "Paiement annulé"
                );

                paymentRepository.save(tx);

                return true;
            }

            // =================================================
            // EXPIRED
            // =================================================

            if ("EXPIRED".equals(status)) {

                tx.setStatus(PaymentStatus.EXPIRED);
                tx.setProviderMessage(
                        "Paiement expiré"
                );

                paymentRepository.save(tx);

                return true;
            }

            // =================================================
            // TOUJOURS EN ATTENTE
            // =================================================

            tx.setStatus(PaymentStatus.PENDING);
            tx.setProviderMessage(
                    "Paiement en attente de confirmation"
            );

            paymentRepository.save(tx);

            return false;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erreur lors de la vérification du paiement: "
                            + e.getMessage(),
                    e
            );
        }
    }
}