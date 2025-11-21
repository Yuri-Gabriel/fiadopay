package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.annotation.logs.Logger;
import edu.ucsal.fiadopay.annotation.VelocityCheck;
import edu.ucsal.fiadopay.annotation.refundable.Refundable;
import edu.ucsal.fiadopay.controller.request.PaymentRequest;
import edu.ucsal.fiadopay.controller.response.PaymentResponse;
import edu.ucsal.fiadopay.annotation.routes.DeprecatedRoute;
import edu.ucsal.fiadopay.service.PaymentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import edu.ucsal.fiadopay.annotation.Currency;

@RestController
@RequestMapping("/fiadopay/gateway")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;

  @VelocityCheck(maxAttempts = 5, timeWindowSeconds = 60) // 5 tentativas por IP/minuto
  @Currency(allowed = {"BRL"})
  @Logger(file = "payment.log")
  @PostMapping("/payments")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<PaymentResponse> create(
      @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
      @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
      @RequestBody @Valid PaymentRequest req) {
    var resp = service.createPayment(auth, idemKey, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @Logger(file = "payment.log")
  @GetMapping("/payments/{id}")
  public PaymentResponse get(@PathVariable String id) {
    return service.getPayment(id);
  }

  // Rota criada para teste da DeprecatedRoute
  @Logger(file = "payment.log")
  @DeprecatedRoute(replacedBy = "/fiadopay/gateway/payments", sinceVersion = "1.0", removalVersion = "2.0", reason = "Use /fiadopay/gateway/payments (nova rota padrão)")
  @PostMapping("/v34/payment")
  public ResponseEntity<PaymentResponse> createDeprecated(
      @Parameter(hidden = true) @RequestHeader("Authorization") String auth,
      @RequestBody @Valid PaymentRequest req) {
    var resp = service.createPayment(auth, null, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }
}
 