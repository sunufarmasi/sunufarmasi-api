package sn.sunufarmasi.localisation.dto.response;

import java.util.UUID;

/**
 * DTO Response Région
 */
public record RegionResponse(
    UUID id,
    String code,
    String nom,
    String chefLieu,
    Long population,
    Double superficie,
    Double latitude,
    Double longitude,
    Integer ordre,
    Boolean actif,
    
    // Pays
    UUID paysId,
    String paysNom,
    String paysCode,
    
    // Stats
    Integer nombreDepartements,
    Integer nombreCommunes
) {}
