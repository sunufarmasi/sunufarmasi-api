package sn.sunufarmasi.patient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.shared.dto.ApiResponse;

import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;
import sn.sunufarmasi.stock.repository.ProduitRepository;
import sn.sunufarmasi.stock.repository.ProduitPharmacieRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * API Patient - Recherche de médicaments
 * Nécessite un abonnement actif (FREE_TRIAL, MONTHLY, ou ANNUAL)
 *
 * Fonctionnalités:
 * - Rechercher des médicaments par nom, DCI, code
 * - Voir la disponibilité dans les pharmacies
 * - Détail complet d'un médicament
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/patient/medicaments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Médicaments Patient", description = "Recherche de médicaments (abonnement requis)")
public class PatientMedicamentController {

    private final PatientRepository patientRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final ProduitRepository produitRepository;
    private final ProduitPharmacieRepository produitPharmacieRepository;

    // ═══════════════════════════════════════════════════════════════════════════════
    // RECHERCHE MÉDICAMENTS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/patient/medicaments/search?q=doliprane&patientId=xxx
     * Rechercher des médicaments
     */
    @GetMapping("/search")
    @Operation(summary = "Rechercher médicaments", description = "Recherche par nom, DCI ou code (abonnement requis)")
    public ResponseEntity<ApiResponse<Object>> searchMedicaments(
            @RequestParam String q,
            @RequestParam UUID patientId,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("GET /api/v1/patient/medicaments/search - q={}, patient={}", q, patientId);

        // 1. Vérifier l'abonnement
        SubscriptionCheckResult checkResult = checkSubscription(patientId);
        if (!checkResult.isValid()) {
            Map<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("code", "SUBSCRIPTION_REQUIRED");
            errorData.put("canSearchMedicaments", false);
            errorData.put("availablePlans", getAvailablePlans());
            errorData.put("message", checkResult.message());

            return ResponseEntity.status(403).body(
                    ApiResponse.success(checkResult.message(), (Object) errorData)
            );
        }

        // 2. Valider la recherche
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("La recherche doit contenir au moins 2 caractères")
            );
        }

        // 3. Rechercher les médicaments (utiliser searchByNomOrDciOrCode)
        List<Produit> produits = produitRepository.searchByNomOrDciOrCode(q.trim()).stream()
                .limit(limit)
                .toList();

        if (produits.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(
                    "Aucun médicament trouvé pour \"" + q + "\"",
                    Map.of("results", List.of(), "total", 0, "query", q)
            ));
        }

        // 4. Mapper les résultats
        List<Map<String, Object>> results = produits.stream()
                .map(this::mapProduitToSimple)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("%d médicament(s) trouvé(s)", results.size()),
                Map.of(
                        "results", results,
                        "total", results.size(),
                        "query", q
                )
        ));
    }

    /**
     * GET /api/v1/patient/medicaments/{produitId}?patientId=xxx
     * Détail complet d'un médicament
     */
    @GetMapping("/{produitId}")
    @Operation(summary = "Détail médicament", description = "Informations complètes (abonnement requis)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMedicamentDetail(
            @PathVariable UUID produitId,
            @RequestParam UUID patientId) {
        log.info("GET /api/v1/patient/medicaments/{} - patient={}", produitId, patientId);

        // 1. Vérifier l'abonnement
        SubscriptionCheckResult checkResult = checkSubscription(patientId);
        if (!checkResult.isValid()) {
            return ResponseEntity.status(403).body(
                    ApiResponse.error(checkResult.message())
            );
        }

        // 2. Récupérer le produit
        Optional<Produit> produitOpt = produitRepository.findById(produitId);
        if (produitOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = mapProduitToDetail(produitOpt.get());

        return ResponseEntity.ok(ApiResponse.success("Détail du médicament", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // DISPONIBILITÉ
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/patient/medicaments/{produitId}/disponibilite?patientId=xxx
     * Disponibilité dans les pharmacies
     */
    @GetMapping("/{produitId}/disponibilite")
    @Operation(summary = "Disponibilité médicament", description = "Pharmacies ayant ce médicament en stock")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDisponibilite(
            @PathVariable UUID produitId,
            @RequestParam UUID patientId,
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10") double rayon) {
        log.info("GET /api/v1/patient/medicaments/{}/disponibilite - patient={}", produitId, patientId);

        // 1. Vérifier l'abonnement
        SubscriptionCheckResult checkResult = checkSubscription(patientId);
        if (!checkResult.isValid()) {
            Map<String, Object> errorData = new LinkedHashMap<>();
            errorData.put("code", "SUBSCRIPTION_REQUIRED");
            errorData.put("canSearchMedicaments", false);

            return ResponseEntity.status(403).body(
                    ApiResponse.error(checkResult.message())
            );
        }

        // 2. Récupérer le produit
        Optional<Produit> produitOpt = produitRepository.findById(produitId);
        if (produitOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Produit produit = produitOpt.get();

        // 3. Chercher les pharmacies qui ont ce produit
        List<ProduitPharmacie> stocks = produitPharmacieRepository.findByProduitId(produitId);

        // 4. Filtrer: pharmacie opérationnelle, produit actif, en stock
        stocks = stocks.stream()
                .filter(pp -> pp.getEstActif() != null && pp.getEstActif())
                .filter(pp -> pp.getPharmacie() != null && pp.getPharmacie().estOperationnelle())
                .filter(pp -> pp.getQuantiteStock() != null && pp.getQuantiteStock() > 0)
                .toList();

        // 5. Filtrer par commune si spécifié
        if (communeId != null) {
            stocks = stocks.stream()
                    .filter(pp -> pp.getPharmacie().getCommune() != null &&
                            pp.getPharmacie().getCommune().getId().equals(communeId))
                    .toList();
        }

        // 6. Construire la liste des pharmacies avec stock
        List<Map<String, Object>> pharmaciesAvecStock = new ArrayList<>();

        for (ProduitPharmacie pp : stocks) {
            Map<String, Object> item = new LinkedHashMap<>();

            // Infos pharmacie
            item.put("pharmacieId", pp.getPharmacie().getId().toString());
            item.put("pharmacieNom", pp.getPharmacie().getNom());
            item.put("pharmacieAdresse", pp.getPharmacie().getAdresseComplete());
            item.put("pharmacieTelephone", pp.getPharmacie().getTelephone());

            // Coordonnées GPS
            if (pp.getPharmacie().getLatitude() != null) {
                item.put("latitude", pp.getPharmacie().getLatitude());
            }
            if (pp.getPharmacie().getLongitude() != null) {
                item.put("longitude", pp.getPharmacie().getLongitude());
            }

            // Localisation
            if (pp.getPharmacie().getCommune() != null) {
                item.put("commune", pp.getPharmacie().getCommune().getNom());
                if (pp.getPharmacie().getCommune().getDepartement() != null) {
                    item.put("departement", pp.getPharmacie().getCommune().getDepartement().getNom());
                }
            }

            // Stock et prix
            item.put("enStock", true);
            item.put("quantiteDisponible", pp.getQuantiteStock());
            item.put("prixVente", pp.getPrixVenteTTC());
            item.put("prixFormate", pp.getPrixVenteTTC() != null ? pp.getPrixVenteTTC() + " FCFA" : null);

            // Distance si GPS fourni
            if (lat != null && lng != null &&
                    pp.getPharmacie().getLatitude() != null &&
                    pp.getPharmacie().getLongitude() != null) {

                double distance = calculerDistance(lat, lng,
                        pp.getPharmacie().getLatitude().doubleValue(),
                        pp.getPharmacie().getLongitude().doubleValue());

                // Filtrer par rayon
                if (distance <= rayon) {
                    item.put("distance", Math.round(distance * 100.0) / 100.0);
                    item.put("distanceFormate", formatDistance(distance));
                    pharmaciesAvecStock.add(item);
                }
            } else {
                pharmaciesAvecStock.add(item);
            }
        }

        // 7. Trier par distance si disponible
        if (lat != null && lng != null) {
            pharmaciesAvecStock.sort(Comparator.comparingDouble(
                    m -> m.containsKey("distance") ? (Double) m.get("distance") : Double.MAX_VALUE
            ));
        }

        // Limiter à 20 résultats
        pharmaciesAvecStock = pharmaciesAvecStock.stream().limit(20).toList();

        // 8. Construire la réponse
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("produit", Map.of(
                "id", produit.getId().toString(),
                "nom", produit.getNom(),
                "nomComplet", produit.getNomComplet(),
                "dci", produit.getDci() != null ? produit.getDci() : "",
                "dosage", produit.getDosage() != null ? produit.getDosage() : "",
                "prixPublic", produit.getPrixPublicTTC(),
                "prixFormate", produit.getPrixPublicTTC() != null ? produit.getPrixPublicTTC() + " FCFA" : null
        ));
        result.put("pharmaciesDisponibles", pharmaciesAvecStock);
        result.put("totalPharmacies", pharmaciesAvecStock.size());

        String message = pharmaciesAvecStock.isEmpty()
                ? "Ce médicament n'est pas disponible actuellement dans les pharmacies proches"
                : String.format("Disponible dans %d pharmacie(s)", pharmaciesAvecStock.size());

        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Vérifier si le patient a un abonnement actif
     */
    @Transactional
    protected SubscriptionCheckResult checkSubscription(UUID patientId) {
        // Vérifier que le patient existe
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        if (patientOpt.isEmpty()) {
            return new SubscriptionCheckResult(false, "Patient non trouvé. Veuillez vous reconnecter.");
        }

        Patient patient = patientOpt.get();
        if (!patient.isActif()) {
            return new SubscriptionCheckResult(false, "Votre compte a été désactivé.");
        }

        // Chercher un abonnement actif
        Optional<Subscription> subOpt = subscriptionRepository
                .findByPatientIdAndStatus(patientId, SubscriptionStatus.ACTIVE);

        if (subOpt.isEmpty()) {
            return new SubscriptionCheckResult(false,
                    "Aucun abonnement actif. Souscrivez pour accéder à la recherche de médicaments.");
        }

        Subscription sub = subOpt.get();

        // Vérifier expiration
        if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
            return new SubscriptionCheckResult(false,
                    "Votre abonnement a expiré. Renouvelez pour continuer à utiliser la recherche.");
        }

        return new SubscriptionCheckResult(true, "OK");
    }

    /**
     * Mapper un produit en version simple (pour liste)
     */
    private Map<String, Object> mapProduitToSimple(Produit p) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId().toString());
        map.put("code", p.getCode());
        map.put("nom", p.getNom());
        map.put("nomComplet", p.getNomComplet());
        map.put("dci", p.getDci());
        map.put("dosage", p.getDosage());

        if (p.getForme() != null) {
            map.put("forme", p.getForme().getLibelle());
        }
        if (p.getCategorie() != null) {
            map.put("categorie", p.getCategorie().name());
        }

        map.put("prixPublic", p.getPrixPublicTTC());
        map.put("prixFormate", p.getPrixPublicTTC() != null ? p.getPrixPublicTTC() + " FCFA" : null);
        map.put("surOrdonnance", p.getSurOrdonnance());
        map.put("estRemboursable", p.getEstRemboursable());
        map.put("imageUrl", p.getImageUrl());

        return map;
    }

    /**
     * Mapper un produit en version détaillée
     */
    private Map<String, Object> mapProduitToDetail(Produit p) {
        Map<String, Object> map = new LinkedHashMap<>();

        // Identifiants
        map.put("id", p.getId().toString());
        map.put("code", p.getCode());
        map.put("codeBarre", p.getCodeBarre());
        map.put("codeCip", p.getCodeCip());

        // Noms
        map.put("nom", p.getNom());
        map.put("nomComplet", p.getNomComplet());
        map.put("dci", p.getDci());
        map.put("dosage", p.getDosage());

        // Classification
        if (p.getForme() != null) {
            map.put("forme", p.getForme().getLibelle());
        }
        if (p.getCategorie() != null) {
            map.put("categorie", p.getCategorie().name());
        }
        map.put("laboratoire", p.getLaboratoire());

        // Prix
        map.put("prixPublicTTC", p.getPrixPublicTTC());
        map.put("prixFormate", p.getPrixPublicTTC() != null ? p.getPrixPublicTTC() + " FCFA" : null);
        map.put("tauxTVA", p.getTauxTVA());

        // Réglementation
        map.put("surOrdonnance", p.getSurOrdonnance());
        map.put("estRemboursable", p.getEstRemboursable());
        map.put("tauxRemboursement", p.getTauxRemboursement());
        map.put("estGenerique", p.getEstGenerique());
        map.put("listeMedicament", p.getListeMedicament());

        // Conditionnement
        map.put("contenance", p.getContenance());
        map.put("uniteVente", p.getUniteVente() != null ? p.getUniteVente().name() : null);

        // Conservation
        map.put("temperatureConservation", p.getTemperatureConservation());
        map.put("chaineFroid", p.getChaineFroid());

        // Description
        map.put("description", p.getDescription());

        // Image
        map.put("imageUrl", p.getImageUrl());
        map.put("noticeUrl", p.getNoticeUrl());

        return map;
    }

    /**
     * Récupérer les plans disponibles
     */
    private List<Map<String, Object>> getAvailablePlans() {
        return subscriptionPlanRepository.findByActifTrueOrderByOrdreAsc().stream()
                .filter(p -> !"FREE_TRIAL".equals(p.getCode()))
                .map(p -> Map.<String, Object>of(
                        "code", p.getCode(),
                        "nom", p.getNom(),
                        "prix", p.getPrix(),
                        "prixFormate", p.getPrix() + " FCFA",
                        "dureeJours", p.getDureeJours()
                ))
                .toList();
    }

    /**
     * Calculer la distance entre deux points GPS (Haversine)
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

    /**
     * Résultat de vérification d'abonnement
     */
    private record SubscriptionCheckResult(boolean isValid, String message) {}
}