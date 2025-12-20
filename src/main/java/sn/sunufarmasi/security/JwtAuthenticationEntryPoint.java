package sn.sunufarmasi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.shared.dto.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Point d'entrée pour gérer les erreurs d'authentification JWT
 * Retourne une réponse JSON structurée au lieu de la page de login par défaut
 * 
 * @author WeCan
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Gérer les erreurs d'authentification
     * Appelé lorsqu'un utilisateur non authentifié tente d'accéder à une ressource protégée
     * 
     * @param request Requête HTTP
     * @param response Réponse HTTP
     * @param authException Exception d'authentification
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        log.error("Erreur d'authentification: {} - URI: {}", 
                  authException.getMessage(), 
                  request.getRequestURI());
        
        // Déterminer le message d'erreur approprié
        String errorMessage = "Authentification requise";
        String errorCode = "UNAUTHORIZED";
        
        // Vérifier si c'est un problème de token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            errorMessage = "Token d'authentification manquant";
            errorCode = "MISSING_TOKEN";
        } else if (!authHeader.startsWith("Bearer ")) {
            errorMessage = "Format de token invalide. Utilisez: Bearer <token>";
            errorCode = "INVALID_TOKEN_FORMAT";
        } else {
            errorMessage = "Token d'authentification invalide ou expiré";
            errorCode = "INVALID_TOKEN";
        }
        
        // Créer la réponse d'erreur
        ErrorResponse errorResponse = new ErrorResponse(
            errorCode,
            errorMessage,
            LocalDateTime.now(),
            request.getRequestURI()
        );
        
        ApiResponse<Void> apiResponse = ApiResponse.error(
            errorMessage,
            errorResponse
        );
        
        // Configurer la réponse HTTP
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // Écrire la réponse JSON
        response.getWriter().write(
            objectMapper.writeValueAsString(apiResponse)
        );
    }
}
