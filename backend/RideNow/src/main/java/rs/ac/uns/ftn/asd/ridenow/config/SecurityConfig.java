package rs.ac.uns.ftn.asd.ridenow.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import rs.ac.uns.ftn.asd.ridenow.security.JwtRequestFilter;

import java.util.List;

@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // WebSocket endpoints MUST come first to avoid conflicts with REST API patterns
                        .requestMatchers("/api/notifications/websocket").permitAll()
                        .requestMatchers("/api/chat/websocket/**").permitAll()
                        // Auth endpoints
                        .requestMatchers(
                                "/api/auth/login", "/api/auth/register",
                                "/api/auth/activate", "/api/auth/forgot-password",
                                "/api/auth/reset-password", "/api/rides/estimate", "/api/auth/verify-reset-code",
                                "api/auth/activate-code", "api/auth/resend-activation-email")
                        .permitAll()
                        // Static resources
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/admins/driver-register").hasRole("ADMIN")
                        .requestMatchers("/api/admins/driver-requests").hasRole("ADMIN")
                        .requestMatchers("/api/admins/driver-requests/").hasRole("ADMIN")
                        .requestMatchers("/api/admins/price-configs").hasRole("ADMIN")
                        .requestMatchers("/api/admins/users/").hasRole("ADMIN")
                        .requestMatchers("/api/admins/all-users/").hasRole("ADMIN")
                        .requestMatchers("/api/admins/ride-history").hasRole("ADMIN")
                        .requestMatchers("/api/admins/report").hasRole("ADMIN")
                        .requestMatchers("api/rides/active-rides").hasRole("ADMIN")
                        .requestMatchers("/api/rides/inconsistency").hasRole("USER")
                        .requestMatchers("/api/driver/**").permitAll()
                        .requestMatchers("/api/driver/activate-account").permitAll()
                        .requestMatchers("/api/driver/change-request").hasRole("DRIVER")
                        .requestMatchers("/api/driver/can-start-ride").hasRole("DRIVER")
                        .requestMatchers("/api/admins/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/rides/{id}/track").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/rides/my-current-ride").hasAnyRole("USER", "DRIVER")
                        .requestMatchers("/api/auth/logout").hasAnyRole("ADMIN", "USER", "DRIVER")
                        .requestMatchers("/api/rides/my-upcoming-rides").hasRole("USER")
                        .requestMatchers("/api/rides/*/cancel").hasAnyRole("USER", "DRIVER")
                        .requestMatchers("/api/rides/panic-alert").hasAnyRole("USER", "DRIVER")
                        .requestMatchers("/api/rides/estimate-route").hasRole("USER")
                        .requestMatchers("/api/rides/*/start").hasRole("DRIVER")
                        .requestMatchers("/api/rides/reorder-ride").hasAnyRole("USER", "ADMINf")
                        .requestMatchers("/api/rides/order-ride").hasRole("USER")
                        .requestMatchers("/api/vehicles/").permitAll()
                        .requestMatchers("/api/passengers/favorite-routes").hasRole("USER")
                        .requestMatchers("/api/passengers/favorite-routes/").hasRole("USER")
                        .requestMatchers("/api/passengers/ride-history").hasRole("USER")
                        .requestMatchers("/api/driver/update-location").hasRole("DRIVER")

                        .requestMatchers("/api/chats/").hasRole("ADMIN")
                        .requestMatchers("/api/chats/user").hasAnyRole("USER", "DRIVER")
                        .requestMatchers("/api/chats/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/chats/message/{id}").hasAnyRole("USER", "DRIVER", "ADMIN")
                        .requestMatchers("/api/chats/{id}/close").hasRole("ADMIN")
                        .requestMatchers("/api/panic-alerts/unresolved").hasRole("ADMIN")
                        .requestMatchers("/api/panic-alerts/all").hasRole("ADMIN")
                        .requestMatchers("/api/panic-alerts/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/panic-alerts/{id}/resolve").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
