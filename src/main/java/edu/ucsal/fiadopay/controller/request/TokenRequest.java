package edu.ucsal.fiadopay.controller.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
    @NotBlank String client_id,
    @NotBlank String client_secret
) {}
