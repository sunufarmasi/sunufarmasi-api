package sn.sunufarmasi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sn.sunufarmasi.shared.constant.AppConstants;

import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour la documentation interactive de l'API
 * Documentation accessible à: http://localhost:8080/swagger-ui.html
 * 
 * @author WeCan
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {
    
    /**
     * Configuration de l'API OpenAPI
     * 
     * @return OpenAPI configuré
     */
    @Bean
    public OpenAPI pharmaGoOpenAPI() {
        // ═══════════════════════════════════════════════════════════
        // INFORMATIONS API
        // ═══════════════════════════════════════════════════════════
        
        Info apiInfo = new Info()
            .title(AppConstants.APP_NAME + " API")
            .description(AppConstants.APP_DESCRIPTION)
            .version(AppConstants.APP_VERSION_NUMBER)
            .contact(new Contact()
                .name("PharmaGo Support")
                .email(AppConstants.SUPPORT_EMAIL)
                .url("https://pharmago.sn"))
            .license(new License()
                .name("Propriétaire")
                .url("https://pharmago.sn/license"));
        
        // ═══════════════════════════════════════════════════════════
        // SERVEURS
        // ═══════════════════════════════════════════════════════════
        
        Server devServer = new Server()
            .url("http://localhost:8080")
            .description("Serveur de développement");
        
        Server prodServer = new Server()
            .url("https://api.pharmago.sn")
            .description("Serveur de production");
        
        // ═══════════════════════════════════════════════════════════
        // SÉCURITÉ JWT
        // ═══════════════════════════════════════════════════════════
        
        String securitySchemeName = "bearerAuth";
        
        SecurityScheme securityScheme = new SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .description("Token JWT obtenu via /api/v1/auth/login");
        
        SecurityRequirement securityRequirement = new SecurityRequirement()
            .addList(securitySchemeName);
        
        // ═══════════════════════════════════════════════════════════
        // CONFIGURATION FINALE
        // ═══════════════════════════════════════════════════════════
        
        return new OpenAPI()
            .info(apiInfo)
            .servers(List.of(devServer, prodServer))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, securityScheme))
            .addSecurityItem(securityRequirement);
    }
}
