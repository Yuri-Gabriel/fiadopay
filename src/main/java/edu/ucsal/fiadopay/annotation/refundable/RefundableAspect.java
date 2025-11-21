package edu.ucsal.fiadopay.annotation.refundable;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RefundableAspect {
  @Around("@annotation(refundable)")
  public Object around(ProceedingJoinPoint joinPoint, Refundable refundable) throws Throwable {
    Object result = joinPoint.proceed();

    if (result != null) {
      validate(result, refundable);
    }

    return result;
  }

  private void validate(Object target, Refundable annotation) throws NoSuchFieldException, SecurityException {
    try {
      Field deliveredField = target.getClass().getDeclaredField("delivered");
      Field lastAttemptAtField = target.getClass().getDeclaredField("lastAttemptAt");

      if (deliveredField == null || lastAttemptAtField == null) {
        throw new IllegalStateException("Campos não encontrados.");
      }

      deliveredField.setAccessible(true);
      lastAttemptAtField.setAccessible(true);

      // verifico se a mercadoria foi entregue
      Boolean checkDeliveredStatus = (Boolean) deliveredField.get(target);
      if (checkDeliveredStatus == null || !checkDeliveredStatus) {
        throw new IllegalStateException(
            "A mercadoria não foi entregue. Estorno não permitido.");
      }

      // caso tenha sido entregue, sigo para verificar se o estorno é ainda permitido
      Instant deliveryDate = (Instant) lastAttemptAtField.get(target);
      if (deliveryDate == null) {
        throw new IllegalStateException(
            "Data de entrega não encontrada.");
      }

      long daysSinceDelivery = ChronoUnit.DAYS.between(deliveryDate, Instant.now());
      int allowedDays = annotation.days();

      if (daysSinceDelivery > allowedDays) {
        throw new IllegalStateException(
            String.format(
                "Estorno não permitido. Prazo de %d dias expirado (transação há %d dias).",
                allowedDays,
                daysSinceDelivery));
      }

    } catch (IllegalAccessException e) {
      throw new RuntimeException("Erro ao acessar campos do objeto.", e);
    }
  }
}