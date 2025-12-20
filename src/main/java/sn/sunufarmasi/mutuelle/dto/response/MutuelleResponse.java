package sn.sunufarmasi.mutuelle.dto.response;

import sn.sunufarmasi.mutuelle.enums.TypeCouverture;
import sn.sunufarmasi.mutuelle.enums.TypeMutuelle;

import java.math.BigDecimal;
import java.util.UUID;

public record MutuelleResponse(
        UUID id,
        String code,
        String nom,
        String nomCourt,
        String nomAffiche,
        TypeMutuelle type,
        String typeLibelle,

        String adresse,
        String ville,
        String telephone,
        String email,

        String contactNom,
        String contactTelephone,

        TypeCouverture typeCouverture,
        BigDecimal tauxCouvertureDefaut,
        BigDecimal plafondAnnuel,
        BigDecimal franchise,

        Integer delaiPaiementJours,

        Boolean exigeOrdonnance,
        Boolean exigeCarteAdherent,

        Boolean couvreMedicaments,
        Boolean couvreGeneriques,
        Boolean couvreParamedical,

        Boolean estActif,
        String logoUrl
) {}
