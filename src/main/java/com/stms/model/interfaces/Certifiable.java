package com.stms.model.interfaces;

/**
 * Interface representing entities that issue or define qualification certifications.
 * Demonstrates Abstraction and contract enforcement in OOP.
 */
public interface Certifiable {
    boolean isEligibleForCertificate(double score, boolean attended);
    int getCertificateValidityMonths();
    String getCertificateTitle();
}
