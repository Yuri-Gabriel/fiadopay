package edu.ucsal.fiadopay.annotation.payment_method;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentMethodValidator implements ConstraintValidator<PaymentMethod, String> {

    private EPaymentMethod[] acceptedMethods;

    @Override
    public void initialize(PaymentMethod constraintAnnotation) {
        this.acceptedMethods = constraintAnnotation.methods();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null) return false;

        for (EPaymentMethod method : acceptedMethods) {
            if (method.value.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}