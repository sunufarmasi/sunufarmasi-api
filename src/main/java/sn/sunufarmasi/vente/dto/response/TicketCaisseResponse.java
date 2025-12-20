package sn.sunufarmasi.vente.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Response pour le ticket de caisse (impression)
 *
 * @author WeCan
 * @since 1.0.0
 */
public record TicketCaisseResponse(

        // En-tête
        String pharmacieNom,
        String pharmacieAdresse,
        String pharmacieTelephone,
        String pharmacieNinea,

        // Numéro
        String numeroTicket,
        String numeroVente,
        LocalDateTime dateVente,

        // Vendeur
        String vendeurNom,

        // Client
        String clientNom,
        String clientTelephone,

        // Lignes
        List<LigneTicket> lignes,

        // Totaux
        BigDecimal sousTotal,
        BigDecimal remise,
        BigDecimal total,
        BigDecimal partMutuelle,
        BigDecimal aPayerClient,
        BigDecimal montantPaye,
        BigDecimal monnaieRendue,

        // Paiement
        String modePaiement,

        // Pied de page
        String messageRemise,
        String messageMerci

) {
    /**
     * Ligne du ticket
     */
    public record LigneTicket(
            String produitNom,
            Integer quantite,
            BigDecimal prixUnitaire,
            BigDecimal remise,
            BigDecimal total
    ) {}

    /**
     * Générer un ticket formaté en texte (pour impression thermique)
     */
    public String toTextFormat(int largeur) {
        StringBuilder sb = new StringBuilder();
        String ligne = "=".repeat(largeur);
        String ligneFine = "-".repeat(largeur);

        // En-tête
        sb.append(centrer(pharmacieNom, largeur)).append("\n");
        sb.append(centrer(pharmacieAdresse, largeur)).append("\n");
        sb.append(centrer("Tél: " + pharmacieTelephone, largeur)).append("\n");
        if (pharmacieNinea != null) {
            sb.append(centrer("NINEA: " + pharmacieNinea, largeur)).append("\n");
        }
        sb.append(ligne).append("\n");

        // Ticket
        sb.append("Ticket: ").append(numeroTicket).append("\n");
        sb.append("Date: ").append(dateVente.toString().replace("T", " ")).append("\n");
        sb.append("Vendeur: ").append(vendeurNom).append("\n");
        if (clientNom != null) {
            sb.append("Client: ").append(clientNom).append("\n");
        }
        sb.append(ligneFine).append("\n");

        // Lignes
        for (LigneTicket l : lignes) {
            sb.append(l.produitNom()).append("\n");
            sb.append(String.format("  %d x %.0f = %.0f FCFA", l.quantite(), l.prixUnitaire(), l.total()));
            if (l.remise() != null && l.remise().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format(" (-%0.f)", l.remise()));
            }
            sb.append("\n");
        }

        sb.append(ligneFine).append("\n");

        // Totaux
        sb.append(formaterLigneMontant("Sous-total", sousTotal, largeur)).append("\n");
        if (remise != null && remise.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formaterLigneMontant("Remise", remise.negate(), largeur)).append("\n");
        }
        sb.append(formaterLigneMontant("TOTAL", total, largeur)).append("\n");

        if (partMutuelle != null && partMutuelle.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formaterLigneMontant("Part mutuelle", partMutuelle, largeur)).append("\n");
            sb.append(formaterLigneMontant("A payer", aPayerClient, largeur)).append("\n");
        }

        sb.append(ligne).append("\n");
        sb.append(formaterLigneMontant("Payé (" + modePaiement + ")", montantPaye, largeur)).append("\n");

        if (monnaieRendue != null && monnaieRendue.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formaterLigneMontant("Monnaie rendue", monnaieRendue, largeur)).append("\n");
        }

        sb.append(ligne).append("\n");

        // Pied de page
        if (messageRemise != null) {
            sb.append(centrer(messageRemise, largeur)).append("\n");
        }
        sb.append(centrer(messageMerci != null ? messageMerci : "Merci de votre visite!", largeur)).append("\n");

        return sb.toString();
    }

    private String centrer(String texte, int largeur) {
        if (texte == null || texte.length() >= largeur) return texte;
        int padding = (largeur - texte.length()) / 2;
        return " ".repeat(padding) + texte;
    }

    private String formaterLigneMontant(String libelle, BigDecimal montant, int largeur) {
        String montantStr = String.format("%.0f FCFA", montant);
        int espaces = largeur - libelle.length() - montantStr.length();
        return libelle + " ".repeat(Math.max(1, espaces)) + montantStr;
    }
}
