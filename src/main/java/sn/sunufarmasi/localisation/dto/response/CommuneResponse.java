package sn.sunufarmasi.localisation.dto.response;

import sn.sunufarmasi.localisation.enums.TypeCommune;

import java.util.UUID;

/**
 * DTO Response Commune
 */
public record CommuneResponse(
    UUID id,
    String code,
    String nom,
    TypeCommune type,
    String typeLibelle,
    String codePostal,
    Long population,
    Double superficie,
    Double latitude,
    Double longitude,
    Integer altitude,
    Integer ordre,
    Boolean actif,
    Boolean zoneUrbaine,
    String arrondissement,
    
    // Département
    UUID departementId,
    String departementNom,
    String departementCode,
    
    // Région
    UUID regionId,
    String regionNom,
    
    // Pays
    UUID paysId,
    String paysNom,
    
    // Distance (optionnel, pour recherche proximité)
    Double distanceKm
) {
    /**
     * Constructeur sans distance
     */
    public CommuneResponse(
        UUID id, String code, String nom, TypeCommune type, String typeLibelle,
        String codePostal, Long population, Double superficie,
        Double latitude, Double longitude, Integer altitude, Integer ordre,
        Boolean actif, Boolean zoneUrbaine, String arrondissement,
        UUID departementId, String departementNom, String departementCode,
        UUID regionId, String regionNom, UUID paysId, String paysNom
    ) {
        this(id, code, nom, type, typeLibelle, codePostal, population, superficie,
             latitude, longitude, altitude, ordre, actif, zoneUrbaine, arrondissement,
             departementId, departementNom, departementCode, regionId, regionNom,
             paysId, paysNom, null);
    }

    /**
     * Ajouter la distance
     */
    public CommuneResponse withDistance(Double distance) {
        return new CommuneResponse(
            id, code, nom, type, typeLibelle, codePostal, population, superficie,
            latitude, longitude, altitude, ordre, actif, zoneUrbaine, arrondissement,
            departementId, departementNom, departementCode, regionId, regionNom,
            paysId, paysNom, distance
        );
    }
}
