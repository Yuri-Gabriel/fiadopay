package edu.ucsal.fiadopay.exception;

public class InvalidCurrencyException extends RuntimeException {

    public InvalidCurrencyException(String currency, String[] allowed) {
        super("Moeda '" + currency + "' não é aceita. " +
                "Use uma destas: " + String.join(", ", allowed));
    }
}
