package sn.sunufarmasi.syndicat.dto.request;

import java.time.LocalDate;

/**
 * Requête pour enregistrer un paiement manuel de syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
public record EnregistrerPaiementRequest(
        LocalDate datePaiement,   // Date du paiement reçu
        String reference,          // Référence de paiement (ex: Wave, OM)
        String notes               // Notes optionnelles
) {}
