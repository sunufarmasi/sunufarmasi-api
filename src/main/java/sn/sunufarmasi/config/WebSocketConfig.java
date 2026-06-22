package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import sn.sunufarmasi.security.JwtTokenProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration WebSocket avec STOMP pour discussion temps réel
 * - Authentification JWT sur la connexion WebSocket
 * - Broker en mémoire (pour MVP - remplacer par Redis en prod)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Broker en mémoire pour les topics et queues
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés au serveur
        config.setApplicationDestinationPrefixes("/app");
        // Préfixe pour les messages directs à un utilisateur
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Intercepteur pour valider le JWT sur la connexion WebSocket
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class
                );

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        if (jwtTokenProvider.validateToken(token)) {
                            String userId = jwtTokenProvider.getUsernameFromToken(token);
                            List<String> roles = jwtTokenProvider.getRolesFromToken(token);

                            List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(role -> new SimpleGrantedAuthority(
                                            role.startsWith("ROLE_") ? role : "ROLE_" + role))
                                    .collect(Collectors.toList());

                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

                            accessor.setUser(auth);
                        }
                    }
                }

                return message;
            }
        });
    }
}
