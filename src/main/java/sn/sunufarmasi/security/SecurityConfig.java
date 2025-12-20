//package sn.sunufarmasi.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfigurationSource;
//
///**
// * Configuration de Spring Security avec JWT + OTP
// *
// * AUTHENTIFICATION OTP :
// * - Pas de UserDetailsService (pas de mot de passe en base)
// * - Auth par OTP uniquement
// * - JWT généré après vérification OTP
// *
// * @author WeCan
// * @since 1.0.0
// */
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity(
//        securedEnabled = true,
//        jsr250Enabled = true,
//        prePostEnabled = true
//)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final CorsConfigurationSource corsConfigurationSource;
//
//    // ═══════════════════════════════════════════════════════════
//    // PASSWORD ENCODER
//    // ═══════════════════════════════════════════════════════════
//
//    /**
//     * Encodeur de mots de passe BCrypt
//     * (Gardé pour compatibilité future si besoin)
//     *
//     * @return PasswordEncoder
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(12); // Force 12 (très sécurisé)
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    // AUTHENTICATION MANAGER
//    // ═══════════════════════════════════════════════════════════
//
//    /**
//     * Authentication Manager pour gérer l'authentification
//     * (Gardé pour compatibilité, mais pas utilisé avec OTP)
//     *
//     * @param authConfig Configuration d'authentification
//     * @return AuthenticationManager
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration authConfig
//    ) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    // SECURITY FILTER CHAIN
//    // ═══════════════════════════════════════════════════════════
//
//    /**
//     * Configuration de la chaîne de filtres de sécurité
//     *
//     * @param http HttpSecurity
//     * @return SecurityFilterChain
//     */
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // ═══════════════════════════════════════════════════════════
//                // CORS
//                // ═══════════════════════════════════════════════════════════
//                .cors(cors -> cors.configurationSource(corsConfigurationSource))
//
//                // ═══════════════════════════════════════════════════════════
//                // CSRF (Désactivé pour API REST avec JWT)
//                // ═══════════════════════════════════════════════════════════
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // ═══════════════════════════════════════════════════════════
//                // GESTION DES EXCEPTIONS
//                // ═══════════════════════════════════════════════════════════
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
//                )
//
//                // ═══════════════════════════════════════════════════════════
//                // SESSION (Stateless pour JWT)
//                // ═══════════════════════════════════════════════════════════
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//
//                // ═══════════════════════════════════════════════════════════
//                // AUTORISATION DES REQUÊTES
//                // ═══════════════════════════════════════════════════════════
//                .authorizeHttpRequests(authorize -> authorize
//
//                        // ───────────────────────────────────────────────────────
//                        // ROUTES PUBLIQUES (Pas d'authentification requise)
//                        // ───────────────────────────────────────────────────────
//
//                        // Authentification (OTP)
//                        .requestMatchers("/api/v1/auth/**").permitAll()
//
//                        // 🆕 AJOUTE ÇA ICI ↓
//                        // DONNÉES GÉOGRAPHIQUES (temporairement public pour setup initial)
//                        .requestMatchers("/api/v1/pays/**").permitAll()
//                        .requestMatchers("/api/v1/regions/**").permitAll()
//                        .requestMatchers("/api/v1/departements/**").permitAll()
//                        .requestMatchers("/api/v1/communes/**").permitAll()
//                        .requestMatchers("/api/v1/admin/localisation/init-senegal").permitAll()
//
//
//                        // Plans d'abonnement (public pour voir les prix)
//                        .requestMatchers("/api/v1/subscriptions/plans").permitAll()
//
//                        .requestMatchers("/api/v1/subscriptions/plans").permitAll()
//
//                        // Routes publiques API
//                        .requestMatchers("/api/v1/init/**").permitAll()
//
//                        // Documentation API
//                        .requestMatchers(
//                                "/swagger-ui/**",
//                                "/swagger-ui.html",
//                                "/api-docs/**",
//                                "/v3/api-docs/**"
//                        ).permitAll()
//
//                        // H2 Console (dev seulement)
//                        .requestMatchers("/h2-console/**").permitAll()
//
//                        // Actuator health
//                        .requestMatchers("/actuator/health").permitAll()
//
//                        // ───────────────────────────────────────────────────────
//                        // ROUTES PROTÉGÉES PAR RÔLE
//                        // ───────────────────────────────────────────────────────
//
//                        // Admin seulement
//                        .requestMatchers("/api/v1/admin/**")
//                        .permitAll()
////                        .hasAnyRole("ADMIN", "SUPER_ADMIN")
//
//                        // Pharmacien
//                        .requestMatchers("/api/v1/pharmacies/me/**")
//                        .hasRole("PHARMACIEN")
//
//                        // Pharmacien
//                        .requestMatchers("/api/v1/pharmaciens/**")
//                        .permitAll()
//
//                        .requestMatchers("/api/v1/stock/**")
//                        .hasRole("PHARMACIEN")
//
//                        // Patient (authentifié)
//                        .requestMatchers("/api/v1/patients/me/**")
//                        .hasRole("PATIENT")
//
//                        .requestMatchers("/api/v1/subscriptions/me/**")
//                        .hasRole("PATIENT")
//
//                        .requestMatchers("/api/v1/payments/**")
//                        .hasRole("PATIENT")
//
//                        // Syndicat
//                        .requestMatchers("/api/v1/syndicats/**")
//                        .hasRole("SYNDICAT")
//
//                        // ───────────────────────────────────────────────────────
//                        // TOUTES LES AUTRES ROUTES (Authentification requise)
//                        // ───────────────────────────────────────────────────────
//
//                        .anyRequest().authenticated()
//                )
//
//                // ═══════════════════════════════════════════════════════════
//                // JWT FILTER (Avant UsernamePasswordAuthenticationFilter)
//                // ═══════════════════════════════════════════════════════════
//                .addFilterBefore(
//                        jwtAuthenticationFilter,
//                        UsernamePasswordAuthenticationFilter.class
//                );
//
//        // ═══════════════════════════════════════════════════════════
//        // H2 CONSOLE (Permettre iframes pour dev)
//        // ═══════════════════════════════════════════════════════════
//        http.headers(headers -> headers
//                .frameOptions(frameOptions -> frameOptions.sameOrigin())
//        );
//
//        return http.build();
//    }
//}



package sn.sunufarmasi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Spring Security
 * Compatible avec le système OTP (patients) et email/password (users)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Routes publiques (sans authentification)
     */
    private static final String[] PUBLIC_ROUTES = {
            // Auth OTP (patients)
            "/api/v1/auth/**",

            // Auth Users (email/password) - pharmaciens, syndicats, admins
            "/api/v1/users/auth/**",

            // Init (⚠️ À désactiver en production)
            "/api/v1/init/**",

            // Public
            "/api/v1/public/**",

            // Gardes publiques
            "/api/v1/gardes",
            "/api/v1/gardes/**",

            // Pharmacies publiques
            "/api/v1/pharmacies/proximite",
            "/api/v1/pharmacies/garde",
            "/api/v1/pharmaciens/**",

            // Localisation
            "/api/v1/pays/**",
            "/api/v1/regions/**",
            "/api/v1/departements/**",
            "/api/v1/communes/**",

            // Subscriptions plans (public)
            "/api/v1/subscriptions/plans",

            // Swagger / OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",

            // H2 Console (dev)
            "/h2-console/**",

            // Actuator
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Configurer CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Autorisation des requêtes
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques
                        .requestMatchers(PUBLIC_ROUTES).permitAll()

                        // Routes Admin
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Routes Syndicat
                        .requestMatchers("/api/v1/syndicat/**").hasAnyRole("ADMIN", "ADMIN_SYNDICAT")
                        .requestMatchers("/api/v1/syndicats/**").hasAnyRole("ADMIN", "ADMIN_SYNDICAT", "SYNDICAT")

                        // Routes Pharmacie
                        .requestMatchers("/api/v1/pharmacie/**").hasAnyRole("ADMIN", "PHARMACIEN")
                        .requestMatchers("/api/v1/pharmacies/me/**").hasRole("PHARMACIEN")
                        .requestMatchers("/api/v1/stock/**").hasRole("PHARMACIEN")

                        // Routes Patient
                        .requestMatchers("/api/v1/patients/me/**").hasRole("PATIENT")
                        .requestMatchers("/api/v1/subscriptions/me/**").hasRole("PATIENT")
                        .requestMatchers("/api/v1/payments/**").hasRole("PATIENT")

                        // Toutes les autres routes nécessitent une authentification
                        .anyRequest().authenticated()
                )

                // Session stateless (JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Ajouter le filtre JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // H2 Console (iframes)
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
        );

        return http.build();
    }

    /**
     * Configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:4200",
                "http://localhost:5173",
                "http://localhost:8080",
                "http://localhost:8100",
                "https://sunufarmasi.sn",
                "https://app.sunufarmasi.sn"
        ));

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count", "X-Page-Number", "X-Page-Size"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Encodeur BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}