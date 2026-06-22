package sn.sunufarmasi.ticket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.ticket.dto.request.CreateTicketRequest;
import sn.sunufarmasi.ticket.dto.request.RepondreTicketRequest;
import sn.sunufarmasi.ticket.dto.response.TicketResponse;
import sn.sunufarmasi.ticket.enums.TicketStatut;
import sn.sunufarmasi.ticket.service.TicketService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    // ═══════════════════════════════════════════════════════════
    // SYNDICAT - Créer et voir ses tickets
    // ═══════════════════════════════════════════════════════════

    @PostMapping
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            Authentication authentication,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        TicketResponse ticket = ticketService.create(syndicatId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket créé avec succès", ticket));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMesTickets(Authentication authentication) {
        UUID syndicatId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(ticketService.getMesTickets(syndicatId)));
    }

    @GetMapping("/me/{id}")
    @PreAuthorize("hasRole('SYNDICAT')")
    public ResponseEntity<ApiResponse<TicketResponse>> getMonTicket(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getById(id)));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN - Voir et répondre à tous les tickets
    // ═══════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getAll()));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getByStatut(@PathVariable TicketStatut statut) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getByStatut(statut)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getById(id)));
    }

    @PostMapping("/{id}/repondre")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> repondre(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody RepondreTicketRequest request
    ) {
        String adminNom = authentication.getName();
        TicketResponse ticket = ticketService.repondre(id, request, adminNom);
        return ResponseEntity.ok(ApiResponse.success("Réponse envoyée", ticket));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> changerStatut(
            @PathVariable UUID id,
            @RequestParam TicketStatut statut
    ) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.changerStatut(id, statut)));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getStats()));
    }
}
