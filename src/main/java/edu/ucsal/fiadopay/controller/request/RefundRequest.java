package edu.ucsal.fiadopay.controller.request;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
    @NotBlank String paymentId
) {}
