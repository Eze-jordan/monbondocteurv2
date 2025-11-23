package com.esiitech.monbondocteurv2.config;


import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
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

        String userEmail = "ANONYMOUS";
        String userId = "UNKNOWN";
        String role = "NONE";

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {

            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails customUser) {
                userEmail = customUser.getUsername();
                userId = customUser.getId();
                role = customUser.getAuthorities().toString();
            } else {
                userEmail = auth.getName();
                role = auth.getAuthorities().toString();
            }
        }

        String action = request.getMethod() + " " + request.getRequestURI();

        String description = String.format(
                "Requête %s effectuée par %s (ID=%s, rôle=%s)",
                action,
                userEmail,
                userId,
                role
        );

        auditLogService.logAction(
                action,
                description,
                userId,
                userEmail,
                role,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        return true;
    }

}

