package sn.sunufarmasi.pharmacie.dto.response;

import sn.sunufarmasi.pharmacie.enums.PlanAbonnementPharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO Response pour un pharmacien
 *
 * @author WeCan
 * @since 1.0.0
 */
public record PharmacienResponse(
        UUID id,
        String nom,
        String prenom,
        String nomComplet,
        String email,
        String telephone,
        LocalDate dateNaissance,
        String sexe,
        String photoUrl,

        // Diplôme
        String numeroOrdreNational,
        String universiteFormation,
        Integer anneeDiplome,

        // Type
        TypePharmacien type,

        // Abonnement
        PlanAbonnementPharmacie plan,
        StatutAbonnement statutAbonnement,
        BigDecimal montantMensuel,
        Boolean essaiGratuit,
        LocalDate dateFinEssai,
        LocalDate dateFinAbonnement,
        Integer nombrePharmaciesMax,
        Integer nombrePharmaciesActuelles,
        Integer nombreEmployesMax,

        // Statut
        Boolean actif,
        Boolean valideParOrdre,
        Boolean compteVerifie,

        // Métadonnées
        LocalDateTime createdAt,
        LocalDateTime derniereConnexion
) {
    /**
     * Vérifie si l'abonnement est actif
     */
    public boolean isAbonnementActif() {
        if (essaiGratuit != null && essaiGratuit && dateFinEssai != null) {
            return dateFinEssai.isAfter(LocalDate.now());
        }
        return statutAbonnement == StatutAbonnement.ACTIF
                && dateFinAbonnement != null
                && dateFinAbonnement.isAfter(LocalDate.now());
    }

    /**
     * Vérifie si peut créer une pharmacie
     */
    public boolean canCreatePharmacie() {
        if (plan == PlanAbonnementPharmacie.ENTERPRISE) {
            return true;
        }
        return (nombrePharmaciesActuelles == null ? 0 : nombrePharmaciesActuelles)
                < (nombrePharmaciesMax == null ? 1 : nombrePharmaciesMax);
    }
}