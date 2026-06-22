package sn.sunufarmasi.ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sn.sunufarmasi.ticket.enums.PrioriteTicket;
import sn.sunufarmasi.ticket.enums.TicketStatut;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_syndicat", columnList = "syndicat_id"),
        @Index(name = "idx_ticket_statut", columnList = "statut"),
        @Index(name = "idx_ticket_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "syndicat_id", nullable = false)
    private UUID syndicatId;

    @Column(name = "syndicat_nom", nullable = false, length = 200)
    private String syndicatNom;

    @Column(name = "syndicat_code", nullable = false, length = 30)
    private String syndicatCode;

    @Column(nullable = false, length = 200)
    private String sujet;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatut statut = TicketStatut.OUVERT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrioriteTicket priorite = PrioriteTicket.NORMALE;

    @Column(name = "categorie", length = 100)
    private String categorie;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(name = "repondu_par", length = 200)
    private String reponduPar;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    @Column(name = "piece_jointe_nom", length = 255)
    private String pieceJointeNom;

    @Column(name = "piece_jointe_url", length = 500)
    private String pieceJointeUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
