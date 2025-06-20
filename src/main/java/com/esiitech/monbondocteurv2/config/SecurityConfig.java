package com.esiitech.monbondocteurv2.config;

import com.esiitech.monbondocteurv2.securite.JwtFiller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFiller jwtFiller;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(BCryptPasswordEncoder bCryptPasswordEncoder, JwtFiller jwtFiller, UserDetailsService userDetailsService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFiller = jwtFiller;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity HttpSecurity) throws Exception {
        return
                HttpSecurity
                      .csrf(AbstractHttpConfigurer::disable)
                      .authorizeHttpRequests(authorize ->
                              authorize
                                    .requestMatchers("/api/users/create",
                                            "/api/users/activation",
                                            "/api/users/connexion",
                                            "/swagger-ui/**",
                                            "/v3/api-docs/**"
                                    ).permitAll()


                                      .anyRequest().authenticated()

                ).sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS))
                        .addFilterBefore(jwtFiller, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return  authenticationConfiguration.getAuthenticationManager();
    }



    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return daoAuthenticationProvider;
    }
}