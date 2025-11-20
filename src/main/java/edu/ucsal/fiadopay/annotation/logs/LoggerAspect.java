package edu.ucsal.fiadopay.annotation.logs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

// Importações para pegar o Request HTTP
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggerAspect {

    @Around("@annotation(logger)")
    public Object around(ProceedingJoinPoint joinPoint, Logger logger) throws Throwable {
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        String httpMethod = request.getMethod();
        String requestPath = request.getRequestURI();

        long inicio = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long fim = System.currentTimeMillis();
        long tempoMs = fim - inicio;

        Log log = Log.getInstance(logger.file());
        
        log.info(
            String.format("[%s] %s - Função %s executada em %d ms", 
                httpMethod,
                requestPath,
                joinPoint.getSignature(),
                tempoMs
            )
        );

        return result;
    }
}