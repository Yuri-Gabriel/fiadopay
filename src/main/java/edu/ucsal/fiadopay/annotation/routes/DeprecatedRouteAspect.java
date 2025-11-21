package edu.ucsal.fiadopay.annotation.routes;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class DeprecatedRouteAspect {

    private static final Logger log = LoggerFactory.getLogger(DeprecatedRouteAspect.class);

    @Around("@annotation(deprecated)")
    public Object handleDeprecatedRoute(ProceedingJoinPoint joinPoint, DeprecatedRoute deprecated) throws Throwable {
        
        String methodName = joinPoint.getSignature().getName();
        HttpServletRequest request = getRequest();
        String clientIp = getClientIp(request);
        String endpoint = request != null ? request.getRequestURI() : "UNKNOWN";
        
        // 1. LOG WARNING com todas informações
        StringBuilder message = new StringBuilder();
        message.append("Rota depreciada: ");
        message.append(methodName).append(" (").append(endpoint).append(")");
        message.append(" | Since: ").append(deprecated.sinceVersion());
        message.append(" | Removal: ").append(deprecated.removalVersion());
        
        if (!deprecated.replacedBy().isEmpty()) {
            message.append(" | Replaced by: ").append(deprecated.replacedBy());
        }
        
        message.append(" | Motivo: ").append(deprecated.reason());
        message.append(" | Client IP: ").append(clientIp);
        
        log.warn(message.toString());
        
        // 2. SE rejectRequests = true, rejeitar com HTTP 410 Gone
        if (deprecated.rejectRequests()) {
            String errorMsg = String.format(
                "Route %s is deprecated since %s and will be removed in %s. %s",
                endpoint,
                deprecated.sinceVersion(),
                deprecated.removalVersion(),
                deprecated.reason()
            );
            log.error("Rota depreciada rejeitada {} from {}", endpoint, clientIp);
            throw new ResponseStatusException(HttpStatus.GONE, errorMsg);
        }
        
        // 3. EXECUTAR método normalmente
        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "UNKNOWN";
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}