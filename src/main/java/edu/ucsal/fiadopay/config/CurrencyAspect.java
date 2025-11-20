package edu.ucsal.fiadopay.config;

import edu.ucsal.fiadopay.annotation.Currency;
import edu.ucsal.fiadopay.exception.InvalidCurrencyException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Aplica a regra de @Currency.
 *
 * Intercepta métodos anotados com @Currency, procura um campo "currency"
 * nos parâmetros do método e valida se o valor está entre as moedas permitidas.
 */
@Aspect
@Component
public class CurrencyAspect {

    @Around("@annotation(currencyAnnotation)")
    public Object validateCurrency(ProceedingJoinPoint pjp,
                                   Currency currencyAnnotation) throws Throwable {

        // Moedas permitidas definidas na anotação (@Currency(allowed = {...}))
        String[] allowed = currencyAnnotation.allowed();

        // Tenta achar o valor de "currency" em algum argumento do método
        String currencyValue = extractCurrencyFromArgs(pjp.getArgs());

        // Se não achar currency, deixa passar (ou você poderia decidir bloquear)
        if (currencyValue == null) {
            return pjp.proceed();
        }

        boolean isAllowed = Arrays.stream(allowed)
                .anyMatch(c -> c.equalsIgnoreCase(currencyValue));

        if (!isAllowed) {
            // Usa o construtor novo: (currency, allowed)
            throw new InvalidCurrencyException(currencyValue, allowed);
        }

        return pjp.proceed();
    }

    /**
     * Procura um field chamado "currency" nos parâmetros do método.
     */
    private String extractCurrencyFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg == null) continue;

            try {
                Field field = arg.getClass().getDeclaredField("currency");
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value != null) {
                    return value.toString();
                }
            } catch (NoSuchFieldException ignored) {
                // esse argumento não tem field "currency", segue pro próximo
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }
}
