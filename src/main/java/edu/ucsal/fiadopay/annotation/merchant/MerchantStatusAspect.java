package edu.ucsal.fiadopay.annotation.merchant;

import java.lang.reflect.Field;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import edu.ucsal.fiadopay.annotation.logs.Logger;

@Aspect
@Component
public class MerchantStatusAspect {
  @Logger
  @Around("@annotation(merchantstatus)")
  public Object around(ProceedingJoinPoint joinPoint, MerchantStatus merchantStatus) throws Throwable {

    Object[] args = joinPoint.getArgs();
    for (Object arg : args) {
      validate(arg, merchantStatus);
    }

    return joinPoint.proceed();
  }

  private void validate(Object target, MerchantStatus annotation) {
    // reflection pra validar se o atributo status é igual ao que eu espero na
    // anotação
    try {
      Field field = target.getClass().getDeclaredField("status");
      field.setAccessible(true);
      Object statusValue = field.get(target);

      if (!statusValue.equals(annotation.value())) {
        throw new IllegalStateException(
            String.format("Status inválido: esperado %s, recebido %s",
                annotation.value(), statusValue));
      }
    } catch (Exception e) {
      throw new RuntimeException("O comerciante não possui o campo 'Status'.");
    }
  }

}
