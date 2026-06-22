package sn.sunufarmasi.messaging.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller WebSocket pour la messagerie temps réel
 *
 * Topics disponibles :
 * - /topic/chat/{roomId}      → Messages d'une salle
 * - /topic/gardes             → Notifications gardes publiées
 * - /user/queue/notifications → Notifications privées
 *
 * @author WeCan
 * @since 1.0.0
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final SimpMessageSendingOperations messagingTemplate;

    // Historique en mémoire (MVP - max 100 messages/salle)
    private final Map<String, List<ChatMessage>> chatHistory = new ConcurrentHashMap<>();

    /**
     * Envoyer un message dans une salle
     * Client → /app/chat/{roomId} → /topic/chat/{roomId}
     */
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        String senderId = principal != null ? principal.getName() : "anonymous";
        ChatMessage finalMsg = new ChatMessage(
                message.roomId(), senderId, message.senderName(),
                message.content(), LocalDateTime.now().toString(), "CHAT"
        );

        log.info("WS - Message salle {}: {}", finalMsg.roomId(), finalMsg.senderName());

        chatHistory.computeIfAbsent(finalMsg.roomId(), k -> new ArrayList<>());
        List<ChatMessage> room = chatHistory.get(finalMsg.roomId());
        room.add(finalMsg);
        if (room.size() > 100) room.remove(0);

        messagingTemplate.convertAndSend("/topic/chat/" + finalMsg.roomId(), finalMsg);
    }

    /**
     * Notification garde publiée
     * Client → /app/gardes/notification → /topic/gardes
     */
    @MessageMapping("/gardes/notification")
    public void notifierGarde(@Payload GardeNotification notification, Principal principal) {
        log.info("WS - Notification garde par: {}", principal != null ? principal.getName() : "anon");
        messagingTemplate.convertAndSend("/topic/gardes", notification);
    }

    /**
     * Notification privée à un utilisateur
     */
    public void sendPrivateNotification(String userId, Object notification) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    public Map<String, List<ChatMessage>> getChatHistory() {
        return chatHistory;
    }

    public record ChatMessage(
            String roomId,
            String senderId,
            String senderName,
            String content,
            String timestamp,
            String type
    ) {}

    public record GardeNotification(
            String syndicatId,
            String syndicatNom,
            String pharmacieNom,
            String dateGarde,
            String message
    ) {}
}
