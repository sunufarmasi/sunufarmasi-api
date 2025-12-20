package sn.sunufarmasi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration Web MVC
 * Configuration des converters, formatters, etc.
 * 
 * @author AL Amine
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Configuration de l'ObjectMapper Jackson pour la sérialisation/désérialisation JSON
     * 
     * @return ObjectMapper configuré
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // ═══════════════════════════════════════════════════════════
        // MODULES
        // ═══════════════════════════════════════════════════════════
        
        // Support Java 8 Date/Time (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        // ═══════════════════════════════════════════════════════════
        // SÉRIALISATION
        // ═══════════════════════════════════════════════════════════
        
        // Ne pas écrire les dates comme timestamps (utiliser ISO-8601)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Ne pas échouer sur propriétés vides (beans sans getters)
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Indenter le JSON pour plus de lisibilité (dev seulement, désactiver en prod)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // ═══════════════════════════════════════════════════════════
        // DÉSÉRIALISATION
        // ═══════════════════════════════════════════════════════════
        
        // Ne pas échouer sur propriétés inconnues (fields non mappés)
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // Accepter les valeurs vides comme null
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        
        // Accepter les nombres comme strings et vice-versa
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        return mapper;
    }
    
    /**
     * Configuration des Message Converters
     * Utilise l'ObjectMapper personnalisé
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Converter JSON avec ObjectMapper personnalisé
        MappingJackson2HttpMessageConverter jsonConverter = 
            new MappingJackson2HttpMessageConverter(objectMapper());
        
        converters.add(jsonConverter);
    }
}
