package sn.sunufarmasi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import sn.sunufarmasi.config.JwtConfig;
import sn.sunufarmasi.patient.repository.PatientRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtre d'authentification JWT
 * Intercepte toutes les requêtes HTTP et valide le token JWT si présent
 *
 * SYSTÈME OTP :
 * - Pas besoin de UserDetailsService
 * - Le JWT contient : userId + roles
 * - On crée l'authentification directement depuis le JWT
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtConfig jwtConfig;
    private final PatientRepository patientRepository;

    /**
     * Filtrer chaque requête pour valider le token JWT
     *
     * @param request Requête HTTP
     * @param response Réponse HTTP
     * @param filterChain Chaîne de filtres
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extraire le token JWT de la requête
            String jwt = extractJwtFromRequest(request);

            // 2. Si token présent et valide
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // 3. Extraire l'userId (subject) du token
                String userId = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. Extraire les rôles du token
                List<String> roles = jwtTokenProvider.getRolesFromToken(jwt);

                // 5. Créer les authorities Spring Security
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> {
                            // Ajouter ROLE_ seulement si pas déjà présent
                            if (role.startsWith("ROLE_")) {
                                return new SimpleGrantedAuthority(role);
                            }
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        })
                        .collect(Collectors.toList());

                // 6. Si le token appartient à un PATIENT, vérifier que le compte est actif
                boolean isPatient = roles.stream().anyMatch(r ->
                        r.equals("PATIENT") || r.equals("ROLE_PATIENT"));
                if (isPatient) {
                    try {
                        UUID patientUuid = UUID.fromString(userId);
                        boolean actif = patientRepository.findById(patientUuid)
                                .map(p -> p.isActif())
                                .orElse(false);
                        if (!actif) {
                            log.warn("🚫 Patient {} suspendu — requête bloquée: {}", userId, request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                "{\"success\":false,\"message\":\"Votre compte a été désactivé. Contactez le support.\"}"
                            );
                            return;
                        }
                    } catch (Exception e) {
                        log.error("Erreur vérification actif patient {}: {}", userId, e.getMessage());
                    }
                }

                // 7. Créer l'authentification
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,  // Principal = userId
                                null,    // Credentials = null (pas de password)
                                authorities
                        );

                // 8. Ajouter les détails de la requête
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Définir l'authentification dans le contexte de sécurité
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("✅ Utilisateur authentifié: {} - Rôles: {} - URI: {}",
                        userId, roles, request.getRequestURI());
            }

        } catch (Exception ex) {
            log.error("❌ Impossible de définir l'authentification utilisateur: {}",
                    ex.getMessage());
            // Ne pas bloquer la requête, laisser Spring Security gérer
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Extraire le token JWT du header Authorization
     *
     * @param request Requête HTTP
     * @return Token JWT ou null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // Utilise getHeader() de JwtConfig
        String bearerToken = request.getHeader(jwtConfig.getHeader());

        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith(jwtConfig.getTokenPrefix())) {
            // Enlever "Bearer " du début
            return bearerToken.substring(jwtConfig.getTokenPrefix().length()).trim();
        }

        return null;
    }

    /**
     * Ne pas filtrer certaines routes (endpoints publics)
     *
     * @param request Requête HTTP
     * @return true si doit être ignoré
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Routes publiques (pas besoin d'authentification)
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/public/") ||
                path.startsWith("/api/v1/users/auth/") ||      // ← AJOUTER CETTE LIGNE
                path.startsWith("/api/v1/init/") ||            // ← AJOUTER CETTE LIGNE
                path.startsWith("/api/v1/subscriptions/plans") ||  // Plans publics
                path.startsWith("/api/v1/gardes") ||               // Gardes publiques
                path.startsWith("/api/v1/pharmacies/proximite") || // Recherche proximité
                path.startsWith("/api/v1/pharmacies/garde") ||     // Pharmacies de garde
                path.startsWith("/h2-console") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator/health");
    }
}