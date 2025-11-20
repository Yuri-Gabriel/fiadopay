package edu.ucsal.fiadopay.config;

import edu.ucsal.fiadopay.annotation.VelocityCheck;
import edu.ucsal.fiadopay.service.VelocityRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VelocityCheckAspect {

    private final VelocityRateLimiter rateLimiter;
    private final HttpServletRequest request;

    public VelocityCheckAspect(VelocityRateLimiter rateLimiter,
                               HttpServletRequest request) {
        this.rateLimiter = rateLimiter;
        this.request = request;
    }

    @Around("@annotation(velocityCheck)")
    public Object enforceVelocity(ProceedingJoinPoint pjp,
                                  VelocityCheck velocityCheck) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();

        String methodName = signature.getMethod().getName();
        String path = request.getRequestURI();
        String clientIp = request.getRemoteAddr();

        // chave = método + rota + IP
        String key = methodName + "|" + path + "|" + clientIp;

        rateLimiter.check(
                key,
                velocityCheck.maxAttempts(),
                velocityCheck.timeWindowSeconds()
        );

        return pjp.proceed();
    }
}
