package sn.sunufarmasi.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.sunufarmasi.ticket.entity.Ticket;
import sn.sunufarmasi.ticket.enums.TicketStatut;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findBySyndicatIdOrderByCreatedAtDesc(UUID syndicatId);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByStatutOrderByCreatedAtDesc(TicketStatut statut);

    long countBySyndicatId(UUID syndicatId);

    long countByStatut(TicketStatut statut);
}
