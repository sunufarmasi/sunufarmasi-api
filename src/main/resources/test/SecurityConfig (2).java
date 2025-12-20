// ═══════════════════════════════════════════════════════════════════════════════
// AJOUT À FAIRE DANS VOTRE SecurityConfig.java
// ═══════════════════════════════════════════════════════════════════════════════
//
// Dans la méthode securityFilterChain, ajouter "/api/v1/init/**" aux routes publiques :
//
// .requestMatchers(
//     "/api/v1/auth/**",
//     "/api/v1/public/**",
//     "/api/v1/init/**",          // ⚠️ AJOUTER CETTE LIGNE
//     "/api/v1/gardes",
//     "/api/v1/gardes/**",
//     "/api/v1/pharmacies/proximite",
//     "/api/v1/pharmacies/garde",
//     "/swagger-ui/**",
//     "/v3/api-docs/**",
//     "/actuator/health"
// ).permitAll()
//
// ═══════════════════════════════════════════════════════════════════════════════

package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sn.sunufarmasi.security.JwtAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration de sécurité Spring Security
 * 
 * @author WeCan
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Routes publiques (sans authentification)
     */
    private static final String[] PUBLIC_ROUTES = {
        // Auth
        "/api/v1/auth/**",
        
        // ⚠️ INIT - À DÉSACTIVER EN PRODUCTION !
        "/api/v1/init/**",
        
        // Public
        "/api/v1/public/**",
        
        // Gardes publiques
        "/api/v1/gardes",
        "/api/v1/gardes/aujourd-hui",
        "/api/v1/gardes/semaine",
        "/api/v1/gardes/proximite",
        
        // Pharmacies publiques
        "/api/v1/pharmacies/proximite",
        "/api/v1/pharmacies/garde",
        
        // Localisation publique
        "/api/v1/public/pays",
        "/api/v1/public/pays/**",
        "/api/v1/public/regions",
        "/api/v1/public/regions/**",
        "/api/v1/public/departements",
        "/api/v1/public/departements/**",
        "/api/v1/public/communes",
        "/api/v1/public/communes/**",
        
        // Swagger
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        
        // Actuator
        "/actuator/health",
        "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour API REST
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurer CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Session stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configurer les autorisations
            .authorizeHttpRequests(auth -> auth
                // Routes publiques
                .requestMatchers(PUBLIC_ROUTES).permitAll()
                
                // Routes Admin
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // Routes Syndicat
                .requestMatchers("/api/v1/syndicat/**").hasAnyRole("ADMIN", "ADMIN_SYNDICAT")
                
                // Routes Pharmacien
                .requestMatchers("/api/v1/pharmacie/**").hasAnyRole("ADMIN", "PHARMACIEN")
                
                // Tout le reste nécessite une authentification
                .anyRequest().authenticated()
            )
            
            // Ajouter le filtre JWT
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",    // React dev
            "http://localhost:4200",    // Angular dev
            "http://localhost:8080",    // Vue dev
            "http://localhost:5173",    // Vite dev
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173",
            "https://sunufarmasi.sn",   // Production
            "https://www.sunufarmasi.sn",
            "https://app.sunufarmasi.sn"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "X-Total-Count", "X-Page-Number"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
