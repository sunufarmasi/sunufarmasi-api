package sn.sunufarmasi.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.shared.dto.ApiResponse;

import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepository;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.garde.entity.Garde;
import sn.sunufarmasi.garde.repository.GardeRepository;
import sn.sunufarmasi.localisation.repository.CommuneRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.ArrayList;

/**
 * API Publique pour l'application mobile SunuFarmasi
 * Endpoints accessibles sans authentification
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Transactional(readOnly = true)
@Tag(name = "API Publique", description = "Endpoints accessibles sans authentification")
public class PublicApiController {

    private final PharmacieRepository pharmacieRepository;
    private final GardeRepository gardeRepository;
    private final CommuneRepository communeRepository;
    private final sn.sunufarmasi.syndicat.repository.SyndicatRepository syndicatRepository;

    // ═══════════════════════════════════════════════════════════════════════════════
    // PHARMACIES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/public/pharmacies
     * Pharmacies avec filtres multiples (commune, département, région)
     *
     * Exemples:
     * - /pharmacies?communeId=xxx         → Pharmacies d'une commune
     * - /pharmacies?departementId=xxx     → Pharmacies d'un département
     * - /pharmacies?regionId=xxx          → Pharmacies d'une région
     * - /pharmacies?search=xxx            → Recherche textuelle
     */
    @GetMapping("/pharmacies")
    @Operation(summary = "Pharmacies avec filtres (commune, département, région)")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPharmacies(
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) String search) {

        log.info("GET /api/v1/public/pharmacies - commune={}, dept={}, region={}, search={}",
                communeId, departementId, regionId, search);

        List<Pharmacie> pharmacies;

        if (communeId != null) {
            // Priorité 1: Par commune
            pharmacies = pharmacieRepository.findByCommuneIdAndStatut(communeId, StatutPharmacie.ACTIVE);
            log.info("Filtre par commune {} → {} pharmacies", communeId, pharmacies.size());

        } else if (departementId != null) {
            // Priorité 2: Par département
            pharmacies = pharmacieRepository.findByDepartementAndStatut(departementId, StatutPharmacie.ACTIVE);
            log.info("Filtre par département {} → {} pharmacies", departementId, pharmacies.size());

        } else if (regionId != null) {
            // Priorité 3: Par région
            pharmacies = pharmacieRepository.findByRegionAndStatut(regionId, StatutPharmacie.ACTIVE);
            log.info("Filtre par région {} → {} pharmacies", regionId, pharmacies.size());

        } else if (search != null && !search.isBlank()) {
            // Priorité 4: Recherche textuelle
            pharmacies = pharmacieRepository.search(search);
            log.info("Recherche '{}' → {} pharmacies", search, pharmacies.size());

        } else {
            // Par défaut: Limiter à 50 pharmacies
            pharmacies = pharmacieRepository.findByStatut(StatutPharmacie.ACTIVE,
                    org.springframework.data.domain.PageRequest.of(0, 50)).getContent();
            log.info("Sans filtre → {} pharmacies (limité à 50)", pharmacies.size());
        }

        List<Map<String, Object>> result = pharmacies.stream()
                .map(this::mapPharmacieToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Pharmacies", result));
    }

    /**
     * GET /api/v1/public/pharmacies/commune/{communeId}
     * Raccourci pour pharmacies d'une commune
     */
    @GetMapping("/pharmacies/commune/{communeId}")
    @Operation(summary = "Pharmacies d'une commune")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPharmaciesByCommune(
            @PathVariable UUID communeId) {
        log.info("GET /api/v1/public/pharmacies/commune/{}", communeId);

        List<Pharmacie> pharmacies = pharmacieRepository.findByCommuneIdAndStatut(communeId, StatutPharmacie.ACTIVE);

        List<Map<String, Object>> result = pharmacies.stream()
                .map(this::mapPharmacieToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies de la commune (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/pharmacies/departement/{departementId}
     * Pharmacies d'un département
     */
    @GetMapping("/pharmacies/departement/{departementId}")
    @Operation(summary = "Pharmacies d'un département")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPharmaciesByDepartement(
            @PathVariable UUID departementId) {
        log.info("GET /api/v1/public/pharmacies/departement/{}", departementId);

        List<Pharmacie> pharmacies = pharmacieRepository.findByDepartementAndStatut(departementId, StatutPharmacie.ACTIVE);

        List<Map<String, Object>> result = pharmacies.stream()
                .map(this::mapPharmacieToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies du département (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/pharmacies/region/{regionId}
     * Pharmacies d'une région (toutes les localités)
     */
    @GetMapping("/pharmacies/region/{regionId}")
    @Operation(summary = "Pharmacies d'une région")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPharmaciesByRegion(
            @PathVariable UUID regionId) {
        log.info("GET /api/v1/public/pharmacies/region/{}", regionId);

        List<Pharmacie> pharmacies = pharmacieRepository.findByRegionAndStatut(regionId, StatutPharmacie.ACTIVE);

        List<Map<String, Object>> result = pharmacies.stream()
                .map(this::mapPharmacieToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies de la région (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/pharmacies/{id}
     * Détail d'une pharmacie
     */
    @GetMapping("/pharmacies/{id}")
    @Operation(summary = "Détail d'une pharmacie")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPharmacieDetail(
            @PathVariable UUID id) {
        log.info("GET /api/v1/public/pharmacies/{}", id);

        Optional<Pharmacie> pharmacieOpt = pharmacieRepository.findByIdWithLocalisation(id);

        if (pharmacieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = mapPharmacieToPublicDetail(pharmacieOpt.get());
        return ResponseEntity.ok(ApiResponse.success("Pharmacie", result));
    }

    /**
     * GET /api/v1/public/pharmacies/proches?lat=xxx&lng=xxx&rayon=5
     * Pharmacies proches par coordonnées GPS
     */
    @GetMapping("/pharmacies/proches")
    @Operation(summary = "Pharmacies proches par GPS")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPharmaciesProches(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double rayon) {
        log.info("GET /api/v1/public/pharmacies/proches - lat={}, lng={}, rayon={}km", lat, lng, rayon);

        List<Pharmacie> pharmacies = pharmacieRepository.findProximite(lat, lng, rayon);

        List<Map<String, Object>> result = pharmacies.stream()
                .map(p -> {
                    double distance = calculerDistance(lat, lng, p.getLatitude(), p.getLongitude());
                    Map<String, Object> map = mapPharmacieToPublic(p);
                    map.put("distance", Math.round(distance * 100.0) / 100.0);
                    map.put("distanceFormate", formatDistance(distance));
                    return map;
                })
                .sorted(Comparator.comparingDouble(map -> (Double) map.get("distance")))
                .limit(20)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies dans un rayon de %.0f km", rayon), result));
    }

    /**
     * GET /api/v1/public/pharmacies/secteur?communeId=&lat=&lng=
     * Toutes les pharmacies ACTIVE du secteur (syndicat) qui couvre cette commune.
     * Inclut isOnDuty (garde du jour). Si lat+lng fournis, triées par distance.
     */
    @GetMapping("/pharmacies/secteur")
    @Transactional
    @Operation(summary = "Pharmacies du secteur (syndicat) d'une commune")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPharmaciesSecteur(
            @RequestParam UUID communeId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {

        log.info("GET /api/v1/public/pharmacies/secteur - communeId={}, lat={}, lng={}", communeId, lat, lng);

        // 1. Trouver le(s) syndicat(s) qui gèrent cette commune (par couverture géographique)
        List<sn.sunufarmasi.syndicat.entity.Syndicat> syndicats =
                syndicatRepository.findSyndicatsGestionnairesByCommune(communeId);

        List<Pharmacie> pharmacies;

        if (!syndicats.isEmpty()) {
            // Commune gérée par un syndicat → pharmacies de ce syndicat uniquement
            pharmacies = syndicats.stream()
                    .flatMap(s -> pharmacieRepository.findBySyndicatIdAndStatut(s.getId(), StatutPharmacie.ACTIVE).stream())
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Aucun syndicat → pharmacies de la commune directement
            pharmacies = new ArrayList<>(pharmacieRepository.findByCommuneIdAndStatut(communeId, StatutPharmacie.ACTIVE));
        }

        // Si aucune pharmacie du tout → zone non couverte
        if (pharmacies.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("syndicatNom", null);
            empty.put("pharmacies", List.of());
            empty.put("totalPharmacies", 0);
            empty.put("totalGardes", 0);
            return ResponseEntity.ok(ApiResponse.success("Secteur non couvert", empty));
        }

        // Noms des syndicats pour la réponse
        String syndicatsNom = syndicats.isEmpty() ? "Zone non syndiquée" : syndicats.stream()
                .map(sn.sunufarmasi.syndicat.entity.Syndicat::getNom)
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));

        // 3. Gardes du jour pour toutes les pharmacies du secteur
        LocalDate today = LocalDate.now();
        List<UUID> pharmacieIds = pharmacies.stream()
                .map(Pharmacie::getId)
                .collect(java.util.stream.Collectors.toList());
        Set<UUID> pharmaciesEnGarde = gardeRepository.findByDateAndPharmacieIds(today, pharmacieIds)
                .stream()
                .filter(g -> g.getPharmacie() != null)
                .map(g -> g.getPharmacie().getId())
                .collect(java.util.stream.Collectors.toSet());

        // 4. Mapper + enrichir distance
        List<Map<String, Object>> pharmaciesMapped = pharmacies.stream()
                .map(p -> {
                    Map<String, Object> map = mapPharmacieToPublic(p);
                    map.put("isOnDuty", pharmaciesEnGarde.contains(p.getId()));
                    if (lat != null && lng != null
                            && p.getLatitude() != null && p.getLongitude() != null) {
                        double distance = calculerDistance(lat, lng, p.getLatitude(), p.getLongitude());
                        map.put("distance", Math.round(distance * 100.0) / 100.0);
                        map.put("distanceFormate", formatDistance(distance));
                    }
                    return map;
                })
                .sorted((a, b) -> {
                    if (a.containsKey("distance") && b.containsKey("distance")) {
                        return Double.compare((Double) a.get("distance"), (Double) b.get("distance"));
                    }
                    return String.valueOf(a.get("nom")).compareToIgnoreCase(String.valueOf(b.get("nom")));
                })
                .toList();

        // 5. Réponse
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("syndicatNom", syndicatsNom);
        result.put("syndicats", syndicats.stream().map(s -> Map.of(
                "id", s.getId(), "nom", s.getNom(), "code", s.getCode())).toList());
        result.put("pharmacies", pharmaciesMapped);
        result.put("totalPharmacies", pharmaciesMapped.size());
        result.put("totalGardes", pharmaciesEnGarde.size());

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Secteur %s (%d pharmacies)", syndicatsNom, pharmaciesMapped.size()),
                result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // GARDES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/public/gardes/semaine
     * Gardes de la semaine avec filtres (commune, département, région)
     */
    @GetMapping("/gardes/semaine")
    @Operation(summary = "Gardes de la semaine")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGardesSemaine(
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId,
            @RequestParam(required = false) UUID regionId,
            @RequestParam(required = false) LocalDate date) {

        log.info("GET /api/v1/public/gardes/semaine - commune={}, dept={}, region={}, date={}",
                communeId, departementId, regionId, date);

        // Calculer le début et fin de semaine (samedi → vendredi)
        LocalDate reference = date != null ? date : LocalDate.now();
        LocalDate debutSemaine = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        LocalDate finSemaine = debutSemaine.plusDays(6);

        List<Garde> gardes;

        if (communeId != null) {
            gardes = gardeRepository.findBySemaineAndCommune(debutSemaine, finSemaine, communeId);
        } else if (departementId != null) {
            gardes = gardeRepository.findBySemaineAndDepartement(debutSemaine, finSemaine, departementId);
        } else if (regionId != null) {
            gardes = gardeRepository.findBySemaineAndRegion(debutSemaine, finSemaine, regionId);
        } else {
            gardes = gardeRepository.findGardesSemaineEnCours(reference);
        }

        // Regrouper par jour
        Map<String, List<Map<String, Object>>> gardesParJour = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate jour = debutSemaine.plusDays(i);
            String jourKey = jour.toString();

            List<Map<String, Object>> gardesJour = gardes.stream()
                    .filter(g -> !jour.isBefore(g.getDateDebut()) && !jour.isAfter(g.getDateFin()))
                    .filter(g -> g.getPharmacie() != null && StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()))
                    .map(this::mapGardeToPublic)
                    .toList();

            gardesParJour.put(jourKey, gardesJour);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("semaine", Map.of(
                "debut", debutSemaine.toString(),
                "fin", finSemaine.toString(),
                "numeroSemaine", debutSemaine.get(java.time.temporal.WeekFields.ISO.weekOfYear())
        ));
        result.put("gardesParJour", gardesParJour);
        result.put("totalGardes", gardes.size());

        return ResponseEntity.ok(ApiResponse.success("Gardes de la semaine", result));
    }

    /**
     * GET /api/v1/public/gardes/aujourd-hui
     * Pharmacies de garde aujourd'hui avec filtres
     */
    @GetMapping("/gardes/aujourd-hui")
    @Operation(summary = "Pharmacies de garde aujourd'hui")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGardesAujourdHui(
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId,
            @RequestParam(required = false) UUID regionId) {

        log.info("GET /api/v1/public/gardes/aujourd-hui - commune={}, dept={}, region={}",
                communeId, departementId, regionId);

        LocalDate today = LocalDate.now();
        List<Garde> gardes;

        if (communeId != null) {
            gardes = gardeRepository.findByDateAndCommune(today, communeId);
        } else if (departementId != null) {
            gardes = gardeRepository.findByDateAndDepartement(today, departementId);
        } else if (regionId != null) {
            gardes = gardeRepository.findByDateAndRegion(today, regionId);
        } else {
            gardes = gardeRepository.findGardesSemaineEnCours(today);
        }

        List<Map<String, Object>> result = gardes.stream()
                .filter(g -> g.getPharmacie() != null && StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()))
                .map(this::mapGardeToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies de garde le %s", today), result));
    }

    /**
     * GET /api/v1/public/gardes/commune/{communeId}
     * Gardes d'une commune (aujourd'hui)
     */
    @GetMapping("/gardes/commune/{communeId}")
    @Operation(summary = "Gardes d'une commune")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGardesByCommune(
            @PathVariable UUID communeId) {
        log.info("GET /api/v1/public/gardes/commune/{}", communeId);

        LocalDate today = LocalDate.now();
        List<Garde> gardes = gardeRepository.findByDateAndCommune(today, communeId);

        List<Map<String, Object>> result = gardes.stream()
                .filter(g -> g.getPharmacie() != null && StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()))
                .map(this::mapGardeToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Gardes commune (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/gardes/departement/{departementId}
     * Gardes d'un département (aujourd'hui)
     */
    @GetMapping("/gardes/departement/{departementId}")
    @Operation(summary = "Gardes d'un département")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGardesByDepartement(
            @PathVariable UUID departementId) {
        log.info("GET /api/v1/public/gardes/departement/{}", departementId);

        LocalDate today = LocalDate.now();
        List<Garde> gardes = gardeRepository.findByDateAndDepartement(today, departementId);

        List<Map<String, Object>> result = gardes.stream()
                .filter(g -> g.getPharmacie() != null && StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()))
                .map(this::mapGardeToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Gardes département (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/gardes/region/{regionId}
     * Gardes d'une région (aujourd'hui) - "Toutes les localités"
     */
    @GetMapping("/gardes/region/{regionId}")
    @Operation(summary = "Gardes d'une région")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGardesByRegion(
            @PathVariable UUID regionId) {
        log.info("GET /api/v1/public/gardes/region/{}", regionId);

        LocalDate today = LocalDate.now();
        List<Garde> gardes = gardeRepository.findByDateAndRegion(today, regionId);

        List<Map<String, Object>> result = gardes.stream()
                .filter(g -> g.getPharmacie() != null && StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()))
                .map(this::mapGardeToPublic)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Gardes région (%d)", result.size()), result));
    }

    /**
     * GET /api/v1/public/gardes/proches?lat=xxx&lng=xxx
     * Pharmacies de garde proches aujourd'hui
     */
    @GetMapping("/gardes/proches")
    @Operation(summary = "Pharmacies de garde proches")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGardesProches(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double rayon) {
        log.info("GET /api/v1/public/gardes/proches - lat={}, lng={}, rayon={}km", lat, lng, rayon);

        LocalDate today = LocalDate.now();
        List<Garde> gardes = gardeRepository.findGardesSemaineEnCours(today);

        List<Map<String, Object>> result = gardes.stream()
                .filter(g -> g.getPharmacie() != null &&
                        StatutPharmacie.ACTIVE.equals(g.getPharmacie().getStatut()) &&
                        g.getPharmacie().getLatitude() != null &&
                        g.getPharmacie().getLongitude() != null)
                .map(g -> {
                    Pharmacie p = g.getPharmacie();
                    double distance = calculerDistance(lat, lng, p.getLatitude(), p.getLongitude());
                    Map<String, Object> map = mapGardeToPublic(g);
                    map.put("distance", Math.round(distance * 100.0) / 100.0);
                    map.put("distanceFormate", formatDistance(distance));
                    return map;
                })
                .filter(map -> (Double) map.get("distance") <= rayon)
                .sorted(Comparator.comparingDouble(map -> (Double) map.get("distance")))
                .limit(10)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Pharmacies de garde dans un rayon de %.0f km", rayon), result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════════════

    private Map<String, Object> mapPharmacieToPublic(Pharmacie p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("code", p.getCode());
        map.put("nom", p.getNom());
        map.put("adresse", p.getAdresseComplete());
        map.put("quartier", p.getQuartier());
        map.put("telephone", p.getTelephone());
        map.put("latitude", p.getLatitude());
        map.put("longitude", p.getLongitude());

        // Localisation - avec protection contre LazyInitializationException
        try {
            if (p.getCommune() != null) {
                map.put("communeId", p.getCommune().getId());
                map.put("commune", p.getCommune().getNom());
                if (p.getCommune().getDepartement() != null) {
                    map.put("departementId", p.getCommune().getDepartement().getId());
                    map.put("departement", p.getCommune().getDepartement().getNom());
                    if (p.getCommune().getDepartement().getRegion() != null) {
                        map.put("regionId", p.getCommune().getDepartement().getRegion().getId());
                        map.put("region", p.getCommune().getDepartement().getRegion().getNom());
                    }
                }
            }
        } catch (org.hibernate.LazyInitializationException e) {
            log.debug("Commune non chargée pour pharmacie {}", p.getId());
        }

        // Pharmacien responsable
//        map.put("pharmacienResponsable", p.getPharmacienProprietaire());

        try {
            if (p.getPharmacienProprietaire() != null) {
                Pharmacien pharmacien = p.getPharmacienProprietaire();
                String nomComplet = pharmacien.getPrenom() + " " + pharmacien.getNom();
                map.put("pharmacienResponsable", nomComplet);
            }
        } catch (LazyInitializationException e) {
            map.put("pharmacienResponsable", null);
        }

        return map;
    }

    private Map<String, Object> mapPharmacieToPublicDetail(Pharmacie p) {
        Map<String, Object> map = mapPharmacieToPublic(p);

        // Infos supplémentaires
        map.put("email", p.getEmail());
        map.put("siteWeb", p.getSiteWeb());
        map.put("telephoneSecondaire", p.getTelephoneSecondaire());
        map.put("horaires", p.getHoraires());
        map.put("lienGoogleMaps", p.getLienGoogleMaps());

        // Services
        List<String> services = new ArrayList<>();
        if (Boolean.TRUE.equals(p.getProposeLivraison())) {
            services.add("Livraison");
            if (p.getRayonLivraisonKm() != null) {
                services.add("Livraison dans " + p.getRayonLivraisonKm() + " km");
            }
        }
        if (Boolean.TRUE.equals(p.getAccepteCommandes())) services.add("Commandes en ligne");
        map.put("services", services);

        // Statut
        map.put("statut", p.getStatut() != null ? p.getStatut().name() : null);
        map.put("estOperationnelle", p.estOperationnelle());

        return map;
    }

    private Map<String, Object> mapGardeToPublic(Garde g) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", g.getId());
        map.put("dateDebut", g.getDateDebut().toString());
        map.put("dateFin", g.getDateFin().toString());
        map.put("typeGarde", g.getTypeGarde() != null ? g.getTypeGarde().name() : null);
        map.put("statut", g.getStatut() != null ? g.getStatut().name() : null);

        // Pharmacie
        if (g.getPharmacie() != null) {
            map.put("pharmacie", mapPharmacieToPublic(g.getPharmacie()));
        }

        // Zone
        map.put("zoneNom", g.getZoneNom());

        // Commune/Département avec IDs
        if (g.getCommune() != null) {
            map.put("communeId", g.getCommune().getId());
            map.put("commune", g.getCommune().getNom());
        }
        if (g.getDepartement() != null) {
            map.put("departementId", g.getDepartement().getId());
            map.put("departement", g.getDepartement().getNom());
        }

        return map;
    }

    /**
     * Calcul de distance entre deux points GPS (formule Haversine)
     * @return distance en kilomètres
     */
    private double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Formater la distance pour affichage
     */
    private String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return Math.round(distanceKm * 1000) + " m";
        } else {
            return Math.round(distanceKm * 10.0) / 10.0 + " km";
        }
    }
}