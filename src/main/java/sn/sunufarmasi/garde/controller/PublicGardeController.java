//package sn.sunufarmasi.garde.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//import sn.sunufarmasi.garde.entity.Garde;
//import sn.sunufarmasi.garde.repository.GardeRepository;
//import sn.sunufarmasi.pharmacie.entity.Pharmacie;
//
//import java.time.LocalDate;
//import java.util.*;
//
///**
// * Controller PUBLIC pour les gardes - Accessible sans authentification
// *
// * ═══════════════════════════════════════════════════════════════════════════════
// * ENDPOINTS PRINCIPAUX POUR L'APP MOBILE :
// * ═══════════════════════════════════════════════════════════════════════════════
// *
// * 1. GET /api/v1/public/gardes/aujourd-hui
// *    → Pharmacies de garde AUJOURD'HUI (avec filtres commune/département/région)
// *
// * 2. GET /api/v1/public/gardes/region/{regionId}
// *    → Pharmacies de garde aujourd'hui pour une région (Toutes les communes)
// *
// * 3. GET /api/v1/public/gardes/proches?lat=X&lng=Y&rayon=10
// *    → Pharmacies de garde aujourd'hui près d'une position GPS
// *
// * @author WeCan
// * @since 1.0.0
// */
//@RestController
//@RequestMapping("/api/v1/public")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Public - Gardes", description = "Pharmacies de garde - Endpoints publics")
//public class PublicGardeController {
//
//    private final GardeRepository gardeRepository;
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // ENDPOINT PRINCIPAL : GARDES AUJOURD'HUI
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * GET /api/v1/public/gardes/aujourd-hui
//     *
//     * Pharmacies de garde AUJOURD'HUI avec filtres optionnels
//     *
//     * Exemples :
//     * - /gardes/aujourd-hui                          → Toutes les gardes du jour
//     * - /gardes/aujourd-hui?communeId=xxx           → Gardes dans une commune
//     * - /gardes/aujourd-hui?departementId=xxx       → Gardes dans un département
//     * - /gardes/aujourd-hui?regionId=xxx            → Gardes dans une région
//     */
//    @GetMapping("/gardes/aujourd-hui")
//    @Operation(summary = "Pharmacies de garde aujourd'hui")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<Map<String, Object>>> getGardesAujourdHui(
//            @RequestParam(required = false) UUID communeId,
//            @RequestParam(required = false) UUID departementId,
//            @RequestParam(required = false) UUID regionId
//    ) {
//        log.info("📍 GET /public/gardes/aujourd-hui - commune={}, dept={}, region={}",
//                communeId, departementId, regionId);
//
//        LocalDate today = LocalDate.now();
//        List<Garde> gardes;
//
//        // Priorité : commune > département > région > toutes
//        if (communeId != null) {
//            log.info("   → Filtre par COMMUNE: {}", communeId);
//            gardes = gardeRepository.findByDateAndCommune(today, communeId);
//        } else if (departementId != null) {
//            log.info("   → Filtre par DÉPARTEMENT: {}", departementId);
//            gardes = gardeRepository.findByDateAndDepartement(today, departementId);
//        } else if (regionId != null) {
//            log.info("   → Filtre par RÉGION: {}", regionId);
//            gardes = gardeRepository.findByDateAndRegion(today, regionId);
//        } else {
//            log.info("   → TOUTES les gardes du jour");
//            gardes = gardeRepository.findGardesSemaineEnCours(today);
//        }
//
//        List<Map<String, Object>> result = gardes.stream()
//                .map(this::mapGardeToPublic)
//                .toList();
//
//        log.info("✅ {} pharmacie(s) de garde trouvée(s)", result.size());
//        return ResponseEntity.ok(result);
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // GARDES PAR RÉGION (TOUTES LES COMMUNES)
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * GET /api/v1/public/gardes/region/{regionId}
//     *
//     * Pharmacies de garde AUJOURD'HUI pour une région entière
//     * Utilisé quand le patient choisit "Toutes les communes" d'une région
//     */
//    @GetMapping("/gardes/region/{regionId}")
//    @Operation(summary = "Pharmacies de garde aujourd'hui par région")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<Map<String, Object>>> getGardesByRegion(
//            @PathVariable UUID regionId
//    ) {
//        log.info("📍 GET /public/gardes/region/{}", regionId);
//
//        LocalDate today = LocalDate.now();
//        List<Garde> gardes = gardeRepository.findByDateAndRegion(today, regionId);
//
//        List<Map<String, Object>> result = gardes.stream()
//                .map(this::mapGardeToPublic)
//                .toList();
//
//        log.info("✅ {} pharmacie(s) de garde trouvée(s) pour région {}", result.size(), regionId);
//        return ResponseEntity.ok(result);
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // GARDES PAR DÉPARTEMENT
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * GET /api/v1/public/gardes/departement/{departementId}
//     *
//     * Pharmacies de garde AUJOURD'HUI pour un département
//     */
//    @GetMapping("/gardes/departement/{departementId}")
//    @Operation(summary = "Pharmacies de garde aujourd'hui par département")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<Map<String, Object>>> getGardesByDepartement(
//            @PathVariable UUID departementId
//    ) {
//        log.info("📍 GET /public/gardes/departement/{}", departementId);
//
//        LocalDate today = LocalDate.now();
//        List<Garde> gardes = gardeRepository.findByDateAndDepartement(today, departementId);
//
//        List<Map<String, Object>> result = gardes.stream()
//                .map(this::mapGardeToPublic)
//                .toList();
//
//        log.info("✅ {} pharmacie(s) de garde trouvée(s) pour département {}", result.size(), departementId);
//        return ResponseEntity.ok(result);
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // GARDES PAR COMMUNE
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * GET /api/v1/public/gardes/commune/{communeId}
//     *
//     * Pharmacies de garde AUJOURD'HUI pour une commune spécifique
//     */
//    @GetMapping("/gardes/commune/{communeId}")
//    @Operation(summary = "Pharmacies de garde aujourd'hui par commune")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<Map<String, Object>>> getGardesByCommune(
//            @PathVariable UUID communeId
//    ) {
//        log.info("📍 GET /public/gardes/commune/{}", communeId);
//
//        LocalDate today = LocalDate.now();
//        List<Garde> gardes = gardeRepository.findByDateAndCommune(today, communeId);
//
//        List<Map<String, Object>> result = gardes.stream()
//                .map(this::mapGardeToPublic)
//                .toList();
//
//        log.info("✅ {} pharmacie(s) de garde trouvée(s) pour commune {}", result.size(), communeId);
//        return ResponseEntity.ok(result);
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // GARDES PAR GPS (PROXIMITÉ)
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * GET /api/v1/public/gardes/proches?lat=14.6937&lng=-17.4441&rayon=10
//     *
//     * Pharmacies de garde AUJOURD'HUI à proximité d'une position GPS
//     * Triées par distance (la plus proche en premier)
//     */
//    @GetMapping("/gardes/proches")
//    @Operation(summary = "Pharmacies de garde aujourd'hui à proximité (GPS)")
//    @Transactional(readOnly = true)
//    public ResponseEntity<List<Map<String, Object>>> getGardesProches(
//            @RequestParam Double lat,
//            @RequestParam Double lng,
//            @RequestParam(defaultValue = "10.0") Double rayon
//    ) {
//        log.info("📍 GET /public/gardes/proches - lat={}, lng={}, rayon={}km", lat, lng, rayon);
//
//        LocalDate today = LocalDate.now();
//
//        // Récupérer toutes les gardes en cours
//        List<Garde> gardesEnCours = gardeRepository.findGardesSemaineEnCours(today);
//
//        // Filtrer par distance et trier
//        List<Map<String, Object>> result = gardesEnCours.stream()
//                .filter(g -> g.getLatitude() != null && g.getLongitude() != null)
//                .map(g -> {
//                    Map<String, Object> gardeMap = mapGardeToPublic(g);
//                    double distance = calculerDistance(lat, lng, g.getLatitude(), g.getLongitude());
//                    gardeMap.put("distance", Math.round(distance * 100.0) / 100.0);
//                    return gardeMap;
//                })
//                .filter(g -> (Double) g.get("distance") <= rayon)
//                .sorted(Comparator.comparingDouble(g -> (Double) g.get("distance")))
//                .toList();
//
//        log.info("✅ {} pharmacie(s) de garde trouvée(s) dans un rayon de {}km", result.size(), rayon);
//        return ResponseEntity.ok(result);
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // MÉTHODE UTILITAIRE : MAPPER GARDE VERS JSON PUBLIC
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * Convertir une entité Garde en Map pour la réponse JSON
//     * Format compatible avec l'app Flutter (Pharmacy.fromGardeJson)
//     */
//    private Map<String, Object> mapGardeToPublic(Garde garde) {
//        Pharmacie pharmacie = garde.getPharmacie();
//
//        Map<String, Object> result = new LinkedHashMap<>();
//
//        // ═══════════════════════════════════════════════════════════
//        // INFOS GARDE
//        // ═══════════════════════════════════════════════════════════
//        result.put("id", garde.getId().toString());
//        result.put("dateDebut", garde.getDateDebut().toString());
//        result.put("dateFin", garde.getDateFin().toString());
//        result.put("numeroSemaine", garde.getNumeroSemaine());
//        result.put("typeGarde", garde.getTypeGarde() != null ? garde.getTypeGarde().name() : "JOUR_ET_NUIT");
//        result.put("statut", garde.getStatut() != null ? garde.getStatut().name() : "EN_COURS");
//        result.put("zoneNom", garde.getZoneNom());
//
//        // ═══════════════════════════════════════════════════════════
//        // INFOS PHARMACIE (structure attendue par Flutter)
//        // ═══════════════════════════════════════════════════════════
//        Map<String, Object> pharmacieMap = new LinkedHashMap<>();
//        pharmacieMap.put("id", pharmacie.getId().toString());
//        pharmacieMap.put("code", pharmacie.getCode());
//        pharmacieMap.put("nom", pharmacie.getNom());
//        pharmacieMap.put("adresse", pharmacie.getAdresseComplete());
//        pharmacieMap.put("quartier", pharmacie.getQuartier());
//        pharmacieMap.put("telephone", pharmacie.getTelephone());
//        pharmacieMap.put("email", pharmacie.getEmail());
//        pharmacieMap.put("latitude", pharmacie.getLatitude());
//        pharmacieMap.put("longitude", pharmacie.getLongitude());
//
//        // Localisation hiérarchique
//        if (pharmacie.getCommune() != null) {
//            pharmacieMap.put("commune", pharmacie.getCommune().getNom());
//            if (pharmacie.getCommune().getDepartement() != null) {
//                pharmacieMap.put("departement", pharmacie.getCommune().getDepartement().getNom());
//                if (pharmacie.getCommune().getDepartement().getRegion() != null) {
//                    pharmacieMap.put("region", pharmacie.getCommune().getDepartement().getRegion().getNom());
//                }
//            }
//        }
//
//        // Pharmacien responsable
//        if (pharmacie.getPharmacienProprietaire() != null) {
//            pharmacieMap.put("pharmacienResponsable", pharmacie.getPharmacienProprietaire().getNomComplet());
//        }
//
//        result.put("pharmacie", pharmacieMap);
//
//        // ═══════════════════════════════════════════════════════════
//        // CHAMPS DUPLIQUÉS AU NIVEAU RACINE (pour compatibilité Flutter)
//        // ═══════════════════════════════════════════════════════════
//        result.put("commune", pharmacie.getCommune() != null ? pharmacie.getCommune().getNom() : null);
//        result.put("departement", pharmacie.getCommune() != null && pharmacie.getCommune().getDepartement() != null
//                ? pharmacie.getCommune().getDepartement().getNom() : null);
//        result.put("region", pharmacie.getCommune() != null && pharmacie.getCommune().getDepartement() != null
//                && pharmacie.getCommune().getDepartement().getRegion() != null
//                ? pharmacie.getCommune().getDepartement().getRegion().getNom() : null);
//
//        return result;
//    }
//
//    // ═══════════════════════════════════════════════════════════════════════════════
//    // MÉTHODE UTILITAIRE : CALCUL DE DISTANCE GPS
//    // ═══════════════════════════════════════════════════════════════════════════════
//
//    /**
//     * Calculer la distance entre deux points GPS (formule Haversine)
//     * @return distance en kilomètres
//     */
//    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
//        final int R = 6371; // Rayon de la Terre en km
//        double latDistance = Math.toRadians(lat2 - lat1);
//        double lonDistance = Math.toRadians(lon2 - lon1);
//        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
//                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        return R * c;
//    }
//}