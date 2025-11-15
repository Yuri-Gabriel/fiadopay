package edu.ucsal.fiadopay.annotation.logs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {

    @Around("@annotation(Logger)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long inicio = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long fim = System.currentTimeMillis() - inicio;

        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.info("{} ---------------------executado em {} ms", joinPoint.getSignature(), fim);
        return result;
    }
}
