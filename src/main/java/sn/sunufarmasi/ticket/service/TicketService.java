package sn.sunufarmasi.ticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.notification.service.EmailService;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;
import sn.sunufarmasi.ticket.dto.request.CreateTicketRequest;
import sn.sunufarmasi.ticket.dto.request.RepondreTicketRequest;
import sn.sunufarmasi.ticket.dto.response.TicketResponse;
import sn.sunufarmasi.ticket.entity.Ticket;
import sn.sunufarmasi.ticket.enums.PrioriteTicket;
import sn.sunufarmasi.ticket.enums.TicketStatut;
import sn.sunufarmasi.ticket.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SyndicatRepository syndicatRepository;
    private final EmailService emailService;

    public TicketResponse create(UUID syndicatId, CreateTicketRequest request) {
        Syndicat syndicat = syndicatRepository.findById(syndicatId)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicat non trouvé: " + syndicatId));

        Ticket ticket = Ticket.builder()
                .syndicatId(syndicatId)
                .syndicatNom(syndicat.getNom())
                .syndicatCode(syndicat.getCode())
                .sujet(request.sujet())
                .message(request.message())
                .statut(TicketStatut.OUVERT)
                .priorite(request.priorite() != null ? request.priorite() : PrioriteTicket.NORMALE)
                .categorie(request.categorie())
                .pieceJointeNom(request.pieceJointeNom())
                .pieceJointeUrl(request.pieceJointeUrl())
                .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket créé: {} par syndicat: {}", saved.getId(), syndicat.getCode());
        return TicketResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getMesTickets(UUID syndicatId) {
        return ticketRepository.findBySyndicatIdOrderByCreatedAtDesc(syndicatId).stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAll() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getByStatut(TicketStatut statut) {
        return ticketRepository.findByStatutOrderByCreatedAtDesc(statut).stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getById(UUID id) {
        return TicketResponse.from(findOrThrow(id));
    }

    public TicketResponse repondre(UUID id, RepondreTicketRequest request, String adminNom) {
        Ticket ticket = findOrThrow(id);
        ticket.setReponse(request.reponse());
        ticket.setReponduPar(adminNom);
        ticket.setDateReponse(LocalDateTime.now());
        ticket.setStatut(request.nouveauStatut() != null ? request.nouveauStatut() : TicketStatut.RESOLU);
        log.info("Ticket répondu: {} par: {}", id, adminNom);
        Ticket saved = ticketRepository.save(ticket);

        // Notifier le syndicat par email
        syndicatRepository.findById(ticket.getSyndicatId()).ifPresent(s -> {
            if (s.getEmail() != null) {
                emailService.notifierReponseTicket(s.getEmail(), s.getNom(),
                        ticket.getSujet(), request.reponse());
            }
        });

        return TicketResponse.from(saved);
    }

    public TicketResponse changerStatut(UUID id, TicketStatut statut) {
        Ticket ticket = findOrThrow(id);
        ticket.setStatut(statut);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Long> getStats() {
        return java.util.Map.of(
                "total", ticketRepository.count(),
                "ouverts", ticketRepository.countByStatut(TicketStatut.OUVERT),
                "enCours", ticketRepository.countByStatut(TicketStatut.EN_COURS),
                "resolus", ticketRepository.countByStatut(TicketStatut.RESOLU),
                "fermes", ticketRepository.countByStatut(TicketStatut.FERME)
        );
    }

    private Ticket findOrThrow(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket non trouvé: " + id));
    }
}
