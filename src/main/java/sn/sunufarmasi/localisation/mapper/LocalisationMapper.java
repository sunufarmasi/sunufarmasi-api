package sn.sunufarmasi.localisation.mapper;

import org.springframework.stereotype.Component;
import sn.sunufarmasi.localisation.dto.response.*;
import sn.sunufarmasi.localisation.entity.*;

/**
 * Mapper pour les entités de localisation
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
public class LocalisationMapper {

    // ═══════════════════════════════════════════════════════════
    // PAYS
    // ═══════════════════════════════════════════════════════════

    public PaysResponse toResponse(Pays pays) {
        if (pays == null) return null;
        
        return new PaysResponse(
            pays.getId(),
            pays.getCode(),
            pays.getCodeIso2(),
            pays.getCodeIso3(),
            pays.getNom(),
            pays.getNomEn(),
            pays.getCapitale(),
            pays.getIndicatifTelephonique(),
            pays.getDevise(),
            pays.getFuseauHoraire(),
            pays.getDrapeau(),
            pays.getActif(),
            pays.getNombreRegions()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // RÉGION
    // ═══════════════════════════════════════════════════════════

    public RegionResponse toResponse(Region region) {
        if (region == null) return null;
        
        Pays pays = region.getPays();
        
        return new RegionResponse(
            region.getId(),
            region.getCode(),
            region.getNom(),
            region.getChefLieu(),
            region.getPopulation(),
            region.getSuperficie(),
            region.getLatitude(),
            region.getLongitude(),
            region.getOrdre(),
            region.getActif(),
            pays != null ? pays.getId() : null,
            pays != null ? pays.getNom() : null,
            pays != null ? pays.getCode() : null,
            region.getNombreDepartements(),
            region.getNombreCommunes()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // DÉPARTEMENT
    // ═══════════════════════════════════════════════════════════

    public DepartementResponse toResponse(Departement departement) {
        if (departement == null) return null;
        
        Region region = departement.getRegion();
        Pays pays = departement.getPays();
        
        return new DepartementResponse(
            departement.getId(),
            departement.getCode(),
            departement.getNom(),
            departement.getChefLieu(),
            departement.getPopulation(),
            departement.getSuperficie(),
            departement.getLatitude(),
            departement.getLongitude(),
            departement.getOrdre(),
            departement.getActif(),
            region != null ? region.getId() : null,
            region != null ? region.getNom() : null,
            region != null ? region.getCode() : null,
            pays != null ? pays.getId() : null,
            pays != null ? pays.getNom() : null,
            departement.getNombreCommunes()
        );
    }

    // ═══════════════════════════════════════════════════════════
    // COMMUNE
    // ═══════════════════════════════════════════════════════════

    public CommuneResponse toResponse(Commune commune) {
        if (commune == null) return null;
        
        Departement departement = commune.getDepartement();
        Region region = commune.getRegion();
        Pays pays = commune.getPays();
        
        return new CommuneResponse(
            commune.getId(),
            commune.getCode(),
            commune.getNom(),
            commune.getType(),
            commune.getType() != null ? commune.getType().getLibelle() : null,
            commune.getCodePostal(),
            commune.getPopulation(),
            commune.getSuperficie(),
            commune.getLatitude(),
            commune.getLongitude(),
            commune.getAltitude(),
            commune.getOrdre(),
            commune.getActif(),
            commune.getZoneUrbaine(),
            commune.getArrondissement(),
            departement != null ? departement.getId() : null,
            departement != null ? departement.getNom() : null,
            departement != null ? departement.getCode() : null,
            region != null ? region.getId() : null,
            region != null ? region.getNom() : null,
            pays != null ? pays.getId() : null,
            pays != null ? pays.getNom() : null
        );
    }

    /**
     * Mapper Commune avec distance
     */
    public CommuneResponse toResponse(Commune commune, Double userLat, Double userLng) {
        CommuneResponse response = toResponse(commune);
        if (response == null || userLat == null || userLng == null) {
            return response;
        }
        
        Double distance = commune.distanceFrom(userLat, userLng);
        return response.withDistance(distance);
    }
}
