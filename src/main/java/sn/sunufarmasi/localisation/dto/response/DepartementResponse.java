package sn.sunufarmasi.localisation.dto.response;

import java.util.UUID;

/**
 * DTO Response Département
 */
public record DepartementResponse(
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
    
    // Région
    UUID regionId,
    String regionNom,
    String regionCode,
    
    // Pays
    UUID paysId,
    String paysNom,
    
    // Stats
    Integer nombreCommunes
) {}
