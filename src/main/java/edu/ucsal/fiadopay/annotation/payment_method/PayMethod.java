package edu.ucsal.fiadopay.annotation.payment_method;

public enum PayMethod {
    PIX("PIX"),
    BOLETO("BOLETO"),
    CARTAO("CARTAO");

    public String value;

    PayMethod(String value) {
        this.value = value;
    }
}
