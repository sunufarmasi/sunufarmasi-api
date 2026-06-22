package sn.sunufarmasi.messaging.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoint pour récupérer l'historique d'une salle de chat
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final MessageController messageController;

    /**
     * GET /api/v1/chat/{roomId}/history
     * Récupérer les 100 derniers messages d'une salle
     */
    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<MessageController.ChatMessage>> getHistory(@PathVariable String roomId) {
        List<MessageController.ChatMessage> msgs = messageController.getChatHistory()
                .getOrDefault(roomId, List.of());
        return ResponseEntity.ok(msgs);
    }
}
