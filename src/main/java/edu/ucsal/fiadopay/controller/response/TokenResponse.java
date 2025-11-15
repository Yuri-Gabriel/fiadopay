package edu.ucsal.fiadopay.controller.response;
public record TokenResponse(String access_token, String token_type, long expires_in) {}
