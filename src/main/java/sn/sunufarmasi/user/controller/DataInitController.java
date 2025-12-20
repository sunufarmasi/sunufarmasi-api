package sn.sunufarmasi.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.employe.entity.Employe;
import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;
import sn.sunufarmasi.employe.repository.EmployeRepository;
import sn.sunufarmasi.garde.entity.Garde;
import sn.sunufarmasi.garde.entity.PlanningGarde;
import sn.sunufarmasi.garde.entity.StatutPlanning;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;
import sn.sunufarmasi.garde.repository.GardeRepository;
import sn.sunufarmasi.garde.repository.PlanningGardeRepository;
import sn.sunufarmasi.localisation.entity.*;
import sn.sunufarmasi.stock.entity.MouvementStock;
import sn.sunufarmasi.stock.entity.Produit;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;
import sn.sunufarmasi.stock.enums.*;
import sn.sunufarmasi.stock.repository.MouvementStockRepository;
import sn.sunufarmasi.stock.repository.ProduitPharmacieRepository;
import sn.sunufarmasi.stock.repository.ProduitRepository;
import sn.sunufarmasi.localisation.enums.TypeCommune;

import java.util.Random;
import sn.sunufarmasi.localisation.repository.*;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.*;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepository;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.shared.dto.ApiResponse;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;
import sn.sunufarmasi.user.entity.User;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;
import sn.sunufarmasi.user.repository.UserRepository;

import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.payment.entity.Payment;
import sn.sunufarmasi.payment.entity.PaymentMethod;
import sn.sunufarmasi.payment.entity.PaymentStatus;
import sn.sunufarmasi.payment.repository.PaymentRepository;
import sn.sunufarmasi.subscription.entity.Subscription;
import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
import sn.sunufarmasi.subscription.repository.SubscriptionRepository;



import sn.sunufarmasi.mutuelle.entity.Mutuelle;
import sn.sunufarmasi.mutuelle.entity.Adherent;
import sn.sunufarmasi.mutuelle.entity.ContratMutuelle;
import sn.sunufarmasi.mutuelle.enums.TypeMutuelle;
import sn.sunufarmasi.mutuelle.enums.TypeCouverture;
import sn.sunufarmasi.mutuelle.repository.MutuelleRepository;
import sn.sunufarmasi.mutuelle.repository.AdherentRepository;
import sn.sunufarmasi.mutuelle.repository.ContratMutuelleRepository;

import sn.sunufarmasi.vente.entity.Vente;
import sn.sunufarmasi.vente.entity.LigneVente;
import sn.sunufarmasi.vente.enums.ModePaiement;
import sn.sunufarmasi.vente.enums.StatutVente;
import sn.sunufarmasi.vente.enums.TypeVente;
import sn.sunufarmasi.vente.repository.VenteRepository;
import sn.sunufarmasi.vente.repository.LigneVenteRepository;
import sn.sunufarmasi.stock.entity.ProduitPharmacie;
import sn.sunufarmasi.stock.repository.ProduitPharmacieRepository;
import java.util.Random;
import java.util.LinkedHashMap;



import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller d'initialisation des données de test
 * ⚠️ À DÉSACTIVER EN PRODUCTION !
 *
 * @author WeCan
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/init")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Init", description = "⚠️ Initialisation des données de test - DÉSACTIVER EN PRODUCTION")
public class DataInitController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaysRepository paysRepository;
    private final RegionRepository regionRepository;
    private final DepartementRepository departementRepository;
    private final CommuneRepository communeRepository;
    private final PharmacienRepository pharmacienRepository;
    private final PharmacieRepository pharmacieRepository;
    private final EmployeRepository employeRepository;
    private final SyndicatRepository syndicatRepository;
    private final PlanningGardeRepository planningGardeRepository;
    private final GardeRepository gardeRepository;
    private final ProduitRepository produitRepository;
    private final ProduitPharmacieRepository produitPharmacieRepository;
    private final MouvementStockRepository mouvementStockRepository;

    private final PatientRepository patientRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    private final MutuelleRepository mutuelleRepository;
    private final AdherentRepository adherentRepository;
    private final ContratMutuelleRepository contratMutuelleRepository;

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
//    private final ProduitPharmacieRepository produitPharmacieRepository;

    // ═══════════════════════════════════════════════════════════════════════════
    // SEED ALL - Initialiser toutes les données
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-all")
    @Operation(summary = "🚀 Initialiser TOUTES les données de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAll() {
        log.warn("⚠️ SEED ALL - Initialisation complète des données de test");

        Map<String, Object> results = new LinkedHashMap<>();

        // 1. Localisation
        try {
            var locResult = seedLocalisation();
            results.put("localisation", locResult.getBody().data());
        } catch (Exception e) {
            results.put("localisation", "Erreur: " + e.getMessage());
        }

        // 2. Users
        try {
            var userResult = seedUsers();
            results.put("users", userResult.getBody().data());
        } catch (Exception e) {
            results.put("users", "Erreur: " + e.getMessage());
        }

        // 3. Pharmaciens
        try {
            var pharmResult = seedPharmaciens();
            results.put("pharmaciens", pharmResult.getBody().data());
        } catch (Exception e) {
            results.put("pharmaciens", "Erreur: " + e.getMessage());
        }

        // 4. Pharmacies
        try {
            var pharmacieResult = seedPharmacies();
            results.put("pharmacies", pharmacieResult.getBody().data());
        } catch (Exception e) {
            results.put("pharmacies", "Erreur: " + e.getMessage());
        }

        // 5. Syndicats
        try {
            var syndicatResult = seedSyndicats();
            results.put("syndicats", syndicatResult.getBody().data());
        } catch (Exception e) {
            results.put("syndicats", "Erreur: " + e.getMessage());
        }

        // 6. Employés
        try {
            var employeResult = seedEmployes();
            results.put("employes", employeResult.getBody().data());
        } catch (Exception e) {
            results.put("employes", "Erreur: " + e.getMessage());
        }

        // 7. Plannings et Gardes
        try {
            var gardeResult = seedGardes();
            results.put("gardes", gardeResult.getBody().data());
        } catch (Exception e) {
            results.put("gardes", "Erreur: " + e.getMessage());
        }

        // 8. Produits et Stock
        try {
            var stockResult = seedStock();
            results.put("stock", stockResult.getBody().data());
        } catch (Exception e) {
            results.put("stock", "Erreur: " + e.getMessage());
        }

        // 9. Plans d'abonnement
        seedSubscriptionPlans();

        // 10. Patients
        seedPatients();

        // 11. Abonnements
        seedSubscriptions();

        // 12. Paiements
        seedPayments();

        // 13. Mutuelles
        seedMutuelles();

        // 14. Adhérents mutuelles
        seedAdherents();

        // 15. Contrats pharmacies-mutuelles
        seedContratsMutuelles();

        // 16. Ventes
        seedVentes();

        log.info("✅ SEED ALL terminé");

        return ResponseEntity.ok(ApiResponse.success("Initialisation complète terminée", results));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LOCALISATION
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-localisation")
    @Operation(summary = "Initialiser les données de localisation du Sénégal")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedLocalisation() {
        log.info("🌍 Initialisation localisation Sénégal...");

        int paysCount = 0, regionCount = 0, deptCount = 0, communeCount = 0;

        // Créer le Sénégal
        Pays senegal = paysRepository.findByCode("SN").orElseGet(() -> {
            Pays p = Pays.builder()
                    .code("SN")
                    .codeIso2("SN")
                    .codeIso3("SEN")
                    .nom("Sénégal")
                    .nomEn("Senegal")
                    .capitale("Dakar")
                    .indicatifTelephonique("+221")
                    .devise("XOF")
                    .fuseauHoraire("Africa/Dakar")
                    .drapeau("🇸🇳")
                    .actif(true)
                    .build();
            return paysRepository.save(p);
        });
        paysCount = 1;

        // Régions du Sénégal avec leurs départements et communes principales
        Object[][] regionsData = {
                {"DK", "Dakar", "Dakar", 14.7167, -17.4677, new Object[][]{
                        {"DK", "Dakar", new String[]{"Plateau", "Médina", "Grand Dakar", "Parcelles Assainies", "Almadies", "Ouakam", "Yoff", "Ngor", "Mermoz", "Fann"}},
                        {"GU", "Guédiawaye", new String[]{"Guédiawaye", "Sam Notaire", "Wakhinane Nimzatt"}},
                        {"PK", "Pikine", new String[]{"Pikine", "Thiaroye", "Diamaguene", "Keur Massar"}},
                        {"RU", "Rufisque", new String[]{"Rufisque", "Bargny", "Diamniadio", "Sébikotane"}}
                }},
                {"TH", "Thiès", "Thiès", 14.7886, -16.9260, new Object[][]{
                        {"TH", "Thiès", new String[]{"Thiès Nord", "Thiès Sud", "Thiès Est", "Thiès Ouest"}},
                        {"MB", "Mbour", new String[]{"Mbour", "Saly", "Somone", "Ngaparou", "Joal-Fadiouth"}},
                        {"TI", "Tivaouane", new String[]{"Tivaouane", "Mboro", "Mékhé"}}
                }},
                {"SL", "Saint-Louis", "Saint-Louis", 16.0326, -16.4818, new Object[][]{
                        {"SL", "Saint-Louis", new String[]{"Saint-Louis Nord", "Saint-Louis Sud", "Sor"}},
                        {"DG", "Dagana", new String[]{"Dagana", "Richard-Toll", "Ross-Béthio"}},
                        {"PD", "Podor", new String[]{"Podor", "Ndioum"}}
                }},
                {"ZG", "Ziguinchor", "Ziguinchor", 12.5833, -16.2719, new Object[][]{
                        {"ZG", "Ziguinchor", new String[]{"Ziguinchor", "Niaguis"}},
                        {"OU", "Oussouye", new String[]{"Oussouye", "Cap Skirring"}},
                        {"BI", "Bignona", new String[]{"Bignona", "Diouloulou"}}
                }},
                {"KL", "Kaolack", "Kaolack", 14.1652, -16.0758, new Object[][]{
                        {"KL", "Kaolack", new String[]{"Kaolack", "Kahone", "Ndoffane"}},
                        {"GN", "Guinguinéo", new String[]{"Guinguinéo"}},
                        {"NI", "Nioro du Rip", new String[]{"Nioro du Rip", "Paoskoto"}}
                }}
        };

        for (Object[] regionData : regionsData) {
            String regCode = (String) regionData[0];
            String regNom = (String) regionData[1];
            String chefLieu = (String) regionData[2];
            Double lat = (Double) regionData[3];
            Double lng = (Double) regionData[4];
            Object[][] departementsData = (Object[][]) regionData[5];

            // Créer la région (utilise findByCodeAndPaysId)
            final String finalRegCode = regCode;
            Region region = regionRepository.findByCodeAndPaysId(regCode, senegal.getId()).orElseGet(() -> {
                Region r = Region.builder()
                        .pays(senegal)
                        .code(finalRegCode)
                        .nom(regNom)
                        .chefLieu(chefLieu)
                        .latitude(lat)
                        .longitude(lng)
                        .actif(true)
                        .build();
                return regionRepository.save(r);
            });
            regionCount++;

            // Créer les départements et communes
            for (Object[] deptData : departementsData) {
                String deptCode = (String) deptData[0];
                String deptNom = (String) deptData[1];
                String[] communes = (String[]) deptData[2];

                // Utilise findByCodeAndRegionId
                final String finalDeptCode = deptCode;
                Departement dept = departementRepository.findByCodeAndRegionId(deptCode, region.getId()).orElseGet(() -> {
                    Departement d = Departement.builder()
                            .region(region)
                            .code(finalDeptCode)
                            .nom(deptNom)
                            .chefLieu(deptNom)
                            .actif(true)
                            .build();
                    return departementRepository.save(d);
                });
                deptCount++;

                // Créer les communes (utilise existsByCodeAndDepartementId)
                for (String communeNom : communes) {
                    String communeCode = communeNom.toUpperCase().replace(" ", "_").substring(0, Math.min(10, communeNom.length()));
                    if (!communeRepository.existsByCodeAndDepartementId(communeCode, dept.getId())) {
                        Commune c = Commune.builder()
                                .departement(dept)
                                .code(communeCode)
                                .nom(communeNom)
                                .type(TypeCommune.COMMUNE)
                                .zoneUrbaine(true)
                                .actif(true)
                                .build();
                        communeRepository.save(c);
                        communeCount++;
                    }
                }
            }
        }

        log.info("✅ Localisation: {} pays, {} régions, {} départements, {} communes",
                paysCount, regionCount, deptCount, communeCount);

        return ResponseEntity.ok(ApiResponse.success("Localisation initialisée", Map.of(
                "pays", paysCount,
                "regions", regionCount,
                "departements", deptCount,
                "communes", communeCount
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // USERS
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-users")
    @Operation(summary = "Initialiser les utilisateurs de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedUsers() {
        log.info("👤 Initialisation users...");
        int count = 0;

        Object[][] usersData = {
                {"admin@sunufarmasi.sn", "DIOP", "Amadou", "+221771234567", RoleUser.ADMIN},
                {"syndicat@sunufarmasi.sn", "NDIAYE", "Fatou", "+221772345678", RoleUser.ADMIN_SYNDICAT},
                {"employe1@sunufarmasi.sn", "SOW", "Abdoulaye", "+221778901234", RoleUser.EMPLOYE},
                {"employe2@sunufarmasi.sn", "MBAYE", "Khady", "+221779012345", RoleUser.EMPLOYE},
                {"client@test.sn", "THIAM", "Modou", "+221781234567", RoleUser.CLIENT}
        };

        for (Object[] userData : usersData) {
            String email = (String) userData[0];
            if (!userRepository.existsByEmail(email)) {
                User user = User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("password123"))
                        .nom((String) userData[1])
                        .prenom((String) userData[2])
                        .telephone((String) userData[3])
                        .role((RoleUser) userData[4])
                        .statut(StatutUser.ACTIF)
                        .emailVerifie(true)
                        .build();
                userRepository.save(user);
                count++;
            }
        }

        log.info("✅ {} users créés", count);

        return ResponseEntity.ok(ApiResponse.success(count + " users créés", Map.of(
                "count", count,
                "defaultPassword", "password123"
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PHARMACIENS
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-pharmaciens")
    @Operation(summary = "Initialiser les pharmaciens de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedPharmaciens() {
        log.info("💊 Initialisation pharmaciens...");
        int count = 0;

        Object[][] pharmaciensData = {
                {"FALL", "Ibrahima", "pharmacien.plateau@gmail.com", "+221773456789", "ORD-SN-001", "UCAD"},
                {"SARR", "Aminata", "pharmacien.medina@gmail.com", "+221774567890", "ORD-SN-002", "UCAD"},
                {"GUEYE", "Moussa", "pharmacien.almadies@gmail.com", "+221775678901", "ORD-SN-003", "UGB"},
                {"DIALLO", "Fatou", "pharmacien.pikine@gmail.com", "+221776789012", "ORD-SN-004", "UCAD"},
                {"NDIAYE", "Omar", "pharmacien.thies@gmail.com", "+221777890123", "ORD-SN-005", "UCAD"}
        };

        for (Object[] data : pharmaciensData) {
            String telephone = (String) data[3];
            if (!pharmacienRepository.existsByTelephone(telephone)) {
                Pharmacien pharmacien = Pharmacien.builder()
                        .nom((String) data[0])
                        .prenom((String) data[1])
                        .email((String) data[2])
                        .telephone(telephone)
                        .motDePasseHash(passwordEncoder.encode("password123"))
                        .numeroOrdreNational((String) data[4])
                        .universiteFormation((String) data[5])
                        .anneeDiplome(2015 + count)
                        .type(TypePharmacien.PROPRIETAIRE)
                        .plan(PlanAbonnementPharmacie.PREMIUM)
                        .statutAbonnement(StatutAbonnement.ACTIF)
                        .montantMensuel(new BigDecimal("25000"))
                        .essaiGratuit(false)
                        .dateDebutAbonnement(LocalDate.now().minusMonths(3))
                        .dateFinAbonnement(LocalDate.now().plusMonths(9))
                        .nombreEmployesMax(5)
                        .nombrePharmaciesMax(1)
                        .nombrePharmaciesActuelles(0)
                        .actif(true)
                        .valideParOrdre(true)
                        .compteVerifie(true)
                        .sexe(count % 2 == 0 ? "M" : "F")
                        .dateNaissance(LocalDate.of(1985, 1, 1).plusYears(count))
                        .build();
                pharmacienRepository.save(pharmacien);
                count++;
            }
        }

        log.info("✅ {} pharmaciens créés", count);

        return ResponseEntity.ok(ApiResponse.success(count + " pharmaciens créés", Map.of(
                "count", count,
                "defaultPassword", "password123"
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PHARMACIES
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-pharmacies")
    @Operation(summary = "Initialiser les pharmacies de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedPharmacies() {
        log.info("🏥 Initialisation pharmacies...");
        int count = 0;

        // Récupérer les pharmaciens
        List<Pharmacien> pharmaciens = pharmacienRepository.findAll();
        if (pharmaciens.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun pharmacien trouvé. Exécutez d'abord /seed-pharmaciens")
            );
        }

        // Récupérer quelques communes de Dakar
        List<Commune> communes = communeRepository.findAll();
        if (communes.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune commune trouvée. Exécutez d'abord /seed-localisation")
            );
        }

        Object[][] pharmaciesData = {
                {"Pharmacie du Plateau", "PHAR-DK-001", "Avenue Pompidou", "Plateau", 14.6694, -17.4378, "+221338234567"},
                {"Pharmacie de la Médina", "PHAR-DK-002", "Rue 10 x Blaise Diagne", "Médina", 14.6763, -17.4453, "+221338234568"},
                {"Pharmacie des Almadies", "PHAR-DK-003", "Route des Almadies", "Almadies", 14.7449, -17.5098, "+221338234569"},
                {"Grande Pharmacie de Pikine", "PHAR-DK-004", "Avenue Bourguiba", "Pikine", 14.7500, -17.3833, "+221338234570"},
                {"Pharmacie Centrale de Thiès", "PHAR-TH-001", "Avenue Léopold Sédar Senghor", "Thiès", 14.7886, -16.9260, "+221339234571"}
        };

        int pharmacienIndex = 0;
        int communeIndex = 0;

        for (Object[] data : pharmaciesData) {
            String code = (String) data[1];
            if (!pharmacieRepository.existsByCode(code)) {
                Pharmacien proprietaire = pharmaciens.get(pharmacienIndex % pharmaciens.size());
                Commune commune = communes.get(communeIndex % communes.size());

                Pharmacie pharmacie = Pharmacie.builder()
                        .nom((String) data[0])
                        .code(code)
                        .adresseComplete((String) data[2])
                        .quartier((String) data[3])
                        .latitude((Double) data[4])
                        .longitude((Double) data[5])
                        .telephone((String) data[6])
                        .email("contact." + code.toLowerCase().replace("-", "") + "@sunufarmasi.sn")
                        .commune(commune)
                        .pharmacienProprietaire(proprietaire)
                        .statut(StatutPharmacie.VALIDEE)
                        .dateValidation(LocalDateTime.now().minusDays(30))
                        .accepteCommandes(true)
                        .proposeLivraison(count % 2 == 0)
                        .rayonLivraisonKm(count % 2 == 0 ? 5 : null)
                        .notificationsActives(true)
                        // horaires = null pour éviter erreur PostgreSQL JSON casting
                        .build();

                pharmacieRepository.save(pharmacie);

                // Mettre à jour le compteur du pharmacien
                proprietaire.ajouterPharmacie();
                pharmacienRepository.save(proprietaire);

                count++;
                pharmacienIndex++;
                communeIndex++;
            }
        }

        log.info("✅ {} pharmacies créées", count);

        return ResponseEntity.ok(ApiResponse.success(count + " pharmacies créées", Map.of(
                "count", count
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SYNDICATS
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-syndicats")
    @Operation(summary = "Initialiser les syndicats de test")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedSyndicats() {
        log.info("🏛️ Initialisation syndicats...");
        int count = 0;

        // Récupérer les départements
        List<Departement> departements = departementRepository.findAll();
        if (departements.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun département trouvé. Exécutez d'abord /seed-localisation")
            );
        }

        // Récupérer quelques communes
        List<Commune> communes = communeRepository.findAll();

        Object[][] syndicatsData = {
                {"Syndicat des Pharmaciens de Dakar", "SPD", "SYN-DK-001", "syndicat.dakar", TypeSyndicat.DEPARTEMENT, "+221338001001"},
                {"Syndicat des Pharmaciens de Thiès", "SPT", "SYN-TH-001", "syndicat.thies", TypeSyndicat.DEPARTEMENT, "+221339001001"},
                {"Syndicat des Pharmaciens du Plateau", "SPP", "SYN-DK-002", "syndicat.plateau", TypeSyndicat.COMMUNE, "+221338001002"},
                {"Syndicat des Pharmaciens de Mbour", "SPM", "SYN-MB-001", "syndicat.mbour", TypeSyndicat.COMMUNE, "+221339001002"}
        };

        int deptIndex = 0;
        int communeIndex = 0;

        for (Object[] data : syndicatsData) {
            String code = (String) data[2];
            if (!syndicatRepository.existsByCode(code)) {
                TypeSyndicat type = (TypeSyndicat) data[4];

                Syndicat.SyndicatBuilder builder = Syndicat.builder()
                        .nom((String) data[0])
                        .sigle((String) data[1])
                        .code(code)
                        .username((String) data[3])
                        .motDePasseHash(passwordEncoder.encode("password123"))
                        .type(type)
                        .telephone((String) data[5])
                        .email(data[3] + "@sunufarmasi.sn")
                        .nomResponsable("Dr. " + (count % 2 == 0 ? "DIOP" : "NDIAYE"))
                        .telephoneResponsable("+22177" + (1000000 + count))
                        .plan(type == TypeSyndicat.DEPARTEMENT ? PlanAbonnementSyndicat.DEPARTEMENT : PlanAbonnementSyndicat.COMMUNE)
                        .statutAbonnement(StatutAbonnement.ACTIF)
                        .montantMensuel(type == TypeSyndicat.DEPARTEMENT ? new BigDecimal("75000") : new BigDecimal("25000"))
                        .essaiGratuit(false)
                        .dateDebutAbonnement(LocalDate.now().minusMonths(2))
                        .dateFinAbonnement(LocalDate.now().plusMonths(10))
                        .nombrePharmaciesMax(type == TypeSyndicat.DEPARTEMENT ? 200 : 50)
                        .nombrePharmaciesActuelles(0)
                        .statut(StatutSyndicat.ACTIF);

                // Associer département ou commune selon le type
                if (type == TypeSyndicat.DEPARTEMENT && !departements.isEmpty()) {
                    Departement dept = departements.get(deptIndex % departements.size());
                    builder.departement(dept);
                    builder.region(dept.getRegion());
                    deptIndex++;
                } else if (type == TypeSyndicat.COMMUNE && !communes.isEmpty()) {
                    Commune commune = communes.get(communeIndex % communes.size());
                    builder.commune(commune);
                    builder.region(commune.getDepartement().getRegion());
                    communeIndex++;
                }

                syndicatRepository.save(builder.build());
                count++;
            }
        }

        log.info("✅ {} syndicats créés", count);

        return ResponseEntity.ok(ApiResponse.success(count + " syndicats créés", Map.of(
                "count", count,
                "defaultPassword", "password123"
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EMPLOYÉS
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-employes")
    @Operation(summary = "Initialiser les employés de test")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedEmployes() {
        log.info("👷 Initialisation employés...");
        int count = 0;

        // Récupérer les pharmacies
        List<Pharmacie> pharmacies = pharmacieRepository.findAll();
        if (pharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune pharmacie trouvée. Exécutez d'abord /seed-pharmacies")
            );
        }

        Object[][] employesData = {
                {"DIALLO", "Aissatou", "+221771111111", "aissatou.diallo", "Vendeuse"},
                {"BA", "Mamadou", "+221772222222", "mamadou.ba", "Vendeur"},
                {"SECK", "Fatima", "+221773333333", "fatima.seck", "Caissière"},
                {"FAYE", "Ousmane", "+221774444444", "ousmane.faye", "Assistant"},
                {"GUEYE", "Mariama", "+221775555555", "mariama.gueye", "Vendeuse Senior"},
                {"DIOP", "Ibou", "+221776666666", "ibou.diop", "Vendeur"},
                {"NIANG", "Coumba", "+221777777777", "coumba.niang", "Caissière"},
                {"MBENGUE", "Cheikh", "+221778888888", "cheikh.mbengue", "Assistant"}
        };

        int pharmacieIndex = 0;
        int codeCounter = 1;

        for (Object[] data : employesData) {
            String username = (String) data[3];
            if (!employeRepository.existsByUsername(username)) {
                Pharmacie pharmacie = pharmacies.get(pharmacieIndex % pharmacies.size());
                Pharmacien pharmacien = pharmacie.getPharmacienProprietaire();

                // Générer code unique
                String code = String.format("EMP-%s-%03d", pharmacie.getCode().substring(5, 10), codeCounter);

                // Permissions selon le poste
                Set<TypePermission> permissions = new HashSet<>();
                permissions.add(TypePermission.VENDRE);
                permissions.add(TypePermission.VOIR_STOCK);
                permissions.add(TypePermission.VOIR_CLIENTS);
                permissions.add(TypePermission.VOIR_MESSAGES);

                String poste = (String) data[4];
                if (poste.contains("Senior") || poste.contains("Caissière")) {
                    permissions.add(TypePermission.ANNULER_VENTE);
                    permissions.add(TypePermission.APPLIQUER_REMISE);
                    permissions.add(TypePermission.VOIR_RAPPORTS_BASIQUES);
                }
                if (poste.contains("Assistant")) {
                    permissions.add(TypePermission.MODIFIER_STOCK);
                    permissions.add(TypePermission.VOIR_ALERTES_STOCK);
                }

                Employe employe = Employe.builder()
                        .code(code)
                        .username(username)
                        .nom((String) data[0])
                        .prenom((String) data[1])
                        .telephone((String) data[2])
                        .email(username + "@sunufarmasi.sn")
                        .motDePasseHash(passwordEncoder.encode("password123"))
                        .doitChangerMotDePasse(true)
                        .pharmacie(pharmacie)
                        .pharmacien(pharmacien)
                        .poste(poste)
                        .dateEmbauche(LocalDate.now().minusMonths(count + 1))
                        .permissions(permissions)
                        .statut(StatutEmploye.ACTIF)
                        .sexe(count % 2 == 0 ? "F" : "M")
                        .build();

                employeRepository.save(employe);
                count++;
                codeCounter++;

                // Changer de pharmacie tous les 2 employés
                if (count % 2 == 0) {
                    pharmacieIndex++;
                }
            }
        }

        log.info("✅ {} employés créés", count);

        return ResponseEntity.ok(ApiResponse.success(count + " employés créés", Map.of(
                "count", count,
                "defaultPassword", "password123",
                "note", "Les employés doivent changer leur mot de passe à la première connexion"
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PLANNINGS ET GARDES
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-gardes")
    @Operation(summary = "Initialiser les plannings et gardes de test")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedGardes() {
        log.info("📅 Initialisation plannings et gardes...");
        int planningCount = 0;
        int gardeCount = 0;
        int skippedCount = 0;

        // Récupérer les syndicats
        List<Syndicat> syndicats = syndicatRepository.findAll();
        if (syndicats.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun syndicat trouvé. Exécutez d'abord /seed-syndicats")
            );
        }

        // Récupérer les pharmacies
        List<Pharmacie> pharmacies = pharmacieRepository.findAll();
        if (pharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune pharmacie trouvée. Exécutez d'abord /seed-pharmacies")
            );
        }

        // On utilise UN seul syndicat pour créer le planning (évite les doublons)
        Syndicat syndicat = syndicats.get(0);

        // Créer un planning pour le mois en cours
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        LocalDate finMois = debutMois.plusMonths(1).minusDays(1);

        String titrePlanning = "Gardes " + debutMois.getMonth().toString() + " " + debutMois.getYear() + " - " + syndicat.getSigle();

        // Créer le planning
        PlanningGarde planning = PlanningGarde.builder()
                .syndicat(syndicat)
                .titre(titrePlanning)
                .description("Planning des gardes pour " + syndicat.getNom())
                .dateDebut(debutMois)
                .dateFin(finMois)
                .statut(StatutPlanning.PUBLIE)
                .datePublication(LocalDateTime.now().minusDays(5))
                .publicationAuto(false)
                .notifierPharmacies(true)
                .notifierEmail(true)
                .notifierSms(false)
                .creeParNom("Admin Système")
                .build();

        planning = planningGardeRepository.save(planning);
        planningCount++;

        // Récupérer les communes pour les gardes
        List<Commune> communes = communeRepository.findAll();
        if (communes.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune commune trouvée. Exécutez d'abord /seed-localisation")
            );
        }

        // Créer des gardes par SEMAINE (samedi à vendredi)
        // Trouver le premier samedi du mois
        LocalDate currentSamedi = debutMois;
        while (currentSamedi.getDayOfWeek() != java.time.DayOfWeek.SATURDAY) {
            currentSamedi = currentSamedi.plusDays(1);
        }

        int pharmacieIndex = 0;
        int communeIndex = 0;

        // Créer des gardes pour chaque semaine du mois
        while (currentSamedi.isBefore(finMois.plusDays(7))) {
            LocalDate debutSemaine = currentSamedi;
            LocalDate finSemaine = currentSamedi.plusDays(6); // Vendredi

            // Pour chaque commune, créer une garde de semaine
            for (int i = 0; i < Math.min(3, communes.size()); i++) {
                Commune commune = communes.get((communeIndex + i) % communes.size());
                Pharmacie pharmacie = pharmacies.get((pharmacieIndex + i) % pharmacies.size());

                // Vérifier si cette garde existe déjà
                boolean gardeExiste = gardeRepository.existsBySemaineAndCommuneAndType(
                        debutSemaine, finSemaine, commune.getId(), TypeGarde.JOUR_ET_NUIT);

                if (!gardeExiste) {
                    // Déterminer le statut selon la période
                    StatutGarde statutGarde;
                    LocalDate today = LocalDate.now();
                    if (finSemaine.isBefore(today)) {
                        statutGarde = StatutGarde.TERMINEE;
                    } else if (!debutSemaine.isAfter(today) && !finSemaine.isBefore(today)) {
                        statutGarde = StatutGarde.EN_COURS;
                    } else {
                        statutGarde = StatutGarde.PLANIFIEE;
                    }

                    Garde garde = Garde.builder()
                            .planning(planning)
                            .pharmacie(pharmacie)
                            .dateDebut(debutSemaine)
                            .dateFin(finSemaine)
                            .numeroSemaine(debutSemaine.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()))
                            .commune(commune)
                            .zoneNom(commune.getNom())
                            .typeGarde(TypeGarde.JOUR_ET_NUIT)
                            .statut(statutGarde)
                            .confirmeParPharmacie(true)
                            .dateConfirmation(LocalDateTime.now().minusDays(10))
                            .notificationEnvoyee(true)
                            .latitude(pharmacie.getLatitude())
                            .longitude(pharmacie.getLongitude())
                            .build();

                    gardeRepository.save(garde);
                    gardeCount++;
                } else {
                    skippedCount++;
                }
            }

            // Passer à la semaine suivante
            currentSamedi = currentSamedi.plusDays(7);
            pharmacieIndex++;
            communeIndex++;
        }

        log.info("✅ {} plannings et {} gardes créés ({} ignorés car existants)", planningCount, gardeCount, skippedCount);

        return ResponseEntity.ok(ApiResponse.success(planningCount + " plannings et " + gardeCount + " gardes créés", Map.of(
                "plannings", planningCount,
                "gardes", gardeCount,
                "ignores", skippedCount
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRODUITS ET STOCK
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-stock")
    @Operation(summary = "Initialiser les produits et le stock de test")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedStock() {
        log.info("💊 Initialisation produits et stock...");
        int produitCount = 0;
        int stockCount = 0;

        // Récupérer les pharmacies
        List<Pharmacie> pharmacies = pharmacieRepository.findAll();
        if (pharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune pharmacie trouvée. Exécutez d'abord /seed-pharmacies")
            );
        }

        // Données des produits (médicaments courants au Sénégal)
        Object[][] produitsData = {
                // Code, Nom, DCI, Catégorie, Forme, Dosage, Labo, Prix TTC, Sur ordonnance
                {"PRD-00001", "Doliprane", "Paracétamol", CategorieProduit.MEDICAMENT, FormeProduit.COMPRIME, "500mg", "Sanofi", 1500, false},
                {"PRD-00002", "Efferalgan", "Paracétamol", CategorieProduit.MEDICAMENT, FormeProduit.COMPRIME, "1000mg", "UPSA", 2500, false},
                {"PRD-00003", "Amoxicilline Sandoz", "Amoxicilline", CategorieProduit.ORDONNANCE, FormeProduit.GELULE, "500mg", "Sandoz", 3500, true},
                {"PRD-00004", "Augmentin", "Amoxicilline + Acide clavulanique", CategorieProduit.ORDONNANCE, FormeProduit.COMPRIME, "1g", "GSK", 8500, true},
                {"PRD-00005", "Ibuprofen Mylan", "Ibuprofène", CategorieProduit.MEDICAMENT, FormeProduit.COMPRIME, "400mg", "Mylan", 2000, false},
                {"PRD-00006", "Voltarène Emulgel", "Diclofénac", CategorieProduit.MEDICAMENT, FormeProduit.GEL, "1%", "Novartis", 4500, false},
                {"PRD-00007", "Maalox", "Hydroxyde d'aluminium + Magnésium", CategorieProduit.OTC, FormeProduit.SUSPENSION_BUVABLE, "200ml", "Sanofi", 3000, false},
                {"PRD-00008", "Smecta", "Diosmectite", CategorieProduit.OTC, FormeProduit.SACHET, "3g", "Ipsen", 2500, false},
                {"PRD-00009", "Spasfon", "Phloroglucinol", CategorieProduit.MEDICAMENT, FormeProduit.COMPRIME, "80mg", "Teva", 3500, false},
                {"PRD-00010", "Ventoline", "Salbutamol", CategorieProduit.ORDONNANCE, FormeProduit.AEROSOL, "100µg", "GSK", 5500, true},
                {"PRD-00011", "Toplexil", "Oxomémazine", CategorieProduit.MEDICAMENT, FormeProduit.SIROP, "150ml", "Sanofi", 4000, false},
                {"PRD-00012", "Rhinofluimucil", "Acétylcystéine + Tuaminoheptane", CategorieProduit.OTC, FormeProduit.SPRAY_NASAL, "10ml", "Zambon", 5000, false},
                {"PRD-00013", "Collyre Bleu", "Méthylthioninium", CategorieProduit.OTC, FormeProduit.COLLYRE, "10ml", "Faure", 2000, false},
                {"PRD-00014", "Bétadine", "Povidone iodée", CategorieProduit.PARAMEDICAL, FormeProduit.SOLUTION_BUVABLE, "125ml", "Mylan", 3500, false},
                {"PRD-00015", "Biseptine", "Chlorhexidine", CategorieProduit.PARAMEDICAL, FormeProduit.SPRAY_CUTANE, "100ml", "Bayer", 4500, false},
                {"PRD-00016", "Vitamine C 500", "Acide ascorbique", CategorieProduit.NUTRITION, FormeProduit.COMPRIME, "500mg", "UPSA", 3000, false},
                {"PRD-00017", "Mag 2", "Magnésium", CategorieProduit.NUTRITION, FormeProduit.AMPOULE_BUVABLE, "122mg", "Sanofi", 5500, false},
                {"PRD-00018", "Fervex", "Paracétamol + Phéniramine", CategorieProduit.OTC, FormeProduit.SACHET, "500mg", "UPSA", 4000, false},
                {"PRD-00019", "Nurofen", "Ibuprofène", CategorieProduit.MEDICAMENT, FormeProduit.COMPRIME, "200mg", "Reckitt", 2500, false},
                {"PRD-00020", "Strepsils", "Amylmétacrésol + Alcool dichlorobenzylique", CategorieProduit.OTC, FormeProduit.PASTILLE, "24pcs", "Reckitt", 3500, false}
        };

        // Créer les produits
        for (Object[] data : produitsData) {
            String code = (String) data[0];
            if (!produitRepository.existsByCode(code)) {
                Produit produit = Produit.builder()
                        .code(code)
                        .codeBarre("3400930" + code.substring(4)) // Code barre fictif
                        .nom((String) data[1])
                        .dci((String) data[2])
                        .categorie((CategorieProduit) data[3])
                        .forme((FormeProduit) data[4])
                        .dosage((String) data[5])
                        .laboratoire((String) data[6])
                        .prixPublicTTC(new BigDecimal((Integer) data[7]))
                        .prixAchatHT(new BigDecimal((Integer) data[7]).multiply(new BigDecimal("0.7"))) // Marge 30%
                        .tauxTVA(BigDecimal.ZERO) // Pas de TVA sur médicaments au Sénégal
                        .surOrdonnance((Boolean) data[8])
                        .estGenerique(code.contains("Sandoz") || code.contains("Mylan"))
                        .estRemboursable(true)
                        .uniteVente(UniteVente.BOITE)
                        .contenance(20)
                        .statut(StatutProduit.ACTIF)
                        .build();

                produitRepository.save(produit);
                produitCount++;
            }
        }

        // Créer le stock pour chaque pharmacie
        List<Produit> produits = produitRepository.findAll();
        Random random = new Random(42); // Seed fixe pour reproductibilité

        for (Pharmacie pharmacie : pharmacies) {
            for (Produit produit : produits) {
                // Vérifier si le stock existe déjà
                if (!produitPharmacieRepository.existsByPharmacieIdAndProduitId(pharmacie.getId(), produit.getId())) {
                    int quantite = 10 + random.nextInt(90); // Entre 10 et 100
                    int seuilAlerte = 5 + random.nextInt(10);

                    ProduitPharmacie stock = ProduitPharmacie.builder()
                            .pharmacie(pharmacie)
                            .produit(produit)
                            .quantiteStock(quantite)
                            .quantiteReservee(0)
                            .seuilAlerte(seuilAlerte)
                            .seuilReappro(seuilAlerte * 2)
                            .quantiteOptimale(seuilAlerte * 5)
                            .datePeremptionProche(LocalDate.now().plusMonths(6 + random.nextInt(18)))
                            .alertePeremptionJours(90)
                            .prixVenteTTC(produit.getPrixPublicTTC())
                            .prixAchatHT(produit.getPrixAchatHT())
                            .margePourcentage(new BigDecimal("30"))
                            .emplacement("Rayon " + (char)('A' + random.nextInt(5)) + " - Étagère " + (1 + random.nextInt(4)))
                            .estActif(true)
                            .venteAutorisee(true)
                            .commandeAuto(false)
                            .totalVendu(random.nextInt(50))
                            .build();

                    produitPharmacieRepository.save(stock);
                    stockCount++;

                    // Créer un mouvement d'initialisation
                    MouvementStock mouvement = MouvementStock.builder()
                            .pharmacie(pharmacie)
                            .produit(produit)
                            .produitPharmacie(stock)
                            .type(TypeMouvement.INITIALISATION)
                            .quantite(quantite)
                            .quantiteAvant(0)
                            .quantiteApres(quantite)
                            .prixUnitaire(produit.getPrixAchatHT())
                            .montantTotal(produit.getPrixAchatHT().multiply(BigDecimal.valueOf(quantite)))
                            .numeroLot("LOT" + LocalDate.now().getYear() + String.format("%04d", random.nextInt(10000)))
                            .datePeremption(stock.getDatePeremptionProche())
                            .motif("Stock initial")
                            .dateMouvement(LocalDateTime.now().minusDays(30))
                            .effectueParNom("Admin Système")
                            .effectueParRole("ADMIN")
                            .build();

                    mouvementStockRepository.save(mouvement);
                }
            }
        }

        log.info("✅ {} produits et {} stocks créés", produitCount, stockCount);

        return ResponseEntity.ok(ApiResponse.success(produitCount + " produits et " + stockCount + " stocks créés", Map.of(
                "produits", produitCount,
                "stocks", stockCount,
                "pharmacies", pharmacies.size()
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/stats")
    @Operation(summary = "Statistiques des données")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("pays", paysRepository.count());
        stats.put("regions", regionRepository.count());
        stats.put("departements", departementRepository.count());
        stats.put("communes", communeRepository.count());
        stats.put("users", userRepository.count());
        stats.put("pharmaciens", pharmacienRepository.count());
        stats.put("pharmacies", pharmacieRepository.count());
        stats.put("syndicats", syndicatRepository.count());
        stats.put("employes", employeRepository.count());
        stats.put("plannings", planningGardeRepository.count());
        stats.put("gardes", gardeRepository.count());
        stats.put("produits", produitRepository.count());
        stats.put("stocks", produitPharmacieRepository.count());
        stats.put("mouvements", mouvementStockRepository.count());

        stats.put("subscription_plans", subscriptionPlanRepository.count());
        stats.put("patients", patientRepository.count());
        stats.put("subscriptions", subscriptionRepository.count());
        stats.put("payments", paymentRepository.count());

        stats.put("mutuelles", mutuelleRepository.count());
        stats.put("adherents_mutuelle", adherentRepository.count());
        stats.put("contrats_mutuelle", contratMutuelleRepository.count());

        stats.put("ventes", venteRepository.count());
        stats.put("lignes_vente", ligneVenteRepository.count());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "Lister les utilisateurs")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId().toString(),
                        "email", user.getEmail(),
                        "nom", user.getNomComplet(),
                        "role", user.getRole().name(),
                        "statut", user.getStatut().name()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/pharmaciens")
    @Operation(summary = "Lister les pharmaciens")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPharmaciens() {
        List<Map<String, Object>> pharmaciens = pharmacienRepository.findAll().stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId().toString(),
                        "nom", p.getNomComplet(),
                        "telephone", p.getTelephone(),
                        "email", p.getEmail() != null ? p.getEmail() : "",
                        "plan", p.getPlan().name(),
                        "nbPharmacies", p.getNombrePharmaciesActuelles()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(pharmaciens));
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "Lister les pharmacies")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPharmacies() {
        List<Map<String, Object>> pharmacies = pharmacieRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId().toString());
                    map.put("code", p.getCode());
                    map.put("nom", p.getNom());
                    map.put("telephone", p.getTelephone());
                    map.put("quartier", p.getQuartier() != null ? p.getQuartier() : "");
                    map.put("statut", p.getStatut().name());
                    // Accéder au pharmacien dans la transaction
                    Pharmacien prop = p.getPharmacienProprietaire();
                    map.put("proprietaire", prop != null ? prop.getNomComplet() : "N/A");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(pharmacies));
    }

    @GetMapping("/syndicats")
    @Operation(summary = "Lister les syndicats")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSyndicats() {
        List<Map<String, Object>> syndicats = syndicatRepository.findAll().stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId().toString());
                    map.put("code", s.getCode());
                    map.put("nom", s.getNom());
                    map.put("sigle", s.getSigle() != null ? s.getSigle() : "");
                    map.put("username", s.getUsername());
                    map.put("type", s.getType().name());
                    map.put("telephone", s.getTelephone());
                    map.put("statut", s.getStatut().name());
                    map.put("zone", s.getNomZone() != null ? s.getNomZone() : "N/A");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(syndicats));
    }

    @GetMapping("/employes")
    @Operation(summary = "Lister les employés")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listEmployes() {
        List<Map<String, Object>> employes = employeRepository.findAll().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", e.getId().toString());
                    map.put("code", e.getCode());
                    map.put("nom", e.getNomComplet());
                    map.put("username", e.getUsername());
                    map.put("telephone", e.getTelephone());
                    map.put("poste", e.getPoste() != null ? e.getPoste() : "");
                    map.put("statut", e.getStatut().name());
                    // Accéder à la pharmacie dans la transaction
                    Pharmacie pharmacie = e.getPharmacie();
                    map.put("pharmacie", pharmacie != null ? pharmacie.getNom() : "N/A");
                    map.put("permissions", e.getPermissions().size());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(employes));
    }

    @GetMapping("/plannings")
    @Operation(summary = "Lister les plannings de garde")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPlannings() {
        List<Map<String, Object>> plannings = planningGardeRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId().toString());
                    map.put("titre", p.getTitre());
                    map.put("dateDebut", p.getDateDebut().toString());
                    map.put("dateFin", p.getDateFin().toString());
                    map.put("statut", p.getStatut().name());
                    map.put("nombreGardes", p.getGardes().size());
                    // Accéder au syndicat dans la transaction
                    Syndicat syndicat = p.getSyndicat();
                    map.put("syndicat", syndicat != null ? syndicat.getNom() : "N/A");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(plannings));
    }

    @GetMapping("/gardes")
    @Operation(summary = "Lister les gardes (semaines en cours et à venir)")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listGardes() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> gardes = gardeRepository.findAll().stream()
                .filter(g -> !g.getDateFin().isBefore(today.minusDays(14))) // 2 dernières semaines + futur
                .sorted((a, b) -> a.getDateDebut().compareTo(b.getDateDebut()))
                .limit(50)
                .map(g -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", g.getId().toString());
                    map.put("dateDebut", g.getDateDebut().toString());
                    map.put("dateFin", g.getDateFin().toString());
                    map.put("numeroSemaine", g.getNumeroSemaine());
                    map.put("zone", g.getZoneNom());
                    map.put("type", g.getTypeGarde().getLibelle());
                    map.put("statut", g.getStatut().name());
                    map.put("estSemaineEnCours", g.estSemaineEnCours());
                    // Accéder à la pharmacie dans la transaction
                    Pharmacie pharmacie = g.getPharmacie();
                    map.put("pharmacie", pharmacie != null ? pharmacie.getNom() : "N/A");
                    map.put("quartier", pharmacie != null && pharmacie.getQuartier() != null ? pharmacie.getQuartier() : "");
                    map.put("telephone", pharmacie != null ? pharmacie.getTelephone() : "");
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(gardes));
    }

    @GetMapping("/produits")
    @Operation(summary = "Lister les produits du catalogue")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listProduits() {
        List<Map<String, Object>> produits = produitRepository.findAll().stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId().toString());
                    map.put("code", p.getCode());
                    map.put("nom", p.getNom());
                    map.put("dci", p.getDci() != null ? p.getDci() : "");
                    map.put("categorie", p.getCategorie().name());
                    map.put("forme", p.getForme() != null ? p.getForme().getLibelle() : "");
                    map.put("dosage", p.getDosage() != null ? p.getDosage() : "");
                    map.put("laboratoire", p.getLaboratoire() != null ? p.getLaboratoire() : "");
                    map.put("prixTTC", p.getPrixPublicTTC());
                    map.put("surOrdonnance", p.getSurOrdonnance());
                    map.put("statut", p.getStatut().name());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(produits));
    }

    @GetMapping("/stocks")
    @Operation(summary = "Lister les stocks par pharmacie")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listStocks(
            @RequestParam(required = false) UUID pharmacieId
    ) {
        var stream = produitPharmacieRepository.findAll().stream();

        if (pharmacieId != null) {
            stream = stream.filter(s -> s.getPharmacie().getId().equals(pharmacieId));
        }

        List<Map<String, Object>> stocks = stream
                .limit(100)
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId().toString());
                    // Accéder aux relations dans la transaction
                    Pharmacie pharmacie = s.getPharmacie();
                    Produit produit = s.getProduit();
                    map.put("pharmacie", pharmacie != null ? pharmacie.getNom() : "N/A");
                    map.put("pharmacieId", pharmacie != null ? pharmacie.getId().toString() : "");
                    map.put("produit", produit != null ? produit.getNom() : "N/A");
                    map.put("produitCode", produit != null ? produit.getCode() : "");
                    map.put("quantite", s.getQuantiteStock());
                    map.put("seuilAlerte", s.getSeuilAlerte());
                    map.put("prixVente", s.getPrixVenteTTC());
                    map.put("emplacement", s.getEmplacementComplet());
                    map.put("stockBas", s.stockBas());
                    map.put("enRupture", s.enRupture());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(stocks));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RECHERCHE PHARMACIES DE GARDE PAR SEMAINE ET ZONE
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/gardes/recherche")
    @Operation(summary = "Rechercher les pharmacies de garde par semaine et zone (commune ou département)")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> rechercheGardes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId
    ) {
        log.info("🔍 Recherche gardes: debut={}, fin={}, commune={}, dept={}", dateDebut, dateFin, communeId, departementId);

        // Si pas de dates, prendre la semaine en cours (samedi à vendredi)
        if (dateDebut == null) {
            dateDebut = getSamediSemaine(LocalDate.now());
        }
        if (dateFin == null) {
            dateFin = dateDebut.plusDays(6); // Vendredi
        }

        List<Garde> gardes;

        if (communeId != null) {
            gardes = gardeRepository.findBySemaineAndCommune(dateDebut, dateFin, communeId);
        } else if (departementId != null) {
            gardes = gardeRepository.findBySemaineAndDepartement(dateDebut, dateFin, departementId);
        } else {
            // Toutes les gardes de la période
            LocalDate finalDateFin1 = dateFin;
            LocalDate finalDateDebut1 = dateDebut;
            gardes = gardeRepository.findAll().stream()
                    .filter(g -> !g.getDateDebut().isAfter(finalDateFin1) && !g.getDateFin().isBefore(finalDateDebut1))
                    .toList();
        }

        final LocalDate finalDateDebut = dateDebut;
        final LocalDate finalDateFin = dateFin;

        List<Map<String, Object>> resultats = gardes.stream()
                .map(g -> {
                    Pharmacie pharmacie = g.getPharmacie();
                    Map<String, Object> map = new HashMap<>();
                    map.put("gardeId", g.getId().toString());
                    map.put("dateDebut", g.getDateDebut().toString());
                    map.put("dateFin", g.getDateFin().toString());
                    map.put("numeroSemaine", g.getNumeroSemaine());
                    map.put("typeGarde", g.getTypeGarde().getLibelle());
                    map.put("statut", g.getStatut().name());
                    map.put("zone", g.getZoneNom());

                    // Infos pharmacie
                    map.put("pharmacieId", pharmacie.getId().toString());
                    map.put("pharmacieNom", pharmacie.getNom());
                    map.put("adresse", pharmacie.getAdresseComplete());
                    map.put("quartier", pharmacie.getQuartier());
                    map.put("telephone", pharmacie.getTelephone());
                    map.put("latitude", pharmacie.getLatitude());
                    map.put("longitude", pharmacie.getLongitude());

                    // États
                    map.put("estSemaineEnCours", g.estSemaineEnCours());
                    map.put("confirme", g.getConfirmeParPharmacie());

                    return map;
                })
                .toList();

        // Infos zone
        String nomZone = "Toutes les zones";
        if (communeId != null) {
            nomZone = communeRepository.findById(communeId)
                    .map(Commune::getNom).orElse("Commune inconnue");
        } else if (departementId != null) {
            nomZone = departementRepository.findById(departementId)
                    .map(Departement::getNom).orElse("Département inconnu");
        }

        return ResponseEntity.ok(ApiResponse.success(resultats.size() + " pharmacie(s) de garde trouvée(s)", Map.of(
                "periode", Map.of(
                        "dateDebut", finalDateDebut.toString(),
                        "dateFin", finalDateFin.toString(),
                        "libelle", "Du " + finalDateDebut + " au " + finalDateFin
                ),
                "zone", nomZone,
                "nombreResultats", resultats.size(),
                "gardes", resultats
        )));
    }

    @GetMapping("/gardes/semaine-courante")
    @Operation(summary = "Pharmacies de garde cette semaine")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> gardesSemaineCourante(
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId
    ) {
        LocalDate samedi = getSamediSemaine(LocalDate.now());
        LocalDate vendredi = samedi.plusDays(6);
        return rechercheGardes(samedi, vendredi, communeId, departementId);
    }

    @GetMapping("/gardes/semaine-prochaine")
    @Operation(summary = "Pharmacies de garde la semaine prochaine")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> gardesSemaineProchaine(
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId
    ) {
        LocalDate samediProchain = getSamediSemaine(LocalDate.now()).plusDays(7);
        LocalDate vendrediProchain = samediProchain.plusDays(6);
        return rechercheGardes(samediProchain, vendrediProchain, communeId, departementId);
    }

    @GetMapping("/gardes/par-date")
    @Operation(summary = "Pharmacies de garde pour une date spécifique")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> gardesParDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID communeId,
            @RequestParam(required = false) UUID departementId
    ) {
        log.info("🔍 Gardes pour date: {}, commune={}, dept={}", date, communeId, departementId);

        List<Garde> gardes;

        if (communeId != null) {
            gardes = gardeRepository.findByDateAndCommune(date, communeId);
        } else if (departementId != null) {
            gardes = gardeRepository.findByDateAndDepartement(date, departementId);
        } else {
            // Toutes les gardes contenant cette date
            gardes = gardeRepository.findAll().stream()
                    .filter(g -> g.contientDate(date))
                    .toList();
        }

        List<Map<String, Object>> resultats = gardes.stream()
                .map(g -> {
                    Pharmacie pharmacie = g.getPharmacie();
                    Map<String, Object> map = new HashMap<>();
                    map.put("gardeId", g.getId().toString());
                    map.put("periode", g.getPeriodeLibelle());
                    map.put("zone", g.getZoneNom());
                    map.put("pharmacieNom", pharmacie.getNom());
                    map.put("adresse", pharmacie.getAdresseComplete());
                    map.put("telephone", pharmacie.getTelephone());
                    map.put("latitude", pharmacie.getLatitude());
                    map.put("longitude", pharmacie.getLongitude());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(resultats.size() + " pharmacie(s) de garde", Map.of(
                "date", date.toString(),
                "gardes", resultats
        )));
    }

    /**
     * Calculer le samedi de la semaine contenant une date
     * Au Sénégal, la semaine de garde va de samedi à vendredi
     */
    private LocalDate getSamediSemaine(LocalDate date) {
        java.time.DayOfWeek jour = date.getDayOfWeek();
        if (jour == java.time.DayOfWeek.SATURDAY) {
            return date;
        } else if (jour == java.time.DayOfWeek.SUNDAY) {
            return date.minusDays(1);
        } else {
            // Lundi à Vendredi : retourner le samedi précédent
            return date.minusDays(jour.getValue() + 1);
        }
    }

    @GetMapping("/pharmacies/{pharmacieId}/mon-stock")
    @Operation(summary = "Voir tout le stock d'une pharmacie avec détails produits")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStockPharmacie(
            @PathVariable UUID pharmacieId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean stockBas,
            @RequestParam(required = false) Boolean enRupture
    ) {
        log.info("📦 Stock pharmacie: {}", pharmacieId);

        // Vérifier que la pharmacie existe
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId).orElse(null);
        if (pharmacie == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Pharmacie non trouvée"));
        }

        // Récupérer le stock
        List<ProduitPharmacie> stockList;
        if (search != null && !search.isBlank()) {
            stockList = produitPharmacieRepository.searchInPharmacie(pharmacieId, search);
        } else {
            stockList = produitPharmacieRepository.findByPharmacieIdAndEstActif(pharmacieId, true);
        }

        // Filtrer si demandé
        var stream = stockList.stream();
        if (Boolean.TRUE.equals(stockBas)) {
            stream = stream.filter(ProduitPharmacie::stockBas);
        }
        if (Boolean.TRUE.equals(enRupture)) {
            stream = stream.filter(ProduitPharmacie::enRupture);
        }

        List<Map<String, Object>> produits = stream
                .map(s -> {
                    Produit p = s.getProduit();
                    Map<String, Object> map = new HashMap<>();
                    map.put("stockId", s.getId().toString());
                    map.put("produitId", p.getId().toString());
                    map.put("code", p.getCode());
                    map.put("codeBarre", p.getCodeBarre());
                    map.put("nom", p.getNom());
                    map.put("dci", p.getDci());
                    map.put("forme", p.getForme() != null ? p.getForme().getLibelle() : "");
                    map.put("dosage", p.getDosage());
                    map.put("categorie", p.getCategorie().getLibelle());
                    map.put("laboratoire", p.getLaboratoire());
                    map.put("surOrdonnance", p.getSurOrdonnance());

                    // Info stock
                    map.put("quantiteStock", s.getQuantiteStock());
                    map.put("quantiteDisponible", s.getQuantiteDisponible());
                    map.put("quantiteReservee", s.getQuantiteReservee());
                    map.put("seuilAlerte", s.getSeuilAlerte());
                    map.put("seuilReappro", s.getSeuilReappro());

                    // Prix
                    map.put("prixVenteTTC", s.getPrixVenteTTC());
                    map.put("prixAchatHT", s.getPrixAchatHT());
                    map.put("margePourcentage", s.getMargePourcentage());

                    // Péremption
                    map.put("datePeremption", s.getDatePeremptionProche() != null ?
                            s.getDatePeremptionProche().toString() : null);
                    map.put("emplacement", s.getEmplacementComplet());

                    // Alertes
                    map.put("stockBas", s.stockBas());
                    map.put("enRupture", s.enRupture());
                    map.put("bientotPerime", s.bientotPerime());
                    map.put("estPerime", s.estPerime());

                    // Stats
                    map.put("totalVendu", s.getTotalVendu());
                    map.put("derniereVente", s.getDerniereVente() != null ?
                            s.getDerniereVente().toString() : null);

                    return map;
                })
                .toList();

        // Statistiques globales
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProduits", produits.size());
        stats.put("enRupture", produits.stream().filter(p -> (Boolean) p.get("enRupture")).count());
        stats.put("stockBas", produits.stream().filter(p -> (Boolean) p.get("stockBas")).count());
        stats.put("bientotPerimes", produits.stream().filter(p -> (Boolean) p.get("bientotPerime")).count());
        stats.put("valeurStock", produitPharmacieRepository.calculerValeurStock(pharmacieId));

        return ResponseEntity.ok(ApiResponse.success("Stock de " + pharmacie.getNom(), Map.of(
                "pharmacie", Map.of(
                        "id", pharmacie.getId().toString(),
                        "nom", pharmacie.getNom(),
                        "adresse", pharmacie.getAdresseComplete() != null ? pharmacie.getAdresseComplete() : "",
                        "telephone", pharmacie.getTelephone()
                ),
                "stats", stats,
                "produits", produits
        )));
    }

    @GetMapping("/pharmacies/{pharmacieId}/alertes-stock")
    @Operation(summary = "Alertes de stock d'une pharmacie (ruptures, stock bas, péremptions)")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAlertesStock(@PathVariable UUID pharmacieId) {
        log.info("⚠️ Alertes stock pharmacie: {}", pharmacieId);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId).orElse(null);
        if (pharmacie == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Pharmacie non trouvée"));
        }

        // Ruptures
        List<Map<String, Object>> ruptures = produitPharmacieRepository.findEnRupture(pharmacieId).stream()
                .map(s -> Map.<String, Object>of(
                        "produit", s.getProduit().getNom(),
                        "code", s.getProduit().getCode(),
                        "seuilReappro", s.getSeuilReappro()
                ))
                .toList();

        // Stock bas
        List<Map<String, Object>> stockBas = produitPharmacieRepository.findStockBas(pharmacieId).stream()
                .map(s -> Map.<String, Object>of(
                        "produit", s.getProduit().getNom(),
                        "code", s.getProduit().getCode(),
                        "quantite", s.getQuantiteStock(),
                        "seuil", s.getSeuilAlerte()
                ))
                .toList();

        // Bientôt périmés (dans 90 jours)
        LocalDate dans90Jours = LocalDate.now().plusDays(90);
        List<Map<String, Object>> bientotPerimes = produitPharmacieRepository
                .findBientotPerimes(pharmacieId, LocalDate.now(), dans90Jours).stream()
                .map(s -> Map.<String, Object>of(
                        "produit", s.getProduit().getNom(),
                        "code", s.getProduit().getCode(),
                        "quantite", s.getQuantiteStock(),
                        "datePeremption", s.getDatePeremptionProche().toString(),
                        "joursRestants", java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), s.getDatePeremptionProche())
                ))
                .toList();

        // Périmés
        List<Map<String, Object>> perimes = produitPharmacieRepository
                .findPerimes(pharmacieId, LocalDate.now()).stream()
                .map(s -> Map.<String, Object>of(
                        "produit", s.getProduit().getNom(),
                        "code", s.getProduit().getCode(),
                        "quantite", s.getQuantiteStock(),
                        "datePeremption", s.getDatePeremptionProche().toString()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Alertes stock", Map.of(
                "pharmacie", pharmacie.getNom(),
                "resume", Map.of(
                        "ruptures", ruptures.size(),
                        "stockBas", stockBas.size(),
                        "bientotPerimes", bientotPerimes.size(),
                        "perimes", perimes.size()
                ),
                "ruptures", ruptures,
                "stockBas", stockBas,
                "bientotPerimes", bientotPerimes,
                "perimes", perimes
        )));
    }

    @GetMapping("/pharmacies/{pharmacieId}/produit/{codeOuCodeBarre}")
    @Operation(summary = "Chercher un produit par code ou code-barre dans une pharmacie")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProduitPharmacie(
            @PathVariable UUID pharmacieId,
            @PathVariable String codeOuCodeBarre
    ) {
        log.info("🔍 Recherche produit {} dans pharmacie {}", codeOuCodeBarre, pharmacieId);

        // Chercher par code d'abord
        var stockOpt = produitPharmacieRepository.findByPharmacieIdAndProduitCode(pharmacieId, codeOuCodeBarre);

        // Si pas trouvé, chercher par code-barre
        if (stockOpt.isEmpty()) {
            stockOpt = produitPharmacieRepository.findByPharmacieIdAndCodeBarre(pharmacieId, codeOuCodeBarre);
        }

        if (stockOpt.isEmpty()) {
            // Vérifier si le produit existe dans le catalogue mais pas dans cette pharmacie
            var produitOpt = produitRepository.findByCode(codeOuCodeBarre);
            if (produitOpt.isEmpty()) {
                produitOpt = produitRepository.findByCodeBarre(codeOuCodeBarre);
            }

            if (produitOpt.isPresent()) {
                Produit p = produitOpt.get();
                return ResponseEntity.ok(ApiResponse.success("Produit trouvé mais non référencé dans cette pharmacie", Map.of(
                        "existe", true,
                        "enStock", false,
                        "produit", Map.of(
                                "id", p.getId().toString(),
                                "code", p.getCode(),
                                "nom", p.getNom(),
                                "prixPublic", p.getPrixPublicTTC()
                        ),
                        "message", "Ce produit existe dans le catalogue mais n'est pas encore ajouté au stock de cette pharmacie"
                )));
            }

            return ResponseEntity.ok(ApiResponse.error("Produit non trouvé"));
        }

        ProduitPharmacie s = stockOpt.get();
        Produit p = s.getProduit();

        Map<String, Object> result = new HashMap<>();
        result.put("existe", true);
        result.put("enStock", s.getQuantiteStock() > 0);
        result.put("stockId", s.getId().toString());
        result.put("produitId", p.getId().toString());
        result.put("code", p.getCode());
        result.put("codeBarre", p.getCodeBarre());
        result.put("nom", p.getNom());
        result.put("dci", p.getDci());
        result.put("forme", p.getForme() != null ? p.getForme().getLibelle() : "");
        result.put("dosage", p.getDosage());
        result.put("categorie", p.getCategorie().getLibelle());
        result.put("surOrdonnance", p.getSurOrdonnance());
        result.put("quantiteStock", s.getQuantiteStock());
        result.put("quantiteDisponible", s.getQuantiteDisponible());
        result.put("prixVente", s.getPrixVenteTTC());
        result.put("emplacement", s.getEmplacementComplet());
        result.put("stockBas", s.stockBas());
        result.put("enRupture", s.enRupture());

        return ResponseEntity.ok(ApiResponse.success("Produit trouvé", result));
    }

    @GetMapping("/recherche-disponibilite")
    @Operation(summary = "Rechercher un médicament et sa disponibilité dans les pharmacies")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> rechercheDisponibilite(
            @RequestParam String q,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "10") Double rayonKm
    ) {
        log.info("🔍 Recherche disponibilité: {}", q);

        // Rechercher les produits correspondants
        List<Produit> produits = produitRepository.searchByNomOrDciOrCode(q);

        if (produits.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Aucun produit trouvé", Map.of(
                    "query", q,
                    "produits", List.of(),
                    "disponibilites", List.of()
            )));
        }

        // Pour chaque produit, trouver les pharmacies qui l'ont en stock
        List<Map<String, Object>> resultats = new java.util.ArrayList<>();

        for (Produit produit : produits) {
            Map<String, Object> produitInfo = new HashMap<>();
            produitInfo.put("id", produit.getId().toString());
            produitInfo.put("code", produit.getCode());
            produitInfo.put("nom", produit.getNom());
            produitInfo.put("dci", produit.getDci());
            produitInfo.put("forme", produit.getForme() != null ? produit.getForme().getLibelle() : "");
            produitInfo.put("dosage", produit.getDosage());
            produitInfo.put("laboratoire", produit.getLaboratoire());
            produitInfo.put("prixPublic", produit.getPrixPublicTTC());
            produitInfo.put("surOrdonnance", produit.getSurOrdonnance());

            // Trouver les stocks disponibles
            List<ProduitPharmacie> stocks = produitPharmacieRepository.findByProduitId(produit.getId());

            List<Map<String, Object>> disponibilites = stocks.stream()
                    .filter(s -> s.getQuantiteStock() != null && s.getQuantiteStock() > 0)
                    .filter(s -> s.getEstActif() != null && s.getEstActif())
                    .map(s -> {
                        Pharmacie pharmacie = s.getPharmacie();
                        Map<String, Object> dispo = new HashMap<>();
                        dispo.put("pharmacieId", pharmacie.getId().toString());
                        dispo.put("pharmacieNom", pharmacie.getNom());
                        dispo.put("adresse", pharmacie.getAdresseComplete());
                        dispo.put("quartier", pharmacie.getQuartier());
                        dispo.put("telephone", pharmacie.getTelephone());
                        dispo.put("latitude", pharmacie.getLatitude());
                        dispo.put("longitude", pharmacie.getLongitude());
                        dispo.put("quantiteDisponible", s.getQuantiteDisponible());
                        dispo.put("prixVente", s.getPrixVenteTTC());

                        // Calculer la distance si coordonnées fournies
                        if (latitude != null && longitude != null &&
                                pharmacie.getLatitude() != null && pharmacie.getLongitude() != null) {
                            double distance = calculerDistance(
                                    latitude, longitude,
                                    pharmacie.getLatitude(), pharmacie.getLongitude()
                            );
                            dispo.put("distanceKm", Math.round(distance * 100.0) / 100.0);
                        }

                        return dispo;
                    })
                    .sorted((a, b) -> {
                        // Trier par distance si disponible, sinon par quantité
                        if (a.containsKey("distanceKm") && b.containsKey("distanceKm")) {
                            return Double.compare((Double) a.get("distanceKm"), (Double) b.get("distanceKm"));
                        }
                        return Integer.compare((Integer) b.get("quantiteDisponible"), (Integer) a.get("quantiteDisponible"));
                    })
                    .filter(d -> {
                        // Filtrer par rayon si coordonnées fournies
                        if (latitude != null && longitude != null && d.containsKey("distanceKm")) {
                            return (Double) d.get("distanceKm") <= rayonKm;
                        }
                        return true;
                    })
                    .toList();

            produitInfo.put("nombrePharmacies", disponibilites.size());
            produitInfo.put("disponibilites", disponibilites);
            resultats.add(produitInfo);
        }

        return ResponseEntity.ok(ApiResponse.success(resultats.size() + " produit(s) trouvé(s)", Map.of(
                "query", q,
                "totalProduits", resultats.size(),
                "resultats", resultats
        )));
    }

    /**
     * Calcul de distance entre deux points GPS (formule Haversine)
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

    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser un mot de passe")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(
            @RequestParam String email,
            @RequestParam(defaultValue = "password123") String newPassword
    ) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok(ApiResponse.success("Password reset", Map.<String, Object>of(
                            "email", email,
                            "newPassword", newPassword
                    )));
                })
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Utilisateur non trouvé")));
    }

    @GetMapping("/hash")
    @Operation(summary = "Hasher un mot de passe")
    public ResponseEntity<Map<String, String>> hashPassword(
            @RequestParam(defaultValue = "password123") String password
    ) {
        return ResponseEntity.ok(Map.of(
                "password", password,
                "bcryptHash", passwordEncoder.encode(password)
        ));
    }


    /**
     * POST /api/v1/init/seed-plans
     * Initialiser les plans d'abonnement
     */
    @PostMapping("/seed-plans")
    @Transactional
    @Operation(summary = "Initialiser les plans d'abonnement")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedSubscriptionPlans() {
        log.info("POST /api/v1/init/seed-plans");

        List<Map<String, Object>> plansCreated = new ArrayList<>();

        // Plan 1 : Essai gratuit 15 jours
        SubscriptionPlan freeTrial = subscriptionPlanRepository.findByCode("FREE_TRIAL")
                .orElseGet(() -> {
                    SubscriptionPlan plan = SubscriptionPlan.builder()
                            .code("FREE_TRIAL")
                            .nom("Essai Gratuit")
                            .description("Période d'essai de 15 jours pour découvrir SunuFarmasi. Accès complet à toutes les fonctionnalités.")
                            .prix(0)
                            .dureeJours(15)
                            .avecPublicite(true)
                            .actif(true)
                            .ordre(1)
                            .build();
                    return subscriptionPlanRepository.save(plan);
                });
        plansCreated.add(Map.of(
                "code", freeTrial.getCode(),
                "nom", freeTrial.getNom(),
                "prix", freeTrial.getPrix(),
                "dureeJours", freeTrial.getDureeJours()
        ));

        // Plan 2 : Mensuel 750 FCFA
        SubscriptionPlan monthly = subscriptionPlanRepository.findByCode("MONTHLY")
                .orElseGet(() -> {
                    SubscriptionPlan plan = SubscriptionPlan.builder()
                            .code("MONTHLY")
                            .nom("Abonnement Mensuel")
                            .description("Abonnement mensuel avec publicités. Accès complet aux pharmacies de garde, recherche de médicaments, et favoris.")
                            .prix(750)
                            .dureeJours(30)
                            .avecPublicite(true)
                            .actif(true)
                            .ordre(2)
                            .build();
                    return subscriptionPlanRepository.save(plan);
                });
        plansCreated.add(Map.of(
                "code", monthly.getCode(),
                "nom", monthly.getNom(),
                "prix", monthly.getPrix(),
                "dureeJours", monthly.getDureeJours()
        ));

        // Plan 3 : Annuel 8000 FCFA
        SubscriptionPlan annual = subscriptionPlanRepository.findByCode("ANNUAL")
                .orElseGet(() -> {
                    SubscriptionPlan plan = SubscriptionPlan.builder()
                            .code("ANNUAL")
                            .nom("Abonnement Annuel")
                            .description("Abonnement annuel SANS publicités. Économisez 1000 FCFA par rapport au mensuel. Accès VIP à toutes les fonctionnalités.")
                            .prix(8000)
                            .dureeJours(365)
                            .avecPublicite(false)
                            .actif(true)
                            .ordre(3)
                            .build();
                    return subscriptionPlanRepository.save(plan);
                });
        plansCreated.add(Map.of(
                "code", annual.getCode(),
                "nom", annual.getNom(),
                "prix", annual.getPrix(),
                "dureeJours", annual.getDureeJours()
        ));

        Map<String, Object> result = Map.of(
                "message", "Plans d'abonnement initialisés",
                "total", plansCreated.size(),
                "plans", plansCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Plans créés avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED PATIENTS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-patients
     * Initialiser des patients de test
     */
    @PostMapping("/seed-patients")
    @Transactional
    @Operation(summary = "Initialiser les patients de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedPatients() {
        log.info("POST /api/v1/init/seed-patients");

        // Récupérer quelques communes
        List<Commune> communes = communeRepository.findAll();
        if (communes.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les localisations (seed-localisation)")
            );
        }

        List<Map<String, Object>> patientsCreated = new ArrayList<>();

        // Données des patients
        List<Map<String, String>> patientsData = List.of(
                Map.of("nom", "Amadou Diallo", "telephone", "+221771234501", "email", "amadou.diallo@gmail.com", "sexe", "M"),
                Map.of("nom", "Fatou Ndiaye", "telephone", "+221771234502", "email", "fatou.ndiaye@gmail.com", "sexe", "F"),
                Map.of("nom", "Moussa Sow", "telephone", "+221771234503", "email", "moussa.sow@gmail.com", "sexe", "M"),
                Map.of("nom", "Aïssatou Ba", "telephone", "+221771234504", "email", "aissatou.ba@gmail.com", "sexe", "F"),
                Map.of("nom", "Ibrahima Fall", "telephone", "+221771234505", "email", "ibrahima.fall@gmail.com", "sexe", "M"),
                Map.of("nom", "Mariama Diop", "telephone", "+221771234506", "email", "mariama.diop@gmail.com", "sexe", "F"),
                Map.of("nom", "Ousmane Mbaye", "telephone", "+221771234507", "email", "ousmane.mbaye@gmail.com", "sexe", "M"),
                Map.of("nom", "Khady Sarr", "telephone", "+221771234508", "email", "khady.sarr@gmail.com", "sexe", "F"),
                Map.of("nom", "Cheikh Gueye", "telephone", "+221771234509", "email", "cheikh.gueye@gmail.com", "sexe", "M"),
                Map.of("nom", "Aminata Faye", "telephone", "+221771234510", "email", "aminata.faye@gmail.com", "sexe", "F")
        );

        int communeIndex = 0;
        for (Map<String, String> data : patientsData) {
            String telephone = data.get("telephone");

            // Vérifier si le patient existe déjà
            if (patientRepository.findByTelephone(telephone).isPresent()) {
                log.info("Patient {} déjà existant", telephone);
                continue;
            }

            Commune commune = communes.get(communeIndex % communes.size());
            communeIndex++;

            Patient patient = Patient.builder()
                    .nomComplet(data.get("nom"))
                    .telephone(telephone)
                    .email(data.get("email"))
                    .sexe(data.get("sexe"))
                    .dateNaissance(LocalDate.of(1985 + (communeIndex % 20), (communeIndex % 12) + 1, (communeIndex % 28) + 1))
                    .adresse("Quartier " + commune.getNom())
                    .commune(commune)
                    .emailVerified(true)
                    .telephoneVerified(true)
                    .actif(true)
                    .build();

            patient = patientRepository.save(patient);

            patientsCreated.add(Map.of(
                    "id", patient.getId(),
                    "nom", patient.getNomComplet(),
                    "telephone", patient.getTelephone(),
                    "email", patient.getEmail() != null ? patient.getEmail() : "",
                    "commune", commune.getNom()
            ));

            log.info("Patient créé: {} - {}", patient.getNomComplet(), patient.getTelephone());
        }

        Map<String, Object> result = Map.of(
                "message", "Patients initialisés",
                "total", patientsCreated.size(),
                "patients", patientsCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Patients créés avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED SUBSCRIPTIONS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-subscriptions
     * Initialiser des abonnements pour les patients
     */
    @PostMapping("/seed-subscriptions")
    @Transactional
    @Operation(summary = "Initialiser les abonnements de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedSubscriptions() {
        log.info("POST /api/v1/init/seed-subscriptions");

        // Vérifier les prérequis
        List<Patient> patients = patientRepository.findAll();
        if (patients.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les patients (seed-patients)")
            );
        }

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByActifTrueOrderByOrdreAsc();
        if (plans.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les plans (seed-plans)")
            );
        }

        List<Map<String, Object>> subscriptionsCreated = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        int planIndex = 0;
        for (Patient patient : patients) {
            // Vérifier si le patient a déjà un abonnement actif
            if (subscriptionRepository.findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE).isPresent()) {
                log.info("Patient {} a déjà un abonnement actif", patient.getNomComplet());
                continue;
            }

            // Attribuer un plan différent à chaque patient
            SubscriptionPlan plan = plans.get(planIndex % plans.size());
            planIndex++;

            // Déterminer les dates
            LocalDateTime startsAt = now.minusDays(planIndex * 2L); // Décalage pour variété
            LocalDateTime expiresAt = startsAt.plusDays(plan.getDureeJours());

            // Déterminer le statut en fonction des dates
            SubscriptionStatus status;
            if (expiresAt.isBefore(now)) {
                status = SubscriptionStatus.EXPIRED;
            } else {
                status = SubscriptionStatus.ACTIVE;
            }

            Subscription subscription = Subscription.builder()
                    .patient(patient)
                    .plan(plan)
                    .status(status)
                    .startsAt(startsAt)
                    .expiresAt(expiresAt)
                    .autoRenew(plan.isAnnual()) // Auto-renew pour annuel
                    .isTrial(plan.isFreeTrial())
                    .build();

            subscription = subscriptionRepository.save(subscription);

            subscriptionsCreated.add(Map.of(
                    "id", subscription.getId(),
                    "patient", patient.getNomComplet(),
                    "plan", plan.getNom(),
                    "status", subscription.getStatus().name(),
                    "startsAt", subscription.getStartsAt().toString(),
                    "expiresAt", subscription.getExpiresAt().toString(),
                    "daysRemaining", subscription.getDaysRemaining()
            ));

            log.info("Abonnement créé: {} - {} ({})",
                    patient.getNomComplet(), plan.getNom(), status);
        }

        Map<String, Object> result = Map.of(
                "message", "Abonnements initialisés",
                "total", subscriptionsCreated.size(),
                "subscriptions", subscriptionsCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Abonnements créés avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED PAYMENTS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-payments
     * Initialiser des paiements de test
     */
    @PostMapping("/seed-payments")
    @Transactional
    @Operation(summary = "Initialiser les paiements de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedPayments() {
        log.info("POST /api/v1/init/seed-payments");

        // Récupérer les abonnements payants (pas les essais gratuits)
        List<Subscription> paidSubscriptions = subscriptionRepository.findAll().stream()
                .filter(s -> !s.isTrial() && s.getPlan().getPrix() > 0)
                .toList();

        if (paidSubscriptions.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les abonnements (seed-subscriptions)")
            );
        }

        List<Map<String, Object>> paymentsCreated = new ArrayList<>();
        PaymentMethod[] methods = {PaymentMethod.ORANGE_MONEY, PaymentMethod.WAVE, PaymentMethod.FREE_MONEY};

        int methodIndex = 0;
        for (Subscription subscription : paidSubscriptions) {
            // Vérifier si un paiement existe déjà pour cet abonnement
            if (!paymentRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscription.getId()).isEmpty()) {
                log.info("Paiement déjà existant pour l'abonnement {}", subscription.getId());
                continue;
            }

            PaymentMethod method = methods[methodIndex % methods.length];
            methodIndex++;

            // Créer le paiement réussi
            Payment payment = Payment.builder()
                    .patient(subscription.getPatient())
                    .subscription(subscription)
                    .montant(subscription.getPlan().getPrix())
                    .methode(method)
                    .status(PaymentStatus.SUCCESS)
                    .telephonePaiement(subscription.getPatient().getTelephone())
                    .referenceInterne(Payment.generateReferenceInterne())
                    .referenceExterne(generateExternalReference(method))
                    .paidAt(subscription.getStartsAt())
                    .build();

            payment = paymentRepository.save(payment);

            paymentsCreated.add(Map.of(
                    "id", payment.getId(),
                    "patient", subscription.getPatient().getNomComplet(),
                    "montant", payment.getMontant() + " FCFA",
                    "methode", payment.getMethode().name(),
                    "status", payment.getStatus().name(),
                    "referenceInterne", payment.getReferenceInterne(),
                    "referenceExterne", payment.getReferenceExterne()
            ));

            log.info("Paiement créé: {} - {} FCFA via {}",
                    subscription.getPatient().getNomComplet(),
                    payment.getMontant(),
                    method);
        }

        // Ajouter quelques paiements échoués pour le réalisme
        List<Patient> patients = patientRepository.findAll();
        if (!patients.isEmpty()) {
            Patient patientWithFailedPayment = patients.get(0);

            Payment failedPayment = Payment.builder()
                    .patient(patientWithFailedPayment)
                    .subscription(null)
                    .montant(750)
                    .methode(PaymentMethod.ORANGE_MONEY)
                    .status(PaymentStatus.FAILED)
                    .telephonePaiement(patientWithFailedPayment.getTelephone())
                    .referenceInterne(Payment.generateReferenceInterne())
                    .errorMessage("Solde insuffisant")
                    .build();

            failedPayment = paymentRepository.save(failedPayment);

            paymentsCreated.add(Map.of(
                    "id", failedPayment.getId(),
                    "patient", patientWithFailedPayment.getNomComplet(),
                    "montant", failedPayment.getMontant() + " FCFA",
                    "methode", failedPayment.getMethode().name(),
                    "status", failedPayment.getStatus().name(),
                    "referenceInterne", failedPayment.getReferenceInterne(),
                    "error", failedPayment.getErrorMessage()
            ));

            log.info("Paiement échoué créé pour: {}", patientWithFailedPayment.getNomComplet());
        }

        Map<String, Object> result = Map.of(
                "message", "Paiements initialisés",
                "total", paymentsCreated.size(),
                "payments", paymentsCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Paiements créés avec succès", result));
    }

    /**
     * Générer une référence externe simulée
     */
    private String generateExternalReference(PaymentMethod method) {
        String prefix = switch (method) {
            case ORANGE_MONEY -> "OM";
            case WAVE -> "WV";
            case FREE_MONEY -> "FM";
            default -> "XX";
        };
        return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : GET PATIENTS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/patients-list
     * Liste des patients
     */
    @GetMapping("/patients-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les patients")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPatients() {
        log.info("GET /api/v1/init/patients-list");

        List<Patient> patients = patientRepository.findAll();

        List<Map<String, Object>> result = patients.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("nomComplet", p.getNomComplet());
                    map.put("telephone", p.getTelephone());
                    map.put("email", p.getEmail());
                    map.put("sexe", p.getSexe());
                    map.put("dateNaissance", p.getDateNaissance());
                    map.put("commune", p.getCommune() != null ? p.getCommune().getNom() : null);
                    map.put("actif", p.isActif());
                    map.put("emailVerified", p.isEmailVerified());
                    map.put("telephoneVerified", p.isTelephoneVerified());
                    map.put("createdAt", p.getCreatedAt());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des patients", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : GET SUBSCRIPTIONS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/subscriptions-list
     * Liste des abonnements
     */
    @GetMapping("/subscriptions-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les abonnements")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSubscriptions() {
        log.info("GET /api/v1/init/subscriptions-list");

        List<Subscription> subscriptions = subscriptionRepository.findAll();

        List<Map<String, Object>> result = subscriptions.stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    map.put("patient", s.getPatient().getNomComplet());
                    map.put("patientId", s.getPatient().getId());
                    map.put("plan", s.getPlan().getNom());
                    map.put("planCode", s.getPlan().getCode());
                    map.put("prix", s.getPlan().getPrix());
                    map.put("status", s.getStatus().name());
                    map.put("startsAt", s.getStartsAt());
                    map.put("expiresAt", s.getExpiresAt());
                    map.put("daysRemaining", s.getDaysRemaining());
                    map.put("isTrial", s.isTrial());
                    map.put("autoRenew", s.isAutoRenew());
                    map.put("isActive", s.isActive());
                    map.put("isExpired", s.isExpired());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des abonnements", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : GET PAYMENTS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/payments-list
     * Liste des paiements
     */
    @GetMapping("/payments-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les paiements")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPayments() {
        log.info("GET /api/v1/init/payments-list");

        List<Payment> payments = paymentRepository.findAll();

        List<Map<String, Object>> result = payments.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("patient", p.getPatient().getNomComplet());
                    map.put("patientId", p.getPatient().getId());
                    map.put("montant", p.getMontant());
                    map.put("methode", p.getMethode().name());
                    map.put("status", p.getStatus().name());
                    map.put("referenceInterne", p.getReferenceInterne());
                    map.put("referenceExterne", p.getReferenceExterne());
                    map.put("telephonePaiement", p.getTelephonePaiement());
                    map.put("paidAt", p.getPaidAt());
                    map.put("errorMessage", p.getErrorMessage());
                    map.put("createdAt", p.getCreatedAt());
                    if (p.getSubscription() != null) {
                        map.put("subscriptionId", p.getSubscription().getId());
                        map.put("planNom", p.getSubscription().getPlan().getNom());
                    }
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des paiements", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : GET PLANS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/plans-list
     * Liste des plans d'abonnement
     */
    @GetMapping("/plans-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les plans d'abonnement")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPlans() {
        log.info("GET /api/v1/init/plans-list");

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        List<Map<String, Object>> result = plans.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("code", p.getCode());
                    map.put("nom", p.getNom());
                    map.put("description", p.getDescription());
                    map.put("prix", p.getPrix());
                    map.put("dureeJours", p.getDureeJours());
                    map.put("avecPublicite", p.isAvecPublicite());
                    map.put("actif", p.isActif());
                    map.put("ordre", p.getOrdre());
                    map.put("prixParJour", Math.round(p.getPrixParJour() * 100.0) / 100.0);
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des plans", result));
    }



    // ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED MUTUELLES
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-mutuelles
     * Initialiser les mutuelles sénégalaises
     */
    @PostMapping("/seed-mutuelles")
    @Transactional
    @Operation(summary = "Initialiser les mutuelles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedMutuelles() {
        log.info("POST /api/v1/init/seed-mutuelles");

        List<Map<String, Object>> mutuellesCreated = new ArrayList<>();

        // ═══════════════════════════════════════════════════════════
        // IPM (Institutions de Prévoyance Maladie)
        // ═══════════════════════════════════════════════════════════

        // IPM Sonatel
        if (!mutuelleRepository.existsByCode("IPM-SONATEL")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("IPM-SONATEL")
                    .nom("IPM Sonatel")
                    .nomCourt("IPM Sonatel")
                    .type(TypeMutuelle.IPM)
                    .typeCouverture(TypeCouverture.PARTIELLE_80)
                    .tauxCouvertureDefaut(new BigDecimal("80.00"))
                    .plafondAnnuel(new BigDecimal("5000000"))
                    .adresse("46, Boulevard de la République, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 839 90 00")
                    .email("ipm@sonatel.sn")
                    .delaiPaiementJours(30)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // IPM SENELEC
        if (!mutuelleRepository.existsByCode("IPM-SENELEC")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("IPM-SENELEC")
                    .nom("IPM SENELEC")
                    .nomCourt("IPM SENELEC")
                    .type(TypeMutuelle.IPM)
                    .typeCouverture(TypeCouverture.PARTIELLE_80)
                    .tauxCouvertureDefaut(new BigDecimal("80.00"))
                    .plafondAnnuel(new BigDecimal("3000000"))
                    .adresse("28, Rue Vincens, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 839 30 30")
                    .email("ipm@senelec.sn")
                    .delaiPaiementJours(45)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // IPM Port Autonome de Dakar
        if (!mutuelleRepository.existsByCode("IPM-PAD")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("IPM-PAD")
                    .nom("IPM Port Autonome de Dakar")
                    .nomCourt("IPM PAD")
                    .type(TypeMutuelle.IPM)
                    .typeCouverture(TypeCouverture.PARTIELLE_70)
                    .tauxCouvertureDefaut(new BigDecimal("70.00"))
                    .plafondAnnuel(new BigDecimal("2500000"))
                    .adresse("21, Boulevard de la Libération, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 849 45 45")
                    .email("ipm@portdakar.sn")
                    .delaiPaiementJours(30)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // ═══════════════════════════════════════════════════════════
        // MUTUELLES DE SANTÉ
        // ═══════════════════════════════════════════════════════════

        // Mutuelle des Enseignants du Sénégal
        if (!mutuelleRepository.existsByCode("MUT-ENSEIGNANTS")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("MUT-ENSEIGNANTS")
                    .nom("Mutuelle des Enseignants du Sénégal")
                    .nomCourt("MUDES")
                    .type(TypeMutuelle.MUTUELLE_SANTE)
                    .typeCouverture(TypeCouverture.PARTIELLE_70)
                    .tauxCouvertureDefaut(new BigDecimal("70.00"))
                    .plafondAnnuel(new BigDecimal("2000000"))
                    .adresse("Sicap Liberté 6, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 867 12 34")
                    .email("contact@mudes.sn")
                    .delaiPaiementJours(60)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // Mutuelle des Agents de l'État
        if (!mutuelleRepository.existsByCode("MUT-FONCTIONNAIRES")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("MUT-FONCTIONNAIRES")
                    .nom("Mutuelle des Agents de l'État")
                    .nomCourt("MAE")
                    .type(TypeMutuelle.MUTUELLE_SANTE)
                    .typeCouverture(TypeCouverture.PARTIELLE_60)
                    .tauxCouvertureDefaut(new BigDecimal("60.00"))
                    .plafondAnnuel(new BigDecimal("1500000"))
                    .adresse("Avenue Cheikh Anta Diop, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 825 67 89")
                    .email("info@mae.sn")
                    .delaiPaiementJours(45)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // ═══════════════════════════════════════════════════════════
        // ASSURANCES
        // ═══════════════════════════════════════════════════════════

        // AXA Assurances Sénégal
        if (!mutuelleRepository.existsByCode("ASS-AXA")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("ASS-AXA")
                    .nom("AXA Assurances Sénégal")
                    .nomCourt("AXA")
                    .type(TypeMutuelle.ASSURANCE)
                    .typeCouverture(TypeCouverture.PARTIELLE_80)
                    .tauxCouvertureDefaut(new BigDecimal("80.00"))
                    .plafondAnnuel(new BigDecimal("10000000"))
                    .plafondParActe(new BigDecimal("500000"))
                    .adresse("5, Place de l'Indépendance, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 849 10 10")
                    .email("sante@axa.sn")
                    .siteWeb("https://www.axa.sn")
                    .delaiPaiementJours(15)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .exigeFactureDetaillee(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .couvreParamedical(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // NSIA Assurances
        if (!mutuelleRepository.existsByCode("ASS-NSIA")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("ASS-NSIA")
                    .nom("NSIA Assurances Sénégal")
                    .nomCourt("NSIA")
                    .type(TypeMutuelle.ASSURANCE)
                    .typeCouverture(TypeCouverture.PARTIELLE_70)
                    .tauxCouvertureDefaut(new BigDecimal("70.00"))
                    .plafondAnnuel(new BigDecimal("8000000"))
                    .adresse("Avenue Léopold Sédar Senghor, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 889 62 00")
                    .email("sante@nsia.sn")
                    .siteWeb("https://www.nsia.sn")
                    .delaiPaiementJours(20)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // Allianz Sénégal
        if (!mutuelleRepository.existsByCode("ASS-ALLIANZ")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("ASS-ALLIANZ")
                    .nom("Allianz Sénégal Assurances")
                    .nomCourt("Allianz")
                    .type(TypeMutuelle.ASSURANCE)
                    .typeCouverture(TypeCouverture.PARTIELLE_80)
                    .tauxCouvertureDefaut(new BigDecimal("80.00"))
                    .plafondAnnuel(new BigDecimal("15000000"))
                    .plafondParActe(new BigDecimal("1000000"))
                    .adresse("1, Place de l'Indépendance, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 849 96 96")
                    .email("sante@allianz.sn")
                    .siteWeb("https://www.allianz.sn")
                    .delaiPaiementJours(10)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .exigeFactureDetaillee(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .couvreParamedical(true)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // ═══════════════════════════════════════════════════════════
        // CMU (Couverture Maladie Universelle)
        // ═══════════════════════════════════════════════════════════

        if (!mutuelleRepository.existsByCode("CMU-SENEGAL")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("CMU-SENEGAL")
                    .nom("Couverture Maladie Universelle du Sénégal")
                    .nomCourt("CMU")
                    .type(TypeMutuelle.CMU)
                    .typeCouverture(TypeCouverture.PARTIELLE_80)
                    .tauxCouvertureDefaut(new BigDecimal("80.00"))
                    .plafondAnnuel(new BigDecimal("500000"))
                    .adresse("Ministère de la Santé, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 869 42 42")
                    .email("contact@cmu.gouv.sn")
                    .siteWeb("https://www.cmu.gouv.sn")
                    .delaiPaiementJours(90)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .estActif(true)
                    .notes("Couverture pour les populations vulnérables")
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        // ═══════════════════════════════════════════════════════════
        // ENTREPRISES (Prise en charge directe)
        // ═══════════════════════════════════════════════════════════

        // Total Energies Sénégal
        if (!mutuelleRepository.existsByCode("ENT-TOTAL")) {
            Mutuelle mutuelle = Mutuelle.builder()
                    .code("ENT-TOTAL")
                    .nom("Total Energies Sénégal - Santé")
                    .nomCourt("Total Energies")
                    .type(TypeMutuelle.ENTREPRISE)
                    .typeCouverture(TypeCouverture.TOTALE)
                    .tauxCouvertureDefaut(new BigDecimal("100.00"))
                    .plafondAnnuel(new BigDecimal("20000000"))
                    .adresse("Route de la Corniche, Dakar")
                    .ville("Dakar")
                    .telephone("+221 33 859 81 81")
                    .email("sante@total.sn")
                    .delaiPaiementJours(15)
                    .exigeOrdonnance(true)
                    .exigeCarteAdherent(true)
                    .couvreMedicaments(true)
                    .couvreGeneriques(true)
                    .couvreParamedical(true)
                    .couvreCosmetique(false)
                    .estActif(true)
                    .build();
            mutuelle = mutuelleRepository.save(mutuelle);
            mutuellesCreated.add(Map.of("code", mutuelle.getCode(), "nom", mutuelle.getNom(), "type", mutuelle.getType().name()));
        }

        Map<String, Object> result = Map.of(
                "message", "Mutuelles initialisées",
                "total", mutuellesCreated.size(),
                "mutuelles", mutuellesCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Mutuelles créées avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED ADHERENTS
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-adherents
     * Initialiser des adhérents de test pour les mutuelles
     */
    @PostMapping("/seed-adherents")
    @Transactional
    @Operation(summary = "Initialiser les adhérents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAdherents() {
        log.info("POST /api/v1/init/seed-adherents");

        List<Mutuelle> mutuelles = mutuelleRepository.findByEstActif(true);
        if (mutuelles.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les mutuelles (seed-mutuelles)")
            );
        }

        List<Map<String, Object>> adherentsCreated = new ArrayList<>();

        // Données des adhérents
        List<Map<String, String>> adherentsData = List.of(
                Map.of("nom", "DIALLO", "prenom", "Mamadou", "sexe", "M", "telephone", "+221771001001"),
                Map.of("nom", "NDIAYE", "prenom", "Fatou", "sexe", "F", "telephone", "+221771001002"),
                Map.of("nom", "FALL", "prenom", "Ibrahima", "sexe", "M", "telephone", "+221771001003"),
                Map.of("nom", "DIOP", "prenom", "Aïssatou", "sexe", "F", "telephone", "+221771001004"),
                Map.of("nom", "SOW", "prenom", "Ousmane", "sexe", "M", "telephone", "+221771001005"),
                Map.of("nom", "BA", "prenom", "Mariama", "sexe", "F", "telephone", "+221771001006"),
                Map.of("nom", "GUEYE", "prenom", "Abdoulaye", "sexe", "M", "telephone", "+221771001007"),
                Map.of("nom", "SARR", "prenom", "Khady", "sexe", "F", "telephone", "+221771001008"),
                Map.of("nom", "MBAYE", "prenom", "Cheikh", "sexe", "M", "telephone", "+221771001009"),
                Map.of("nom", "FAYE", "prenom", "Aminata", "sexe", "F", "telephone", "+221771001010"),
                Map.of("nom", "SECK", "prenom", "Moussa", "sexe", "M", "telephone", "+221771001011"),
                Map.of("nom", "DIOUF", "prenom", "Ndèye", "sexe", "F", "telephone", "+221771001012"),
                Map.of("nom", "THIAM", "prenom", "Pape", "sexe", "M", "telephone", "+221771001013"),
                Map.of("nom", "KANE", "prenom", "Sokhna", "sexe", "F", "telephone", "+221771001014"),
                Map.of("nom", "CISSE", "prenom", "Modou", "sexe", "M", "telephone", "+221771001015")
        );

        int adherentIndex = 0;
        for (Mutuelle mutuelle : mutuelles) {
            // 3 à 5 adhérents par mutuelle
            int nbAdherents = 3 + (adherentIndex % 3);

            for (int i = 0; i < nbAdherents && adherentIndex < adherentsData.size(); i++) {
                Map<String, String> data = adherentsData.get(adherentIndex);
                String numeroAdherent = String.format("%s-%06d", mutuelle.getCode(), adherentIndex + 1);

                // Vérifier si l'adhérent existe déjà
                if (adherentRepository.findByMutuelleIdAndNumeroAdherent(mutuelle.getId(), numeroAdherent).isPresent()) {
                    adherentIndex++;
                    continue;
                }

                Adherent adherent = Adherent.builder()
                        .mutuelle(mutuelle)
                        .numeroAdherent(numeroAdherent)
                        .numeroCarte("CARTE-" + numeroAdherent)
                        .typeBeneficiaire("TITULAIRE")
                        .nom(data.get("nom"))
                        .prenom(data.get("prenom"))
                        .sexe(data.get("sexe"))
                        .dateNaissance(LocalDate.of(1970 + (adherentIndex % 30), (adherentIndex % 12) + 1, (adherentIndex % 28) + 1))
                        .telephone(data.get("telephone"))
                        .email(data.get("prenom").toLowerCase() + "." + data.get("nom").toLowerCase() + "@email.sn")
                        .adresse("Dakar, Sénégal")
                        .tauxCouverture(mutuelle.getTauxCouvertureDefaut())
                        .plafondAnnuel(mutuelle.getPlafondAnnuel())
                        .consommationAnnuelle(BigDecimal.ZERO)
                        .dateAdhesion(LocalDate.now().minusYears(adherentIndex % 5).minusMonths(adherentIndex % 12))
                        .dateFinDroits(LocalDate.now().plusYears(1))
                        .estActif(true)
                        .build();

                adherent = adherentRepository.save(adherent);

                adherentsCreated.add(Map.of(
                        "id", adherent.getId(),
                        "numeroAdherent", adherent.getNumeroAdherent(),
                        "nomComplet", adherent.getNomComplet(),
                        "mutuelle", mutuelle.getNomCourt() != null ? mutuelle.getNomCourt() : mutuelle.getNom(),
                        "tauxCouverture", adherent.getTauxCouverture() + "%"
                ));

                adherentIndex++;
            }
        }

        Map<String, Object> result = Map.of(
                "message", "Adhérents initialisés",
                "total", adherentsCreated.size(),
                "adherents", adherentsCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Adhérents créés avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED CONTRATS MUTUELLES
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-contrats-mutuelles
     * Initialiser des contrats entre pharmacies et mutuelles
     */
    @PostMapping("/seed-contrats-mutuelles")
    @Transactional
    @Operation(summary = "Initialiser les contrats mutuelles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedContratsMutuelles() {
        log.info("POST /api/v1/init/seed-contrats-mutuelles");

        List<Pharmacie> pharmacies = pharmacieRepository.findAll();
        if (pharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les pharmacies (seed-pharmacies)")
            );
        }

        List<Mutuelle> mutuelles = mutuelleRepository.findByEstActif(true);
        if (mutuelles.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les mutuelles (seed-mutuelles)")
            );
        }

        List<Map<String, Object>> contratsCreated = new ArrayList<>();

        // Chaque pharmacie conventionnée avec quelques mutuelles
        int contratIndex = 0;
        for (Pharmacie pharmacie : pharmacies) {
            // 3 à 6 mutuelles par pharmacie
            int nbMutuelles = 3 + (contratIndex % 4);

            for (int i = 0; i < nbMutuelles && i < mutuelles.size(); i++) {
                Mutuelle mutuelle = mutuelles.get((contratIndex + i) % mutuelles.size());

                // Vérifier si le contrat existe déjà
                if (contratMutuelleRepository.findByPharmacieIdAndMutuelleId(pharmacie.getId(), mutuelle.getId()).isPresent()) {
                    continue;
                }

                String numeroContrat = String.format("CONV-%s-%s-%d",
                        pharmacie.getCode(),
                        mutuelle.getCode(),
                        LocalDate.now().getYear());

                ContratMutuelle contrat = ContratMutuelle.builder()
                        .pharmacie(pharmacie)
                        .mutuelle(mutuelle)
                        .numeroContrat(numeroContrat)
                        .numeroConventionnement("CONV-" + String.format("%06d", contratIndex + 1))
                        .dateDebut(LocalDate.now().minusYears(1))
                        .dateFin(LocalDate.now().plusYears(1))
                        .renouvellementAuto(true)
                        .tauxCouverture(mutuelle.getTauxCouvertureDefaut())
                        .plafondMensuel(new BigDecimal("500000"))
                        .plafondAnnuel(mutuelle.getPlafondAnnuel())
                        .delaiPaiementJours(mutuelle.getDelaiPaiementJours())
                        .frequenceFacturation("MENSUEL")
                        .jourFacturation(5)
                        .emailFacturation(pharmacie.getEmail())
                        .tiersPayantAutorise(true)
                        .montantAvanceMax(new BigDecimal("200000"))
                        .estActif(true)
                        .notes("Contrat standard")
                        .build();

                contrat = contratMutuelleRepository.save(contrat);

                contratsCreated.add(Map.of(
                        "id", contrat.getId(),
                        "numeroContrat", contrat.getNumeroContrat(),
                        "pharmacie", pharmacie.getNom(),
                        "mutuelle", mutuelle.getNomCourt() != null ? mutuelle.getNomCourt() : mutuelle.getNom(),
                        "tauxCouverture", contrat.getTauxCouverture() + "%",
                        "tiersPayant", contrat.getTiersPayantAutorise()
                ));

                contratIndex++;
            }
        }

        Map<String, Object> result = Map.of(
                "message", "Contrats mutuelles initialisés",
                "total", contratsCreated.size(),
                "contrats", contratsCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Contrats créés avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINTS : GET LISTES
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/mutuelles-list
     * Liste des mutuelles
     */
    @GetMapping("/mutuelles-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les mutuelles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listMutuelles() {
        log.info("GET /api/v1/init/mutuelles-list");

        List<Mutuelle> mutuelles = mutuelleRepository.findAll();

        List<Map<String, Object>> result = mutuelles.stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", m.getId());
                    map.put("code", m.getCode());
                    map.put("nom", m.getNom());
                    map.put("nomCourt", m.getNomCourt());
                    map.put("type", m.getType().name());
                    map.put("typeLibelle", m.getType().getLibelle());
                    map.put("typeCouverture", m.getTypeCouverture() != null ? m.getTypeCouverture().name() : null);
                    map.put("tauxCouvertureDefaut", m.getTauxCouvertureDefaut());
                    map.put("plafondAnnuel", m.getPlafondAnnuel());
                    map.put("telephone", m.getTelephone());
                    map.put("email", m.getEmail());
                    map.put("delaiPaiementJours", m.getDelaiPaiementJours());
                    map.put("estActif", m.getEstActif());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des mutuelles", result));
    }

    /**
     * GET /api/v1/init/adherents-list
     * Liste des adhérents
     */
    @GetMapping("/adherents-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les adhérents")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listAdherents() {
        log.info("GET /api/v1/init/adherents-list");

        List<Adherent> adherents = adherentRepository.findAll();

        List<Map<String, Object>> result = adherents.stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("numeroAdherent", a.getNumeroAdherent());
                    map.put("numeroCarte", a.getNumeroCarte());
                    map.put("nomComplet", a.getNomComplet());
                    map.put("telephone", a.getTelephone());
                    map.put("email", a.getEmail());
                    map.put("mutuelle", a.getMutuelle().getNom());
                    map.put("mutuelleCode", a.getMutuelle().getCode());
                    map.put("typeBeneficiaire", a.getTypeBeneficiaire());
                    map.put("tauxCouverture", a.getTauxCouverture());
                    map.put("plafondAnnuel", a.getPlafondAnnuel());
                    map.put("consommationAnnuelle", a.getConsommationAnnuelle());
                    map.put("resteAConsommer", a.getResteAConsommer());
                    map.put("droitsOuverts", a.droitsOuverts());
                    map.put("dateAdhesion", a.getDateAdhesion());
                    map.put("dateFinDroits", a.getDateFinDroits());
                    map.put("estActif", a.getEstActif());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des adhérents", result));
    }

    /**
     * GET /api/v1/init/contrats-mutuelles-list
     * Liste des contrats mutuelles
     */
    @GetMapping("/contrats-mutuelles-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les contrats mutuelles")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listContratsMutuelles() {
        log.info("GET /api/v1/init/contrats-mutuelles-list");

        List<ContratMutuelle> contrats = contratMutuelleRepository.findAll();

        List<Map<String, Object>> result = contrats.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("numeroContrat", c.getNumeroContrat());
                    map.put("numeroConventionnement", c.getNumeroConventionnement());
                    map.put("pharmacie", c.getPharmacie().getNom());
                    map.put("pharmacieCode", c.getPharmacie().getCode());
                    map.put("mutuelle", c.getMutuelle().getNom());
                    map.put("mutuelleCode", c.getMutuelle().getCode());
                    map.put("tauxCouverture", c.getTauxCouverture());
                    map.put("plafondMensuel", c.getPlafondMensuel());
                    map.put("plafondAnnuel", c.getPlafondAnnuel());
                    map.put("tiersPayantAutorise", c.getTiersPayantAutorise());
                    map.put("montantAvanceMax", c.getMontantAvanceMax());
                    map.put("dateDebut", c.getDateDebut());
                    map.put("dateFin", c.getDateFin());
                    map.put("estActif", c.getEstActif());
                    map.put("estValide", c.estValide());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des contrats mutuelles", result));
    }



// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : SEED VENTES
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * POST /api/v1/init/seed-ventes
     * Initialiser des ventes de test
     */
    @PostMapping("/seed-ventes")
    @Transactional
    @Operation(summary = "Initialiser les ventes de test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedVentes() {
        log.info("POST /api/v1/init/seed-ventes");

        // Vérifier les prérequis
        List<Pharmacie> pharmacies = pharmacieRepository.findAll();
        if (pharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser les pharmacies (seed-pharmacies)")
            );
        }

        List<ProduitPharmacie> produitsPharmacies = produitPharmacieRepository.findAll();
        if (produitsPharmacies.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Veuillez d'abord initialiser le stock (seed-stock)")
            );
        }

        // Récupérer les adhérents (optionnel, pour ventes mutuelles)
        List<Adherent> adherents = adherentRepository.findAll();

        List<Map<String, Object>> ventesCreated = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();

        // Modes de paiement
        ModePaiement[] modesPaiement = {
                ModePaiement.ESPECES, ModePaiement.ESPECES, ModePaiement.ESPECES,  // 50% espèces
                ModePaiement.ORANGE_MONEY, ModePaiement.WAVE,                       // 30% mobile
                ModePaiement.CARTE_BANCAIRE                                         // 20% CB
        };

        // Noms de clients fictifs
        List<String> clientsNoms = List.of(
                "Amadou Diallo", "Fatou Ndiaye", "Moussa Sow", "Aïssatou Ba",
                "Ibrahima Fall", "Mariama Diop", "Ousmane Mbaye", "Khady Sarr",
                "Cheikh Gueye", "Aminata Faye", "Modou Seck", "Ndèye Diouf"
        );

        List<String> clientsTelephones = List.of(
                "+221771112233", "+221772223344", "+221773334455", "+221774445566",
                "+221775556677", "+221776667788", "+221777778899", "+221778889900",
                "+221779990011", "+221770001122", "+221771122334", "+221772233445"
        );

        int venteIndex = 0;

        for (Pharmacie pharmacie : pharmacies) {
            // Récupérer les produits de cette pharmacie
            List<ProduitPharmacie> produitsDePhramacie = produitPharmacieRepository
                    .findByPharmacieId(pharmacie.getId());

            if (produitsDePhramacie.isEmpty()) {
                log.warn("Aucun produit pour la pharmacie {}", pharmacie.getNom());
                continue;
            }

            // Créer 5 à 10 ventes par pharmacie
            int nbVentes = 5 + random.nextInt(6);

            for (int v = 0; v < nbVentes; v++) {
                // Générer le numéro de vente
                String numeroVente = String.format("VTE-%s-%s-%04d",
                        pharmacie.getCode(),
                        now.minusDays(venteIndex % 30).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")),
                        venteIndex + 1);

                // Vérifier si la vente existe déjà
                if (venteRepository.existsByNumero(numeroVente)) {
                    venteIndex++;
                    continue;
                }

                // Date de vente (étalée sur le mois)
                LocalDateTime dateVente = now.minusDays(venteIndex % 30)
                        .minusHours(random.nextInt(12))
                        .minusMinutes(random.nextInt(60));

                // Type de vente
                TypeVente typeVente;
                UUID mutuelleId = null;
                String mutuelleNom = null;
                String numeroAdherent = null;
                BigDecimal tauxPriseEnCharge = null;

                int typeRandom = random.nextInt(10);
                if (typeRandom < 6) {
                    typeVente = TypeVente.COMPTOIR;
                } else if (typeRandom < 8 && !adherents.isEmpty()) {
                    typeVente = TypeVente.MUTUELLE;
                    // Prendre un adhérent au hasard
                    Adherent adherent = adherents.get(random.nextInt(adherents.size()));
                    mutuelleId = adherent.getMutuelle().getId();
                    mutuelleNom = adherent.getMutuelle().getNom();
                    numeroAdherent = adherent.getNumeroAdherent();
                    tauxPriseEnCharge = adherent.getTauxEffectif();
                } else if (typeRandom < 9) {
                    typeVente = TypeVente.ORDONNANCE;
                } else {
                    typeVente = TypeVente.LIVRAISON;
                }

                // Client (60% anonyme, 40% identifié)
                String clientNom = null;
                String clientTelephone = null;
                if (random.nextInt(10) >= 6) {
                    int clientIdx = random.nextInt(clientsNoms.size());
                    clientNom = clientsNoms.get(clientIdx);
                    clientTelephone = clientsTelephones.get(clientIdx);
                }

                // Créer la vente
                Vente vente = Vente.builder()
                        .numero(numeroVente)
                        .numeroTicket(String.format("T%06d", venteIndex + 1))
                        .pharmacie(pharmacie)
                        .type(typeVente)
                        .statut(StatutVente.EN_COURS)
                        .clientNom(clientNom)
                        .clientTelephone(clientTelephone)
                        .mutuelleId(mutuelleId)
                        .mutuelleNom(mutuelleNom)
                        .numeroAdherent(numeroAdherent)
                        .tauxPriseEnCharge(tauxPriseEnCharge)
                        .dateVente(dateVente)
                        .vendeurNom("Vendeur " + (venteIndex % 3 + 1))
                        .build();

                // Ajouter des lignes (1 à 5 produits)
                int nbLignes = 1 + random.nextInt(5);
                List<ProduitPharmacie> produitsUtilises = new ArrayList<>();

                for (int l = 0; l < nbLignes && l < produitsDePhramacie.size(); l++) {
                    // Sélectionner un produit différent pour chaque ligne
                    ProduitPharmacie pp;
                    int attempts = 0;
                    do {
                        pp = produitsDePhramacie.get(random.nextInt(produitsDePhramacie.size()));
                        attempts++;
                    } while (produitsUtilises.contains(pp) && attempts < 10);

                    if (produitsUtilises.contains(pp)) continue;
                    produitsUtilises.add(pp);

                    // Quantité (1 à 3)
                    int quantite = 1 + random.nextInt(3);

                    // Créer la ligne
                    LigneVente ligne = LigneVente.builder()
                            .numeroLigne(l + 1)
                            .produit(pp.getProduit())
                            .produitPharmacie(pp)
                            .produitCode(pp.getProduit().getCode())
                            .produitNom(pp.getProduit().getNomComplet())
                            .produitDci(pp.getProduit().getDci())
                            .quantite(quantite)
                            .prixUnitaireTTC(pp.getPrixVenteTTC())
                            .tauxTVA(pp.getProduit().getTauxTVA())
                            .surOrdonnance(pp.getProduit().getSurOrdonnance())
                            .estRemboursable(pp.getProduit().getEstRemboursable())
                            .tauxRemboursement(pp.getProduit().getTauxRemboursement())
                            .build();

                    // Calculer les montants de la ligne
                    ligne.calculerMontants();
                    vente.ajouterLigne(ligne);
                }

                // Recalculer les totaux de la vente
                vente.recalculerTotaux();

                // Appliquer une remise occasionnellement (20% des cas)
                if (random.nextInt(10) >= 8) {
                    BigDecimal remise = BigDecimal.valueOf(5 + random.nextInt(11)); // 5% à 15%
                    vente.appliquerRemise(remise);
                }

                // Mode de paiement
                ModePaiement modePaiement = modesPaiement[random.nextInt(modesPaiement.length)];

                // Enregistrer le paiement (90% payées complètement)
                if (random.nextInt(10) < 9) {
                    BigDecimal montantPaye = vente.getMontantClient();
                    String reference = null;

                    if (modePaiement == ModePaiement.ORANGE_MONEY) {
                        reference = "OM-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                    } else if (modePaiement == ModePaiement.WAVE) {
                        reference = "WV-" + System.currentTimeMillis() + "-" + random.nextInt(1000);
                    } else if (modePaiement == ModePaiement.CARTE_BANCAIRE) {
                        reference = "CB-" + String.format("%04d", random.nextInt(10000));
                    }

                    vente.enregistrerPaiement(montantPaye, modePaiement, reference);
                    vente.setStatut(StatutVente.VALIDEE);
                } else {
                    // Vente partiellement payée (10% des cas)
                    BigDecimal montantPartiel = vente.getMontantClient()
                            .multiply(BigDecimal.valueOf(0.5)); // 50% payé
                    vente.enregistrerPaiement(montantPartiel, modePaiement, null);
                    vente.setStatut(StatutVente.PARTIELLEMENT_PAYEE);
                }

                // Sauvegarder la vente
                vente = venteRepository.save(vente);

                ventesCreated.add(Map.of(
                        "id", vente.getId(),
                        "numero", vente.getNumero(),
                        "pharmacie", pharmacie.getNom(),
                        "type", vente.getType().name(),
                        "statut", vente.getStatut().name(),
                        "nbArticles", vente.getNombreArticles(),
                        "montantTTC", vente.getMontantNetTTC() + " FCFA",
                        "montantClient", vente.getMontantClient() + " FCFA",
                        "modePaiement", vente.getModePaiement() != null ? vente.getModePaiement().name() : "NON_PAYE"
                ));

                log.info("Vente créée: {} - {} FCFA ({} articles)",
                        vente.getNumero(), vente.getMontantNetTTC(), vente.getNombreArticles());

                venteIndex++;
            }
        }

        Map<String, Object> result = Map.of(
                "message", "Ventes initialisées",
                "total", ventesCreated.size(),
                "ventes", ventesCreated
        );

        return ResponseEntity.ok(ApiResponse.success("Ventes créées avec succès", result));
    }


// ═══════════════════════════════════════════════════════════════════════════════
// ENDPOINT : GET VENTES
// ═══════════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/v1/init/ventes-list
     * Liste des ventes
     */
    @GetMapping("/ventes-list")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les ventes")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listVentes() {
        log.info("GET /api/v1/init/ventes-list");

        List<Vente> ventes = venteRepository.findAll();

        List<Map<String, Object>> result = ventes.stream()
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", v.getId());
                    map.put("numero", v.getNumero());
                    map.put("numeroTicket", v.getNumeroTicket());
                    map.put("pharmacie", v.getPharmacie().getNom());
                    map.put("pharmacieCode", v.getPharmacie().getCode());
                    map.put("type", v.getType().name());
                    map.put("statut", v.getStatut().name());
                    map.put("clientNom", v.getClientNom());
                    map.put("clientTelephone", v.getClientTelephone());
                    map.put("mutuelleNom", v.getMutuelleNom());
                    map.put("numeroAdherent", v.getNumeroAdherent());
                    map.put("tauxPriseEnCharge", v.getTauxPriseEnCharge());
                    map.put("nombreArticles", v.getNombreArticles());
                    map.put("montantBrutTTC", v.getMontantBrutTTC());
                    map.put("montantRemise", v.getMontantRemise());
                    map.put("montantNetTTC", v.getMontantNetTTC());
                    map.put("montantMutuelle", v.getMontantMutuelle());
                    map.put("montantClient", v.getMontantClient());
                    map.put("montantPaye", v.getMontantPaye());
                    map.put("resteAPayer", v.getResteAPayer());
                    map.put("modePaiement", v.getModePaiement() != null ? v.getModePaiement().name() : null);
                    map.put("dateVente", v.getDateVente());
                    map.put("vendeurNom", v.getVendeurNom());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Liste des ventes", result));
    }


    /**
     * GET /api/v1/init/ventes-list/{venteId}/lignes
     * Liste des lignes d'une vente
     */
    @GetMapping("/ventes-list/{venteId}/lignes")
    @Transactional(readOnly = true)
    @Operation(summary = "Lister les lignes d'une vente")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listLignesVente(@PathVariable UUID venteId) {
        log.info("GET /api/v1/init/ventes-list/{}/lignes", venteId);

        List<LigneVente> lignes = ligneVenteRepository.findByVenteIdOrderByNumeroLigne(venteId);

        List<Map<String, Object>> result = lignes.stream()
                .map(l -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", l.getId());
                    map.put("numeroLigne", l.getNumeroLigne());
                    map.put("produitCode", l.getProduitCode());
                    map.put("produitNom", l.getProduitNom());
                    map.put("produitDci", l.getProduitDci());
                    map.put("quantite", l.getQuantite());
                    map.put("quantiteGratuite", l.getQuantiteGratuite());
                    map.put("prixUnitaireTTC", l.getPrixUnitaireTTC());
                    map.put("tauxTVA", l.getTauxTVA());
                    map.put("remisePourcentage", l.getRemisePourcentage());
                    map.put("remiseMontant", l.getRemiseMontant());
                    map.put("montantHT", l.getMontantHT());
                    map.put("montantTVA", l.getMontantTVA());
                    map.put("montantTTC", l.getMontantTTC());
                    map.put("surOrdonnance", l.getSurOrdonnance());
                    map.put("estRemboursable", l.getEstRemboursable());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Lignes de la vente", result));
    }


    /**
     * GET /api/v1/init/ventes-statistiques
     * Statistiques globales des ventes
     */
    @GetMapping("/ventes-statistiques")
    @Transactional(readOnly = true)
    @Operation(summary = "Statistiques des ventes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getVentesStatistiques() {
        log.info("GET /api/v1/init/ventes-statistiques");

        List<Vente> ventes = venteRepository.findAll();

        // Calculs
        long totalVentes = ventes.size();
        long ventesValidees = ventes.stream().filter(v -> v.getStatut() == StatutVente.VALIDEE).count();
        long ventesPartielles = ventes.stream().filter(v -> v.getStatut() == StatutVente.PARTIELLEMENT_PAYEE).count();

        BigDecimal caTotal = ventes.stream()
                .filter(v -> v.getStatut() == StatutVente.VALIDEE)
                .map(Vente::getMontantNetTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal caMutuelle = ventes.stream()
                .filter(v -> v.getStatut() == StatutVente.VALIDEE)
                .map(Vente::getMontantMutuelle)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remisesTotal = ventes.stream()
                .filter(v -> v.getStatut() == StatutVente.VALIDEE)
                .map(Vente::getMontantRemise)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalArticles = ventes.stream()
                .filter(v -> v.getStatut() == StatutVente.VALIDEE)
                .mapToInt(v -> v.getNombreArticles() != null ? v.getNombreArticles() : 0)
                .sum();

        BigDecimal panierMoyen = ventesValidees > 0
                ? caTotal.divide(BigDecimal.valueOf(ventesValidees), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Répartition par type
        Map<String, Long> parType = ventes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        v -> v.getType().name(),
                        java.util.stream.Collectors.counting()
                ));

        // Répartition par mode de paiement
        Map<String, Long> parPaiement = ventes.stream()
                .filter(v -> v.getModePaiement() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        v -> v.getModePaiement().name(),
                        java.util.stream.Collectors.counting()
                ));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalVentes", totalVentes);
        stats.put("ventesValidees", ventesValidees);
        stats.put("ventesPartiellementPayees", ventesPartielles);
        stats.put("chiffreAffaires", caTotal + " FCFA");
        stats.put("montantMutuelles", caMutuelle + " FCFA");
        stats.put("totalRemises", remisesTotal + " FCFA");
        stats.put("totalArticlesVendus", totalArticles);
        stats.put("panierMoyen", panierMoyen + " FCFA");
        stats.put("repartitionParType", parType);
        stats.put("repartitionParPaiement", parPaiement);

        return ResponseEntity.ok(ApiResponse.success("Statistiques des ventes", stats));
    }


    // ═══════════════════════════════════════════════════════════════════════════
    // PHARMACIES - VERSION AMÉLIORÉE (10 par commune)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-pharmacies-v2")
    @Operation(summary = "Initialiser les pharmacies de test (10 par commune)")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedPharmaciesV2() {
        log.info("🏥 Initialisation pharmacies V2 (10 par commune)...");
        int count = 0;

        // Récupérer les pharmaciens (en créer plus si nécessaire)
        List<Pharmacien> pharmaciens = pharmacienRepository.findAll();
        if (pharmaciens.size() < 20) {
            // Créer plus de pharmaciens
            seedMorePharmaciens(20 - pharmaciens.size());
            pharmaciens = pharmacienRepository.findAll();
        }

        // Récupérer les communes principales de Dakar
        List<Commune> communesDakar = communeRepository.findAll().stream()
                .filter(c -> {
                    try {
                        return c.getDepartement().getRegion().getNom().contains("Dakar");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .limit(5) // 5 communes
                .toList();

        if (communesDakar.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucune commune de Dakar trouvée. Exécutez d'abord /seed-localisation")
            );
        }

        log.info("📍 {} communes trouvées pour Dakar", communesDakar.size());

        // Données de base pour les pharmacies
        String[][] nomsPharmacies = {
                {"Pharmacie Principale", "Pharmacie Centrale", "Grande Pharmacie", "Pharmacie du Carrefour", "Pharmacie de l'Avenue"},
                {"Pharmacie du Marché", "Pharmacie de la Place", "Pharmacie Moderne", "Pharmacie du Quartier", "Pharmacie Populaire"}
        };

        int pharmacienIndex = 0;
        int codeCounter = 1;

        for (Commune commune : communesDakar) {
            log.info("🏥 Création de 10 pharmacies pour {}", commune.getNom());

            // Coordonnées de base pour cette commune (approximatives)
            double baseLat = 14.6928 + (communesDakar.indexOf(commune) * 0.02);
            double baseLng = -17.4467 + (communesDakar.indexOf(commune) * 0.02);

            for (int i = 0; i < 10; i++) {
                String nomPharmacie = nomsPharmacies[i / 5][i % 5] + " " + commune.getNom();
                String code = String.format("PHAR-%s-%03d",
                        commune.getCode().substring(0, Math.min(3, commune.getCode().length())).toUpperCase(),
                        codeCounter);

                if (pharmacieRepository.existsByCode(code)) {
                    codeCounter++;
                    continue;
                }

                Pharmacien proprietaire = pharmaciens.get(pharmacienIndex % pharmaciens.size());

                // Varier les coordonnées pour chaque pharmacie
                double lat = baseLat + (i * 0.003) - 0.015;
                double lng = baseLng + ((i % 3) * 0.004) - 0.006;

                Pharmacie pharmacie = Pharmacie.builder()
                        .nom(nomPharmacie)
                        .code(code)
                        .adresseComplete("Rue " + (i + 1) + ", " + commune.getNom())
                        .quartier(commune.getNom())
                        .latitude(lat)
                        .longitude(lng)
                        .telephone(String.format("+22133%07d", 8000000 + codeCounter))
                        .email("contact." + code.toLowerCase().replace("-", "") + "@sunufarmasi.sn")
                        .commune(commune)
                        .pharmacienProprietaire(proprietaire)
                        .statut(StatutPharmacie.VALIDEE)
                        .dateValidation(LocalDateTime.now().minusDays(30))
                        .accepteCommandes(true)
                        .proposeLivraison(i % 3 == 0) // 1 sur 3 propose la livraison
                        .rayonLivraisonKm(i % 3 == 0 ? 5 : null)
                        .notificationsActives(true)
                        .build();

                pharmacieRepository.save(pharmacie);

                // Mettre à jour le compteur du pharmacien
                proprietaire.ajouterPharmacie();
                pharmacienRepository.save(proprietaire);

                count++;
                codeCounter++;
                pharmacienIndex++;

                log.info("   ✅ {} créée", nomPharmacie);
            }
        }

        log.info("✅ {} pharmacies créées au total", count);

        return ResponseEntity.ok(ApiResponse.success(count + " pharmacies créées", Map.of(
                "count", count,
                "communes", communesDakar.size(),
                "pharmaciesParCommune", 10
        )));
    }

    /**
     * Créer des pharmaciens supplémentaires
     */
    private void seedMorePharmaciens(int count) {
        log.info("👨‍⚕️ Création de {} pharmaciens supplémentaires...", count);

        String[][] noms = {
                {"DIOP", "Amadou"}, {"NDIAYE", "Fatou"}, {"FALL", "Ibrahima"}, {"SARR", "Aminata"},
                {"GUEYE", "Moussa"}, {"DIALLO", "Aïssatou"}, {"BA", "Ousmane"}, {"SOW", "Mariama"},
                {"MBAYE", "Cheikh"}, {"FAYE", "Ndèye"}, {"SECK", "Pape"}, {"THIAM", "Sokhna"},
                {"KANE", "Modou"}, {"CISSE", "Khady"}, {"DIOUF", "Malick"}, {"NIANG", "Coumba"},
                {"TALL", "Babacar"}, {"WADE", "Rama"}, {"LY", "Aliou"}, {"TOURE", "Binta"}
        };

        int existingCount = (int) pharmacienRepository.count();

        for (int i = 0; i < count && i < noms.length; i++) {
            int idx = (existingCount + i) % noms.length;
            String telephone = String.format("+22177%07d", 5000000 + existingCount + i);

            if (!pharmacienRepository.existsByTelephone(telephone)) {
                Pharmacien pharmacien = Pharmacien.builder()
                        .nom(noms[idx][0])
                        .prenom(noms[idx][1])
                        .email(noms[idx][1].toLowerCase() + "." + noms[idx][0].toLowerCase() + (existingCount + i) + "@gmail.com")
                        .telephone(telephone)
                        .motDePasseHash(passwordEncoder.encode("password123"))
                        .numeroOrdreNational("ORD-SN-" + String.format("%03d", existingCount + i + 1))
                        .universiteFormation("UCAD")
                        .anneeDiplome(2010 + (i % 10))
                        .type(TypePharmacien.PROPRIETAIRE)
                        .plan(PlanAbonnementPharmacie.PREMIUM)
                        .statutAbonnement(StatutAbonnement.ACTIF)
                        .montantMensuel(new BigDecimal("25000"))
                        .essaiGratuit(false)
                        .dateDebutAbonnement(LocalDate.now().minusMonths(3))
                        .dateFinAbonnement(LocalDate.now().plusMonths(9))
                        .nombreEmployesMax(5)
                        .nombrePharmaciesMax(3)
                        .nombrePharmaciesActuelles(0)
                        .actif(true)
                        .valideParOrdre(true)
                        .compteVerifie(true)
                        .sexe(i % 2 == 0 ? "M" : "F")
                        .dateNaissance(LocalDate.of(1980 + (i % 15), (i % 12) + 1, (i % 28) + 1))
                        .build();
                pharmacienRepository.save(pharmacien);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GARDES - VERSION AMÉLIORÉE (3 pharmacies de garde par commune)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-gardes-v2")
    @Operation(summary = "Initialiser les gardes (3 par commune, semaine actuelle)")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedGardesV2() {
        log.info("📅 Initialisation gardes V2 (3 par commune)...");
        int planningCount = 0;
        int gardeCount = 0;

        // Récupérer les syndicats
        List<Syndicat> syndicats = syndicatRepository.findAll();
        if (syndicats.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Aucun syndicat trouvé. Exécutez d'abord /seed-syndicats")
            );
        }

        Syndicat syndicat = syndicats.get(0);

        // Calculer la semaine en cours (samedi → vendredi)
        LocalDate today = LocalDate.now();
        LocalDate debutSemaine = getSamediSemaine(today);
        LocalDate finSemaine = debutSemaine.plusDays(6); // Vendredi

        log.info("📆 Semaine de garde: {} → {}", debutSemaine, finSemaine);

        // Créer le planning pour la semaine en cours
        String titrePlanning = "Gardes Semaine " + debutSemaine.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                + " - " + debutSemaine.getYear();

        PlanningGarde planning = planningGardeRepository.findAll().stream()
                .filter(p -> p.getDateDebut().equals(debutSemaine))
                .findFirst()
                .orElseGet(() -> {
                    PlanningGarde p = PlanningGarde.builder()
                            .syndicat(syndicat)
                            .titre(titrePlanning)
                            .description("Planning des gardes - Semaine du " + debutSemaine + " au " + finSemaine)
                            .dateDebut(debutSemaine)
                            .dateFin(finSemaine)
                            .statut(StatutPlanning.PUBLIE)
                            .datePublication(LocalDateTime.now().minusDays(5))
                            .publicationAuto(false)
                            .notifierPharmacies(true)
                            .notifierEmail(true)
                            .notifierSms(false)
                            .creeParNom("Admin Système")
                            .build();
                    return planningGardeRepository.save(p);
                });
        planningCount = 1;

        // Récupérer les communes avec leurs pharmacies
        List<Commune> communes = communeRepository.findAll().stream()
                .filter(c -> {
                    try {
                        return c.getDepartement().getRegion().getNom().contains("Dakar");
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();

        for (Commune commune : communes) {
            // Récupérer les pharmacies de cette commune
            List<Pharmacie> pharmaciesCommune = pharmacieRepository.findByCommuneIdAndStatut(
                    commune.getId(), StatutPharmacie.VALIDEE);

            if (pharmaciesCommune.isEmpty()) {
                log.warn("⚠️ Aucune pharmacie pour la commune {}", commune.getNom());
                continue;
            }

            log.info("🏥 {} pharmacies trouvées pour {}", pharmaciesCommune.size(), commune.getNom());

            // Sélectionner 3 pharmacies pour la garde (ou moins si pas assez)
            int nbGardes = Math.min(3, pharmaciesCommune.size());

            for (int i = 0; i < nbGardes; i++) {
                Pharmacie pharmacie = pharmaciesCommune.get(i);

                // Vérifier si la garde existe déjà
                boolean gardeExiste = gardeRepository.findBySemaineAndCommune(debutSemaine, finSemaine, commune.getId())
                        .stream()
                        .anyMatch(g -> g.getPharmacie().getId().equals(pharmacie.getId()));

                if (gardeExiste) {
                    log.info("   ⏭️ Garde déjà existante pour {}", pharmacie.getNom());
                    continue;
                }

                // Déterminer le statut (EN_COURS si aujourd'hui dans la période)
                StatutGarde statutGarde;
                if (!debutSemaine.isAfter(today) && !finSemaine.isBefore(today)) {
                    statutGarde = StatutGarde.EN_COURS;
                } else if (finSemaine.isBefore(today)) {
                    statutGarde = StatutGarde.TERMINEE;
                } else {
                    statutGarde = StatutGarde.PLANIFIEE;
                }

                Garde garde = Garde.builder()
                        .planning(planning)
                        .pharmacie(pharmacie)
                        .dateDebut(debutSemaine)
                        .dateFin(finSemaine)
                        .numeroSemaine(debutSemaine.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()))
                        .commune(commune)
                        .departement(commune.getDepartement())
                        .zoneNom(commune.getNom())
                        .typeGarde(TypeGarde.JOUR_ET_NUIT)
                        .statut(statutGarde)
                        .confirmeParPharmacie(true)
                        .dateConfirmation(LocalDateTime.now().minusDays(3))
                        .notificationEnvoyee(true)
                        .latitude(pharmacie.getLatitude())
                        .longitude(pharmacie.getLongitude())
                        .build();

                gardeRepository.save(garde);
                gardeCount++;

                log.info("   ✅ Garde créée pour {} ({})", pharmacie.getNom(), statutGarde);
            }
        }

        // Créer aussi des gardes pour la semaine prochaine
        LocalDate debutSemaineProchaine = debutSemaine.plusDays(7);
        LocalDate finSemaineProchaine = debutSemaineProchaine.plusDays(6);

        log.info("📆 Création gardes semaine prochaine: {} → {}", debutSemaineProchaine, finSemaineProchaine);

        String titrePlanningProchain = "Gardes Semaine " + debutSemaineProchaine.get(java.time.temporal.WeekFields.ISO.weekOfYear())
                + " - " + debutSemaineProchaine.getYear();

        PlanningGarde planningProchain = planningGardeRepository.findAll().stream()
                .filter(p -> p.getDateDebut().equals(debutSemaineProchaine))
                .findFirst()
                .orElseGet(() -> {
                    PlanningGarde p = PlanningGarde.builder()
                            .syndicat(syndicat)
                            .titre(titrePlanningProchain)
                            .description("Planning des gardes - Semaine du " + debutSemaineProchaine + " au " + finSemaineProchaine)
                            .dateDebut(debutSemaineProchaine)
                            .dateFin(finSemaineProchaine)
                            .statut(StatutPlanning.PUBLIE)
                            .datePublication(LocalDateTime.now())
                            .publicationAuto(false)
                            .notifierPharmacies(true)
                            .notifierEmail(true)
                            .notifierSms(false)
                            .creeParNom("Admin Système")
                            .build();
                    return planningGardeRepository.save(p);
                });
        planningCount++;

        for (Commune commune : communes) {
            List<Pharmacie> pharmaciesCommune = pharmacieRepository.findByCommuneIdAndStatut(
                    commune.getId(), StatutPharmacie.VALIDEE);

            if (pharmaciesCommune.size() <= 3) continue;

            // Pour la semaine prochaine, prendre les pharmacies 4, 5, 6 (différentes de cette semaine)
            int startIdx = Math.min(3, pharmaciesCommune.size());
            int nbGardes = Math.min(3, pharmaciesCommune.size() - startIdx);

            for (int i = 0; i < nbGardes; i++) {
                Pharmacie pharmacie = pharmaciesCommune.get(startIdx + i);

                boolean gardeExiste = gardeRepository.findBySemaineAndCommune(debutSemaineProchaine, finSemaineProchaine, commune.getId())
                        .stream()
                        .anyMatch(g -> g.getPharmacie().getId().equals(pharmacie.getId()));

                if (gardeExiste) continue;

                Garde garde = Garde.builder()
                        .planning(planningProchain)
                        .pharmacie(pharmacie)
                        .dateDebut(debutSemaineProchaine)
                        .dateFin(finSemaineProchaine)
                        .numeroSemaine(debutSemaineProchaine.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()))
                        .commune(commune)
                        .departement(commune.getDepartement())
                        .zoneNom(commune.getNom())
                        .typeGarde(TypeGarde.JOUR_ET_NUIT)
                        .statut(StatutGarde.PLANIFIEE)
                        .confirmeParPharmacie(false)
                        .notificationEnvoyee(true)
                        .latitude(pharmacie.getLatitude())
                        .longitude(pharmacie.getLongitude())
                        .build();

                gardeRepository.save(garde);
                gardeCount++;
            }
        }

        log.info("✅ {} plannings et {} gardes créés", planningCount, gardeCount);

        return ResponseEntity.ok(ApiResponse.success(planningCount + " plannings et " + gardeCount + " gardes créés", Map.of(
                "plannings", planningCount,
                "gardes", gardeCount,
                "semaineEnCours", Map.of(
                        "debut", debutSemaine.toString(),
                        "fin", finSemaine.toString()
                ),
                "semaineProchaine", Map.of(
                        "debut", debutSemaineProchaine.toString(),
                        "fin", finSemaineProchaine.toString()
                )
        )));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEED ALL V2 - Initialisation complète améliorée
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/seed-all-v2")
    @Operation(summary = "🚀 Initialiser TOUTES les données V2 (avec plus de pharmacies)")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAllV2() {
        log.warn("⚠️ SEED ALL V2 - Initialisation complète des données de test");

        Map<String, Object> results = new LinkedHashMap<>();

        // 1. Localisation
        try {
            var locResult = seedLocalisation();
            results.put("1_localisation", locResult.getBody().data());
        } catch (Exception e) {
            results.put("1_localisation", "Erreur: " + e.getMessage());
            log.error("Erreur localisation", e);
        }

        // 2. Users
        try {
            var userResult = seedUsers();
            results.put("2_users", userResult.getBody().data());
        } catch (Exception e) {
            results.put("2_users", "Erreur: " + e.getMessage());
        }

        // 3. Pharmaciens
        try {
            var pharmResult = seedPharmaciens();
            results.put("3_pharmaciens", pharmResult.getBody().data());
        } catch (Exception e) {
            results.put("3_pharmaciens", "Erreur: " + e.getMessage());
        }

        // 4. Pharmacies V2 (10 par commune)
        try {
            var pharmacieResult = seedPharmaciesV2();
            results.put("4_pharmacies", pharmacieResult.getBody().data());
        } catch (Exception e) {
            results.put("4_pharmacies", "Erreur: " + e.getMessage());
            log.error("Erreur pharmacies", e);
        }

        // 5. Syndicats
        try {
            var syndicatResult = seedSyndicats();
            results.put("5_syndicats", syndicatResult.getBody().data());
        } catch (Exception e) {
            results.put("5_syndicats", "Erreur: " + e.getMessage());
        }

        // 6. Employés
        try {
            var employeResult = seedEmployes();
            results.put("6_employes", employeResult.getBody().data());
        } catch (Exception e) {
            results.put("6_employes", "Erreur: " + e.getMessage());
        }

        // 7. Gardes V2 (3 par commune)
        try {
            var gardeResult = seedGardesV2();
            results.put("7_gardes", gardeResult.getBody().data());
        } catch (Exception e) {
            results.put("7_gardes", "Erreur: " + e.getMessage());
            log.error("Erreur gardes", e);
        }

        // 8. Produits et Stock
        try {
            var stockResult = seedStock();
            results.put("8_stock", stockResult.getBody().data());
        } catch (Exception e) {
            results.put("8_stock", "Erreur: " + e.getMessage());
        }

        // 9-16. Autres seeds
        try { seedSubscriptionPlans(); results.put("9_plans", "OK"); } catch (Exception e) { results.put("9_plans", "Erreur"); }
        try { seedPatients(); results.put("10_patients", "OK"); } catch (Exception e) { results.put("10_patients", "Erreur"); }
        try { seedSubscriptions(); results.put("11_subscriptions", "OK"); } catch (Exception e) { results.put("11_subscriptions", "Erreur"); }
        try { seedPayments(); results.put("12_payments", "OK"); } catch (Exception e) { results.put("12_payments", "Erreur"); }
        try { seedMutuelles(); results.put("13_mutuelles", "OK"); } catch (Exception e) { results.put("13_mutuelles", "Erreur"); }
        try { seedAdherents(); results.put("14_adherents", "OK"); } catch (Exception e) { results.put("14_adherents", "Erreur"); }
        try { seedContratsMutuelles(); results.put("15_contrats", "OK"); } catch (Exception e) { results.put("15_contrats", "Erreur"); }
        try { seedVentes(); results.put("16_ventes", "OK"); } catch (Exception e) { results.put("16_ventes", "Erreur"); }

        log.info("✅ SEED ALL V2 terminé");

        return ResponseEntity.ok(ApiResponse.success("Initialisation V2 complète terminée", results));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINT : VÉRIFIER LES GARDES EN COURS
    // ═══════════════════════════════════════════════════════════════════════════

    @GetMapping("/gardes-en-cours")
    @Operation(summary = "Vérifier les gardes en cours")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGardesEnCours() {
        LocalDate today = LocalDate.now();
        LocalDate debutSemaine = getSamediSemaine(today);
        LocalDate finSemaine = debutSemaine.plusDays(6);

        List<Garde> gardesEnCours = gardeRepository.findGardesSemaineEnCours(today);

        // Grouper par commune
        Map<String, List<Map<String, Object>>> gardesParCommune = new LinkedHashMap<>();

        for (Garde garde : gardesEnCours) {
            String communeNom = garde.getCommune() != null ? garde.getCommune().getNom() : "Inconnu";

            gardesParCommune.computeIfAbsent(communeNom, k -> new ArrayList<>());

            Map<String, Object> gardeInfo = new LinkedHashMap<>();
            gardeInfo.put("id", garde.getId());
            gardeInfo.put("pharmacie", garde.getPharmacie().getNom());
            gardeInfo.put("pharmacieCode", garde.getPharmacie().getCode());
            gardeInfo.put("adresse", garde.getPharmacie().getAdresseComplete());
            gardeInfo.put("telephone", garde.getPharmacie().getTelephone());
            gardeInfo.put("latitude", garde.getPharmacie().getLatitude());
            gardeInfo.put("longitude", garde.getPharmacie().getLongitude());
            gardeInfo.put("dateDebut", garde.getDateDebut().toString());
            gardeInfo.put("dateFin", garde.getDateFin().toString());
            gardeInfo.put("statut", garde.getStatut().name());
            gardeInfo.put("estSemaineEnCours", garde.estSemaineEnCours());

            gardesParCommune.get(communeNom).add(gardeInfo);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dateAujourdhui", today.toString());
        result.put("semaineEnCours", Map.of(
                "debut", debutSemaine.toString(),
                "fin", finSemaine.toString(),
                "numeroSemaine", debutSemaine.get(java.time.temporal.WeekFields.ISO.weekOfYear())
        ));
        result.put("totalGardes", gardesEnCours.size());
        result.put("nombreCommunes", gardesParCommune.size());
        result.put("gardesParCommune", gardesParCommune);

        return ResponseEntity.ok(ApiResponse.success("Gardes en cours", result));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENDPOINT : RESET ET RE-SEED GARDES
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/reset-gardes")
    @Operation(summary = "⚠️ Supprimer et recréer les gardes")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetGardes() {
        log.warn("⚠️ RESET GARDES - Suppression de toutes les gardes...");

        // Supprimer toutes les gardes
        long gardesDeleted = gardeRepository.count();
        gardeRepository.deleteAll();

        // Supprimer tous les plannings
        long planningsDeleted = planningGardeRepository.count();
        planningGardeRepository.deleteAll();

        log.info("🗑️ {} gardes et {} plannings supprimés", gardesDeleted, planningsDeleted);

        // Recréer avec V2
        var result = seedGardesV2();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gardesSupprimees", gardesDeleted);
        response.put("planningsSupprimees", planningsDeleted);
        response.put("nouvellesGardes", result.getBody().data());

        return ResponseEntity.ok(ApiResponse.success("Reset gardes terminé", response));
    }

}