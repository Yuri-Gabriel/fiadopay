package edu.ucsal.fiadopay.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VelocityCheck {

    // Máximo de tentativas permitidas na janela
    int maxAttempts();

    // Tamanho da janela em segundos
    int timeWindowSeconds();
}
