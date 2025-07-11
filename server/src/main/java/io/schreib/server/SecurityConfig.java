package io.schreib.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Allow unauthenticated access to the webhook endpoint
                .requestMatchers("/webhook/onedrive").permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            // Configure the OAuth2 login process
            .oauth2Login(withDefaults())
            // Configure a simple logout
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // Disable CSRF for the webhook endpoint for simplicity. 
            // In a production app, you'd want a more robust CSRF strategy.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/webhook/onedrive"));

        return http.build();
    }
}
