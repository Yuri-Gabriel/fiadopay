package edu.ucsal.fiadopay.annotation.payment_method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
public @interface PaymentMethod {
    String message() default "Método de pagamento inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    EPaymentMethod[] methods();
}
