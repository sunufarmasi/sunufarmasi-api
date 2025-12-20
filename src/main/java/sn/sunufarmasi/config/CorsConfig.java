//package sn.sunufarmasi.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
///**
// * Configuration CORS (Cross-Origin Resource Sharing)
// * Permet aux applications frontend d'accéder à l'API depuis différents domaines
// *
// * @author WeCan
// * @since 1.0.0
// */
//@Configuration
//public class CorsConfig {
//
//    /**
//     * Configuration CORS pour l'application
//     *
//     * @return CorsConfigurationSource
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // ═══════════════════════════════════════════════════════════
//        // ORIGINES AUTORISÉES
//        // ═══════════════════════════════════════════════════════════
//
//        // Développement local
//        configuration.setAllowedOrigins(Arrays.asList(
//            "http://localhost:3000",           // React dev
//            "http://localhost:4200",           // Angular dev
//            "http://localhost:8080",           // Vue dev
//            "http://localhost:5173",           // Vite dev
//            "http://127.0.0.1:3000",
//            "http://127.0.0.1:4200",
//            "http://127.0.0.1:8080",
//            "http://127.0.0.1:5173"
//        ));
//
//        // Production (à configurer via application.yml)
//        // configuration.addAllowedOrigin("https://pharmago.sn");
//        // configuration.addAllowedOrigin("https://app.pharmago.sn");
//        // configuration.addAllowedOrigin("https://admin.pharmago.sn");
//
//        // Permettre toutes les origines (UNIQUEMENT EN DEV - DANGEREUX EN PROD)
//        // configuration.addAllowedOriginPattern("*");
//
//        // ═══════════════════════════════════════════════════════════
//        // MÉTHODES HTTP AUTORISÉES
//        // ═══════════════════════════════════════════════════════════
//
//        configuration.setAllowedMethods(Arrays.asList(
//            "GET",
//            "POST",
//            "PUT",
//            "PATCH",
//            "DELETE",
//            "OPTIONS"
//        ));
//
//        // ═══════════════════════════════════════════════════════════
//        // HEADERS AUTORISÉS
//        // ═══════════════════════════════════════════════════════════
//
//        configuration.setAllowedHeaders(Arrays.asList(
//            "Authorization",
//            "Content-Type",
//            "Accept",
//            "X-Requested-With",
//            "X-Request-ID",
//            "Cache-Control",
//            "Origin"
//        ));
//
//        // ═══════════════════════════════════════════════════════════
//        // HEADERS EXPOSÉS (visibles côté client)
//        // ═══════════════════════════════════════════════════════════
//
//        configuration.setExposedHeaders(Arrays.asList(
//            "Authorization",
//            "Content-Disposition",
//            "X-Total-Count",
//            "X-Total-Pages"
//        ));
//
//        // ═══════════════════════════════════════════════════════════
//        // CREDENTIALS
//        // ═══════════════════════════════════════════════════════════
//
//        // Autoriser l'envoi de cookies et credentials
//        configuration.setAllowCredentials(true);
//
//        // ═══════════════════════════════════════════════════════════
//        // CACHE
//        // ═══════════════════════════════════════════════════════════
//
//        // Durée du cache de la configuration CORS (en secondes)
//        configuration.setMaxAge(3600L); // 1 heure
//
//        // ═══════════════════════════════════════════════════════════
//        // APPLIQUER LA CONFIGURATION
//        // ═══════════════════════════════════════════════════════════
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//
//        // Appliquer à toutes les routes
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }
//}
