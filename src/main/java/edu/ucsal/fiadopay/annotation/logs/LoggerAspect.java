package edu.ucsal.fiadopay.annotation.logs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.stereotype.Component;

import edu.ucsal.fiadopay.annotation.logs.Logger;

@Aspect
@Component
public class LoggerAspect {

    @Around("@annotation(Logger)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long inicio = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        Thread.sleep(100);
        long fim = System.currentTimeMillis();
        long tempoMs = fim - inicio;

        Log log = Log.getInstance();
        log.info(
            String.format("Função %s executada em %d ms", joinPoint.getSignature(), tempoMs)
        );
        return result;
    }
}
