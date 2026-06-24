package com.esiitech.monbondocteurv2.integration;

import com.esiitech.monbondocteurv2.dto.KycResponse;
import com.esiitech.monbondocteurv2.dto.PaymentProviderInitRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SolutechPaymentClient {

    private final RestClient restClient;

    public SolutechPaymentClient(@Value("${solutech.payment.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public KycResponse kyc(String appId, String customerAccountNumber) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/pvit/kyc")
                        .queryParam("appId", appId)
                        .queryParam("customerAccountNumber", customerAccountNumber)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(KycResponse.class);
    }

    public String initPayment(String appId, PaymentProviderInitRequest body) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/pvit/payments")
                        .queryParam("appId", appId)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public String paymentStatus(String appId, String transactionId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/pvit/payments/status")
                        .queryParam("appId", appId)
                        .queryParam("transactionId", transactionId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }
}