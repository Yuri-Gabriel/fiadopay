package edu.ucsal.fiadopay.controller.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import edu.ucsal.fiadopay.annotation.payment_method.EPaymentMethod;
import edu.ucsal.fiadopay.annotation.payment_method.PaymentMethod;

public record PaymentRequest(
    @NotBlank @PaymentMethod(methods = {EPaymentMethod.DEBITO, EPaymentMethod.PIX}) String method,
    @NotBlank String currency,
    @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
    @Min(1) @Max(12) Integer installments,
    @Size(max = 255) String metadataOrderId
) {}
