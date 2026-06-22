package sn.sunufarmasi.ticket.dto.response;

import sn.sunufarmasi.ticket.entity.Ticket;
import sn.sunufarmasi.ticket.enums.PrioriteTicket;
import sn.sunufarmasi.ticket.enums.TicketStatut;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID syndicatId,
        String syndicatNom,
        String syndicatCode,
        String sujet,
        String message,
        TicketStatut statut,
        PrioriteTicket priorite,
        String categorie,
        String reponse,
        String reponduPar,
        LocalDateTime dateReponse,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String pieceJointeNom,
        String pieceJointeUrl
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSyndicatId(),
                ticket.getSyndicatNom(),
                ticket.getSyndicatCode(),
                ticket.getSujet(),
                ticket.getMessage(),
                ticket.getStatut(),
                ticket.getPriorite(),
                ticket.getCategorie(),
                ticket.getReponse(),
                ticket.getReponduPar(),
                ticket.getDateReponse(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getPieceJointeNom(),
                ticket.getPieceJointeUrl()
        );
    }
}
