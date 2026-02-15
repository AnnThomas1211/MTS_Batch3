package com.fidelity.mts.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http.csrf(csrf->csrf.disable());
		
		http.authorizeHttpRequests(
				auth->auth.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
				.anyRequest().authenticated());
		
		http.httpBasic(Customizer.withDefaults());
		
		return http.build();
	}
}

