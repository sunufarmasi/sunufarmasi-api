package sn.sunufarmasi.mutuelle.dto.request;

import jakarta.validation.constraints.*;
import sn.sunufarmasi.mutuelle.enums.TypeCouverture;
import sn.sunufarmasi.mutuelle.enums.TypeMutuelle;

import java.math.BigDecimal;

/**
 * DTO pour créer une mutuelle
 */
public record CreateMutuelleRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200) String nom,

        @Size(max = 50) String nomCourt,

        @NotNull(message = "Le type est obligatoire")
        TypeMutuelle type,

        @Size(max = 500) String adresse,
        @Size(max = 100) String ville,
        String telephone,
        String telephoneUrgence,
        @Email String email,
        String siteWeb,

        String ninea,
        String numeroAgrement,

        String contactNom,
        String contactTelephone,
        @Email String contactEmail,
        String contactPoste,

        TypeCouverture typeCouverture,
        @DecimalMin("0") @DecimalMax("100") BigDecimal tauxCouvertureDefaut,
        @DecimalMin("0") BigDecimal plafondAnnuel,
        @DecimalMin("0") BigDecimal plafondParActe,
        @DecimalMin("0") BigDecimal franchise,
        @Min(0) Integer delaiCarenceJours,

        @Min(0) Integer delaiPaiementJours,
        String modePaiement,
        @Min(1) @Max(31) Integer jourPaiement,

        Boolean exigeOrdonnance,
        Boolean exigeCarteAdherent,
        Boolean exigeFactureDetaillee,
        @Min(0) Integer delaiSoumissionJours,

        Boolean couvreMedicaments,
        Boolean couvreGeneriques,
        Boolean couvreParamedical,
        Boolean couvreCosmetique,
        @Size(max = 2000) String listeExclusions,

        @Size(max = 1000) String notes,
        String logoUrl

) {}
