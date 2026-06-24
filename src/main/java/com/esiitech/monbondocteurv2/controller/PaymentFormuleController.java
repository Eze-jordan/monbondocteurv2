package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.PaymentCallbackRequest;
import com.esiitech.monbondocteurv2.dto.PaymentFormuleInitRequest;
import com.esiitech.monbondocteurv2.dto.PaymentFormuleResponse;
import com.esiitech.monbondocteurv2.service.PaymentFormuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/V2/payments/formules")
@Tag(name = "Paiements Formules", description = "Paiement des formules d'abonnement des structures sanitaires")
public class PaymentFormuleController {

    private final PaymentFormuleService paymentService;
    private final String callbackSecret;

    public PaymentFormuleController(
            PaymentFormuleService paymentService,
            @Value("${solutech.payment.callback-secret:}") String callbackSecret
    ) {
        this.paymentService = paymentService;
        this.callbackSecret = callbackSecret;
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Initier le paiement d'une formule pour une structure sanitaire")
    public ResponseEntity<PaymentFormuleResponse> initiate(@RequestBody PaymentFormuleInitRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @PostMapping("/callback")
    @Operation(summary = "Recevoir le callback provider après paiement")
    public ResponseEntity<PaymentFormuleResponse> callback(
            @RequestHeader(value = "X-Payment-Callback-Secret", required = false) String receivedSecret,
            @RequestBody PaymentCallbackRequest request
    ) {
        verifyCallbackSecret(receivedSecret);
        return ResponseEntity.ok(paymentService.handlePaymentCallback(request));
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un paiement par référence")
    public ResponseEntity<PaymentFormuleResponse> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(paymentService.getPaymentByReference(reference));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer un paiement par ID")
    public ResponseEntity<PaymentFormuleResponse> getByPaymentId(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/{paymentId}/status-provider")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vérifier le statut du paiement chez le provider")
    public ResponseEntity<String> checkProviderStatus(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.checkProviderStatus(paymentId));
    }

    private void verifyCallbackSecret(String receivedSecret) {
        if (callbackSecret == null || callbackSecret.isBlank()) {
            return;
        }

        if (receivedSecret == null || !callbackSecret.equals(receivedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid payment callback secret");
        }
    }
}