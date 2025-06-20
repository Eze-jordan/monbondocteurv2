package com.esiitech.monbondocteurv2.securite;

import com.esiitech.monbondocteurv2.service.UtilisateurService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Service
public class JwtFiller extends OncePerRequestFilter {

    private UtilisateurService utilisateurService;
    private  JwtService jwtService;

    public JwtFiller (UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String token = null;
        String username = null;
        boolean isTokenExpired = true;


        final String authorization = request.getHeader("Authorization");
        try {
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
                isTokenExpired = jwtService.isTokenExpired(token);
                username = jwtService.extractUsername(token);
            }

            if (!isTokenExpired && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = utilisateurService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails,
                                null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            System.out.println("Erreur dans JwtFilter : " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("Erreur d'authentification : " + e.getMessage());
        }    }
}

