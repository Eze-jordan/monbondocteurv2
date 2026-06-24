package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.ActivationFormuleStructureRequest;
import com.esiitech.monbondocteurv2.dto.KycResponse;
import com.esiitech.monbondocteurv2.dto.PaymentCallbackRequest;
import com.esiitech.monbondocteurv2.dto.PaymentFormuleInitRequest;
import com.esiitech.monbondocteurv2.dto.PaymentFormuleResponse;
import com.esiitech.monbondocteurv2.dto.PaymentProviderInitRequest;
import com.esiitech.monbondocteurv2.enums.PaymentStatus;
import com.esiitech.monbondocteurv2.integration.SolutechPaymentClient;
import com.esiitech.monbondocteurv2.model.Formules;
import com.esiitech.monbondocteurv2.model.PaymentTransaction;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.repository.FormulesRepository;
import com.esiitech.monbondocteurv2.repository.PaymentTransactionRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentFormuleService {

    private final StructureSanitaireRepository structureRepository;
    private final FormulesRepository formulesRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final SolutechPaymentClient paymentClient;
    private final AbonnementStructureService abonnementStructureService;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_REFERENCE_PREFIX = "PEYREF";
    private static final int PAYMENT_REFERENCE_DIGITS = 10;
    private static final int MAX_REFERENCE_GENERATION_ATTEMPTS = 20;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public PaymentFormuleService(
            StructureSanitaireRepository structureRepository,
            FormulesRepository formulesRepository,
            PaymentTransactionRepository paymentRepository,
            SolutechPaymentClient paymentClient,
            AbonnementStructureService abonnementStructureService,
            ObjectMapper objectMapper
    ) {
        this.structureRepository = structureRepository;
        this.formulesRepository = formulesRepository;
        this.paymentRepository = paymentRepository;
        this.paymentClient = paymentClient;
        this.abonnementStructureService = abonnementStructureService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentFormuleResponse initiatePayment(PaymentFormuleInitRequest request) {
        validateInitRequest(request);

        StructureSanitaire structure = structureRepository.findById(request.getStructureId())
                .orElseThrow(() -> new IllegalArgumentException("Structure sanitaire introuvable: " + request.getStructureId()));

        Formules formule = formulesRepository.findById(request.getFormuleId())
                .orElseThrow(() -> new IllegalArgumentException("Formule introuvable: " + request.getFormuleId()));

        if (!formule.isActif()) {
            throw new IllegalStateException("Cette formule n'est pas active.");
        }

        if (formule.getMontantTTC() == null || formule.getMontantTTC().signum() <= 0) {
            throw new IllegalStateException("Le montant de la formule doit être supérieur à zéro.");
        }

        boolean alreadyRunning = paymentRepository.existsByStructureIdAndStatusIn(
                request.getStructureId(),
                List.of(
                        PaymentStatus.CREATED,
                        PaymentStatus.KYC_PENDING,
                        PaymentStatus.KYC_OK,
                        PaymentStatus.INITIATED,
                        PaymentStatus.PENDING
                )
        );

        if (alreadyRunning) {
            throw new IllegalStateException("Un paiement est déjà en cours pour cette structure.");
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setPaymentId("pay-" + UUID.randomUUID());
        tx.setStructureId(request.getStructureId());
        tx.setFormuleId(request.getFormuleId());
        tx.setReference(generateUniquePaymentReference());
        tx.setAppId(request.getAppId());
        tx.setOperatorName(request.getOperatorName());
        tx.setCustomerAccountNumber(request.getCustomerAccountNumber());
        tx.setAmount(formule.getMontantTTC());
        tx.setStatus(PaymentStatus.CREATED);

        paymentRepository.save(tx);

        tx.setStatus(PaymentStatus.KYC_PENDING);
        paymentRepository.save(tx);

        KycResponse kyc = paymentClient.kyc(
                request.getAppId(),
                request.getCustomerAccountNumber()
        );

        if (kyc == null) {
            tx.setStatus(PaymentStatus.KYC_FAILED);
            tx.setProviderMessage("KYC response null");
            paymentRepository.save(tx);
            throw new IllegalStateException("KYC response null");
        }

        if (kyc.getErrorMessage() != null && !kyc.getErrorMessage().isBlank()) {
            tx.setStatus(PaymentStatus.KYC_FAILED);
            tx.setProviderMessage(kyc.getErrorMessage());
            paymentRepository.save(tx);
            throw new IllegalStateException("KYC error: " + kyc.getErrorMessage());
        }

        if (!kyc.isIs_active()) {
            tx.setStatus(PaymentStatus.KYC_FAILED);
            tx.setProviderMessage("Compte client inactif");
            paymentRepository.save(tx);
            throw new IllegalStateException("Compte client inactif");
        }

        tx.setStatus(PaymentStatus.KYC_OK);
        tx.setProviderMessage("KYC OK - " + kyc.getFull_name());
        paymentRepository.save(tx);

        PaymentProviderInitRequest providerBody = new PaymentProviderInitRequest(
                tx.getAmount(),
                tx.getReference(),
                request.getCustomerAccountNumber()
        );

        try {
            tx.setStatus(PaymentStatus.INITIATED);
            paymentRepository.save(tx);

            String initResponse = paymentClient.initPayment(request.getAppId(), providerBody);

            applyInitResponse(tx, initResponse);

            return buildResponse(tx);

        } catch (Exception firstEx) {
            if (!shouldRetry(firstEx)) {
                tx.setStatus(PaymentStatus.FAILED);
                tx.setProviderMessage(firstEx.getMessage());
                paymentRepository.save(tx);
                throw new RuntimeException("Payment initiation failed: " + firstEx.getMessage(), firstEx);
            }

            try {
                String retryResponse = paymentClient.initPayment(request.getAppId(), providerBody);

                applyInitResponse(tx, retryResponse);

                return buildResponse(tx);

            } catch (Exception retryEx) {
                tx.setStatus(PaymentStatus.FAILED);
                tx.setProviderMessage(retryEx.getMessage());
                paymentRepository.save(tx);
                throw new RuntimeException("Payment initiation failed after retry: " + retryEx.getMessage(), retryEx);
            }
        }
    }

    @Transactional
    public PaymentFormuleResponse handlePaymentCallback(PaymentCallbackRequest callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback body is required");
        }

        String reference = normalize(callback.getReference());

        if (reference == null) {
            throw new IllegalArgumentException("Callback reference is required");
        }

        PaymentTransaction tx = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            return buildResponse(tx);
        }

        String transactionId = normalize(callback.getTransactionId());
        String callbackStatus = normalize(callback.getStatus());
        String callbackMessage = normalize(callback.getMessage());

        if (transactionId != null) {
            tx.setExternalTransactionId(transactionId);
        }

        if (callbackStatus != null) {
            tx.setCallbackRawStatus(callbackStatus);
        }

        if (callbackMessage != null) {
            tx.setProviderMessage(callbackMessage);
        }

        String status = callbackStatus == null ? "" : callbackStatus.trim().toUpperCase();

        if ("SUCCESS".equals(status) || "PAID".equals(status) || "COMPLETED".equals(status)) {
            tx.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(tx);

            ActivationFormuleStructureRequest activationRequest = new ActivationFormuleStructureRequest();
            activationRequest.setStructureId(tx.getStructureId());
            activationRequest.setFormuleId(tx.getFormuleId());

            abonnementStructureService.activerOuProlonger(activationRequest);

        } else if ("FAILED".equals(status)) {
            tx.setStatus(PaymentStatus.FAILED);

        } else if ("CANCELLED".equals(status)) {
            tx.setStatus(PaymentStatus.CANCELLED);

        } else if ("EXPIRED".equals(status)) {
            tx.setStatus(PaymentStatus.EXPIRED);

        } else {
            tx.setStatus(PaymentStatus.PENDING);
        }

        paymentRepository.save(tx);

        return buildResponse(tx);
    }

    @Transactional(readOnly = true)
    public PaymentFormuleResponse getPaymentByReference(String reference) {
        PaymentTransaction tx = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        return buildResponse(tx);
    }

    @Transactional(readOnly = true)
    public PaymentFormuleResponse getPaymentById(String paymentId) {
        PaymentTransaction tx = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        return buildResponse(tx);
    }

    @Transactional(readOnly = true)
    public String checkProviderStatus(String paymentId) {
        PaymentTransaction tx = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment transaction not found"));

        if (tx.getExternalTransactionId() == null || tx.getExternalTransactionId().isBlank()) {
            throw new IllegalStateException("External transaction ID not available yet");
        }

        return paymentClient.paymentStatus(
                tx.getAppId(),
                tx.getExternalTransactionId()
        );
    }

    private void applyInitResponse(PaymentTransaction tx, String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            String providerStatus = readAny(root, "status", "state", "result");
            String transactionId = readAny(
                    root,
                    "reference_id",
                    "transactionId",
                    "transaction_id",
                    "providerTransactionId",
                    "reference"
            );

            String message = extractMessage(rawJson, "Payment initiated");

            if (providerStatus != null) {
                String ps = providerStatus.trim().toUpperCase();

                if (ps.contains("FAILED") || ps.contains("ERROR")) {
                    tx.setStatus(PaymentStatus.FAILED);
                    tx.setProviderMessage(message);
                    paymentRepository.save(tx);
                    throw new IllegalStateException("Provider returned failed status: " + providerStatus);
                }
            }

            tx.setStatus(PaymentStatus.PENDING);
            tx.setExternalTransactionId(transactionId);
            tx.setProviderMessage(message);
            paymentRepository.save(tx);

        } catch (RuntimeException re) {
            throw re;

        } catch (Exception e) {
            tx.setStatus(PaymentStatus.FAILED);
            tx.setProviderMessage(e.getMessage());
            paymentRepository.save(tx);
            throw new RuntimeException("Unable to parse payment init response", e);
        }
    }

    private PaymentFormuleResponse buildResponse(PaymentTransaction tx) {
        return new PaymentFormuleResponse(
                tx.getPaymentId(),
                tx.getStructureId(),
                tx.getFormuleId(),
                tx.getReference(),
                tx.getStatus().name(),
                tx.getAmount(),
                tx.getExternalTransactionId(),
                tx.getProviderMessage()
        );
    }

    private void validateInitRequest(PaymentFormuleInitRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Le body est obligatoire.");
        }

        if (request.getStructureId() == null || request.getStructureId().isBlank()) {
            throw new IllegalArgumentException("structureId est obligatoire.");
        }

        if (request.getFormuleId() == null || request.getFormuleId().isBlank()) {
            throw new IllegalArgumentException("formuleId est obligatoire.");
        }

        if (request.getAppId() == null || request.getAppId().isBlank()) {
            throw new IllegalArgumentException("appId est obligatoire.");
        }

        if (request.getOperatorName() == null || request.getOperatorName().isBlank()) {
            throw new IllegalArgumentException("operatorName est obligatoire.");
        }

        if (request.getCustomerAccountNumber() == null || request.getCustomerAccountNumber().isBlank()) {
            throw new IllegalArgumentException("customerAccountNumber est obligatoire.");
        }
    }

    private String extractMessage(String rawJson, String fallback) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String message = readAny(root, "message", "label", "description", "detail");
            return message != null ? message : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String readAny(JsonNode root, String... fieldNames) {
        for (String field : fieldNames) {
            JsonNode node = root.get(field);

            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                return node.asText();
            }
        }

        return null;
    }

    private boolean shouldRetry(Exception ex) {
        String message = ex.getMessage();

        if (message == null) {
            return false;
        }

        String upper = message.toUpperCase();

        return upper.contains("502")
                || upper.contains("RENEWSECRET")
                || upper.contains("AUTHENTICATION_FAILED")
                || upper.contains("PVIT_RENEW_NETWORK_ERROR");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String generateUniquePaymentReference() {
        for (int attempt = 0; attempt < MAX_REFERENCE_GENERATION_ATTEMPTS; attempt++) {
            String reference = generatePaymentReferenceCandidate();

            if (paymentRepository.findByReference(reference).isEmpty()) {
                return reference;
            }
        }

        throw new IllegalStateException("Unable to generate unique payment reference");
    }

    private String generatePaymentReferenceCandidate() {
        StringBuilder reference = new StringBuilder(PAYMENT_REFERENCE_PREFIX);

        for (int i = 0; i < PAYMENT_REFERENCE_DIGITS; i++) {
            reference.append(SECURE_RANDOM.nextInt(10));
        }

        return reference.toString();
    }
}