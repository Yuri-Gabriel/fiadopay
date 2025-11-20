package edu.ucsal.fiadopay.annotation.payment_method;

public enum EPaymentMethod {
    PIX("PIX"),
    BOLETO("BOLETO"),
    DEBITO("DEBITO"),
    CREDITO("CREDITO");

    public String value;

    EPaymentMethod(String value) {
        this.value = value;
    }
}
