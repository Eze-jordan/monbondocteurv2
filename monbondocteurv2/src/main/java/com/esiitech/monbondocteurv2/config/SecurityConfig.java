package com.esiitech.monbondocteurv2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity HttpSecurity) throws Exception {
        return
                HttpSecurity
                      .csrf(AbstractHttpConfigurer::disable)
                      .authorizeHttpRequests(authorize ->
                              authorize
                                    .requestMatchers("/api/users/create").permitAll()
                                      .requestMatchers("/api/users/all").permitAll()
                                    .requestMatchers("/api/users/activation").permitAll()
                                      .anyRequest().authenticated()

                ).build();

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}