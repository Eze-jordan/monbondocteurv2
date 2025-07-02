package com.esiitech.monbondocteurv2.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI MonBonDocteurV2API() {
        return new OpenAPI()
                .info(new Info()
                        .title("MON BON DOCTEUR API")
                        .version("1.0.0")
                        .description("UNE API POUR LA GESTION DES RENDEZ-VOUS ET DES STRUCTURES SANITAIRES"));
    }
}
