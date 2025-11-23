package com.esiitech.monbondocteurv2.config;


import com.esiitech.monbondocteurv2.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuditInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;

    public AuditInterceptor(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = (auth != null && auth.getName() != null) ? auth.getName() : "ANONYMOUS";
        String role = (auth != null && auth.getAuthorities() != null) ? auth.getAuthorities().toString() : "NONE";

        auditLogService.logAction(
                request.getMethod() + " " + request.getRequestURI(),
                "Appel API automatique",
                null, // si tu veux pas gérer d’ID ici
                userEmail,
                role,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return true;
    }
}

