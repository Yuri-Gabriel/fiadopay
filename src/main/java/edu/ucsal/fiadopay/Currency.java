package edu.ucsal.fiadopay;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe as moedas aceitas por um endpoint ou campo.
 * Exemplo:
 *
 * {@code
 *  @Currency(allowed = {"BRL"})
 *  public void pagar(PaymentRequest req) { ... }
 * }
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface Currency {

    /**
     * Moedas que são permitidas para esse endpoint/campo.
     * Ex.: {"BRL"} ou {"BRL", "USD"}
     */
    String[] allowedCurrencies() default {"BRL"};
}