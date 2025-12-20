package sn.sunufarmasi.vente.dto.response;

import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour une vente (version simple)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record VenteResponse(

        UUID id,
        String numero,
        String numeroTicket,

        // Type et statut
        TypeVente type,
        String typeLibelle,
        StatutVente statut,
        String statutLibelle,

        // Client
        UUID clientId,
        String clientNom,
        String clientTelephone,

        // Montants
        BigDecimal montantBrutTTC,
        BigDecimal montantRemise,
        BigDecimal montantNetTTC,
        BigDecimal montantMutuelle,
        BigDecimal montantClient,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,

        // Paiement
        ModePaiement modePaiement,
        String modePaiementLibelle,
        boolean estPayee,

        // Stats
        Integer nombreArticles,

        // Vendeur
        String vendeurNom,

        // Dates
        LocalDateTime dateVente,
        LocalDateTime datePaiement

) {}
