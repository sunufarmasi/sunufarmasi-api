package sn.sunufarmasi.pharmacie.mapper;

import org.springframework.stereotype.Component;
import sn.sunufarmasi.pharmacie.dto.response.PharmacienResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;

/**
 * Mapper pour convertir Pharmacien entity vers DTOs
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
public class PharmacienMapper {

    /**
     * Convertir Pharmacien → PharmacienResponse
     */
    public PharmacienResponse toResponse(Pharmacien pharmacien) {
        if (pharmacien == null) {
            return null;
        }

        return new PharmacienResponse(
                pharmacien.getId(),
                pharmacien.getNom(),
                pharmacien.getPrenom(),
                pharmacien.getNomComplet(),
                pharmacien.getEmail(),
                pharmacien.getTelephone(),
                pharmacien.getDateNaissance(),
                pharmacien.getSexe(),
                pharmacien.getPhotoUrl(),

                // Diplôme
                pharmacien.getNumeroOrdreNational(),
                pharmacien.getUniversiteFormation(),
                pharmacien.getAnneeDiplome(),

                // Type
                pharmacien.getType(),

                // Abonnement
                pharmacien.getPlan(),
                pharmacien.getStatutAbonnement(),
                pharmacien.getMontantMensuel(),
                pharmacien.getEssaiGratuit(),
                pharmacien.getDateFinEssai(),
                pharmacien.getDateFinAbonnement(),
                pharmacien.getNombrePharmaciesMax(),
                pharmacien.getNombrePharmaciesActuelles(),
                pharmacien.getNombreEmployesMax(),

                // Statut
                pharmacien.getActif(),
                pharmacien.getValideParOrdre(),
                pharmacien.getCompteVerifie(),

                // Métadonnées
                pharmacien.getCreatedAt(),
                pharmacien.getDerniereConnexion()
        );
    }
}