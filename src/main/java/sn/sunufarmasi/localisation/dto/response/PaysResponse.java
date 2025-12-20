package sn.sunufarmasi.localisation.dto.response;

import java.util.UUID;

/**
 * DTO Response Pays
 */
public record PaysResponse(
    UUID id,
    String code,
    String codeIso2,
    String codeIso3,
    String nom,
    String nomEn,
    String capitale,
    String indicatifTelephonique,
    String devise,
    String fuseauHoraire,
    String drapeau,
    Boolean actif,
    Integer nombreRegions
) {}
