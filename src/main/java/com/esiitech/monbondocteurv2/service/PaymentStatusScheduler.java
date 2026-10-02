package com.esiitech.monbondocteurv2.service;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentStatusScheduler {

    private final TaskScheduler taskScheduler;
    private final PaymentStatusChecker paymentStatusChecker;

    public PaymentStatusScheduler(
            TaskScheduler taskScheduler,
            PaymentStatusChecker paymentStatusChecker
    ) {
        this.taskScheduler = taskScheduler;
        this.paymentStatusChecker = paymentStatusChecker;
    }

    public void scheduleStatusChecks(String paymentId) {

        scheduleCheck(paymentId, 30);
    }

    private void scheduleCheck(
            String paymentId,
            long delaySeconds
    ) {

        taskScheduler.schedule(
                () -> executeCheck(paymentId, delaySeconds),
                Instant.now().plusSeconds(delaySeconds)
        );
    }

    private void executeCheck(
            String paymentId,
            long currentDelay
    ) {

        try {

            boolean finished =
                    paymentStatusChecker.check(paymentId);

            if (finished) {
                System.out.println(
                        "PAYMENT CHECK TERMINE : " + paymentId
                );
                return;
            }

            // Maximum 3 vérifications :
            // 30 sec, 60 sec, 90 sec
            if (currentDelay < 90) {

                System.out.println(
                        "Paiement toujours PENDING : "
                                + paymentId
                                + " - nouvelle vérification dans 30 secondes"
                );

                scheduleCheck(
                        paymentId,
                        currentDelay + 30
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Erreur vérification paiement "
                            + paymentId
                            + " : "
                            + e.getMessage()
            );
        }
    }
}