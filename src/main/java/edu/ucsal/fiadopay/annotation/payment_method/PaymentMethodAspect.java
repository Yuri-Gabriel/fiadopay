package edu.ucsal.fiadopay.annotation.payment_method;

import java.lang.reflect.Field;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PaymentMethodAspect {
    @Around("@annotation(PaymentMethod)")
    public Object around(ProceedingJoinPoint joinPoint, PaymentMethod paymentMethod) throws Throwable {

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            validate(arg, paymentMethod);
        }

        return joinPoint.proceed();
    }

    private void validate(Object target, PaymentMethod annotation) {
        EPaymentMethod[] methods = annotation.methods();
        String current = getMethod(target);
        boolean accept = false;

        for(int i = 0; i <= methods.length - 1; i++) {
            accept = methods[i].value.equals(current);

            if(accept) break;
        }
        if (!accept) {
            throw new IllegalStateException(
                String.format("Metodo de pagamento não aceito: %s", current)
            );
        }
    }

    private String getMethod(Object target) {
        try {
            Field field = target.getClass().getDeclaredField("method");
            field.setAccessible(true);
            return (String) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Objeto não possui campo 'status'.");
        }
    }
}

/*
package edu.ucsal.fiadopay.validators;

import edu.ucsal.fiadopay.annotation.MerchantStatus;
import edu.ucsal.fiadopay.model.Merchant.Status;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Aspect
@Component
public class MerchantStatusValidator {

    @Around("@annotation(merchantStatus)")
    public Object around(ProceedingJoinPoint joinPoint, MerchantStatus merchantStatus) throws Throwable {

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            validate(arg, merchantStatus);
        }

        return joinPoint.proceed();
    }

    private void validate(Object target, MerchantStatus annotation) {
        Status allowed = annotation.allowed();
        Status current = getStatus(target);

        if (!current.equals(allowed)) {
            throw new IllegalStateException(
                "Merchant com status inválido! Atual: " + current + " — Permitido: " + allowed
            );
        }
    }

    private Status getStatus(Object target) {
        try {
            Field field = target.getClass().getDeclaredField("status");
            field.setAccessible(true);
            return (Status) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Objeto não possui campo 'status'.");
        }
    }
}

*/
