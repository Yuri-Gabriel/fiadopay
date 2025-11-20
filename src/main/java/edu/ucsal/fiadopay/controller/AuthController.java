package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.annotation.logs.Logger;
import edu.ucsal.fiadopay.controller.request.TokenRequest;
import edu.ucsal.fiadopay.controller.response.TokenResponse;
import edu.ucsal.fiadopay.model.Merchant;
import edu.ucsal.fiadopay.repo.MerchantRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/fiadopay/auth")
@RequiredArgsConstructor
public class AuthController {
  private final MerchantRepository merchants;

  @Logger(file = "auth.log")
  @PostMapping("/token")
  public TokenResponse token(@RequestBody @Valid TokenRequest req) {
    var merchant = merchants.findByClientId(req.client_id())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    if (!merchant.getClientSecret().equals(req.client_secret())
        || merchant.getStatus()!= Merchant.Status.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return new TokenResponse("FAKE-"+merchant.getId(), "Bearer", 3600);
  }
}
