package com.esiitech.monbondocteurv2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure Spring Boot pour servir des fichiers statiques depuis le répertoire de téléchargement
        registry.addResourceHandler("/uploads/**")  // Accès via /uploads/ dans l'URL
                .addResourceLocations("file:D:/Ptojet spring/NEW/monbondocteurv2/uploads/utilisateurs/") // Spécifie le chemin absolu du répertoire
                .addResourceLocations("file:D:/Ptojet spring/NEW/monbondocteurv2/uploads/medecins/") // Spécifie le chemin absolu du répertoire
                .addResourceLocations("file:D:/Ptojet spring/NEW/monbondocteurv2/uploads/structuresanitaire/");  // Spécifie le chemin absolu du répertoire

    }
}
