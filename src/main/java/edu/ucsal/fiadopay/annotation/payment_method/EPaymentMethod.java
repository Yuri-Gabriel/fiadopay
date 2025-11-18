package edu.ucsal.fiadopay.annotation.payment_method;

public enum EPaymentMethod {
    PIX("PIX"),
    BOLETO("BOLETO"),
    CARTAO("CARTAO");

    public String value;

    EPaymentMethod(String value) {
        this.value = value;
    }
}
