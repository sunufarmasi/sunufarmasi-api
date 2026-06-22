package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.*;
import sn.sunufarmasi.localisation.enums.TypeCommune;
import sn.sunufarmasi.localisation.repository.*;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.PlanAbonnementPharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;
import sn.sunufarmasi.user.entity.User;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;
import sn.sunufarmasi.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seed automatique au démarrage — exécuté en PREMIER (Order 1).
 * Crée : localisation + users + pharmaciens + syndicats.
 * PharmaciesDataSeeder (Order 2) crée ensuite les pharmacies + gardes.
 *
 * Activé via app.seed.pharmacies=true dans application.yml.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.seed.pharmacies", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class MasterDataSeeder implements ApplicationRunner {

    private final PaysRepository         paysRepository;
    private final RegionRepository       regionRepository;
    private final DepartementRepository  departementRepository;
    private final CommuneRepository      communeRepository;
    private final UserRepository         userRepository;
    private final PharmacienRepository   pharmacienRepository;
    private final SyndicatRepository     syndicatRepository;
    private final PasswordEncoder        passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        log.info("🚀 MasterDataSeeder — démarrage seed initial...");

        try { seedLocalisation(); }
        catch (Exception e) { log.error("   ❌ Localisation : {}", e.getMessage()); }

        try { seedUsers(); }
        catch (Exception e) { log.error("   ❌ Users : {}", e.getMessage()); }

        try { seedPharmaciens(); }
        catch (Exception e) { log.error("   ❌ Pharmaciens : {}", e.getMessage()); }

        try { seedSyndicats(); }
        catch (Exception e) { log.error("   ❌ Syndicats : {}", e.getMessage()); }

        log.info("✅ MasterDataSeeder — terminé");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 1. LOCALISATION
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    protected void seedLocalisation() {
        log.info("   🌍 Mise à jour localisation du Sénégal (idempotent)...");

        Pays senegal = paysRepository.findByCode("SN").orElseGet(() ->
            paysRepository.save(Pays.builder()
                .code("SN").codeIso2("SN").codeIso3("SEN")
                .nom("Sénégal").nomEn("Senegal").capitale("Dakar")
                .indicatifTelephonique("+221").devise("XOF")
                .fuseauHoraire("Africa/Dakar").drapeau("🇸🇳").actif(true)
                .build()));

        Object[][] regionsData = {
            {"DK", "Dakar",       "Dakar",       14.7167, -17.4677, new Object[][]{
                {"DK", "Dakar",       new String[]{"Plateau","Médina","Grand Dakar","Parcelles Assainies","Almadies","Ouakam","Yoff","Ngor","Mermoz","Fann"}},
                {"GU", "Guédiawaye", new String[]{"Guédiawaye","Sam Notaire","Wakhinane Nimzatt"}},
                {"PK", "Pikine",      new String[]{"Pikine","Thiaroye","Diamaguene","Keur Massar"}},
                {"RU", "Rufisque",    new String[]{"Rufisque","Bargny","Diamniadio","Sébikotane"}}
            }},
            {"TH", "Thiès",       "Thiès",       14.7886, -16.9260, new Object[][]{
                {"TH", "Thiès",      new String[]{"Thiès Nord","Thiès Sud","Thiès Est","Thiès Ouest"}},
                {"MB", "Mbour",      new String[]{"Mbour","Saly","Somone","Ngaparou","Joal-Fadiouth"}},
                {"TI", "Tivaouane", new String[]{"Tivaouane","Mboro","Mékhé"}}
            }},
            {"SL", "Saint-Louis", "Saint-Louis", 16.0326, -16.4818, new Object[][]{
                {"SL", "Saint-Louis", new String[]{"Saint-Louis Nord","Saint-Louis Sud","Sor"}},
                {"DG", "Dagana",      new String[]{"Dagana","Richard-Toll","Ross-Béthio"}},
                {"PD", "Podor",       new String[]{"Podor","Ndioum"}}
            }},
            {"ZG", "Ziguinchor",  "Ziguinchor",  12.5833, -16.2719, new Object[][]{
                {"ZG", "Ziguinchor", new String[]{"Ziguinchor","Niaguis"}},
                {"OU", "Oussouye",   new String[]{"Oussouye","Cap Skirring"}},
                {"BI", "Bignona",    new String[]{"Bignona","Diouloulou"}}
            }},
            {"KL", "Kaolack",     "Kaolack",     14.1652, -16.0758, new Object[][]{
                {"KL", "Kaolack",      new String[]{"Kaolack","Kahone","Ndoffane"}},
                {"GN", "Guinguinéo",   new String[]{"Guinguinéo"}},
                {"NI", "Nioro du Rip", new String[]{"Nioro du Rip","Paoskoto"}}
            }},
            {"DI", "Diourbel",    "Diourbel",    14.6552, -16.2282, new Object[][]{
                {"DI", "Diourbel",  new String[]{"Diourbel","Ndame","Ndindy"}},
                {"MB", "Mbacké",    new String[]{"Mbacké","Touba","Dahra","Kael"}},
                {"BA", "Bambey",    new String[]{"Bambey","Diourbel","Baba Garage"}}
            }},
            {"LG", "Louga",       "Louga",       15.6172, -16.2311, new Object[][]{
                {"LG", "Louga",     new String[]{"Louga","Sakal","Kébémer"}},
                {"LN", "Linguère",  new String[]{"Linguère","Barkedji"}},
                {"KB", "Kébémer",   new String[]{"Kébémer","Thiès Kebemer"}}
            }}
        };

        int communes = 0;
        for (Object[] rd : regionsData) {
            String regCode = (String) rd[0];
            Region region = regionRepository.save(Region.builder()
                    .pays(senegal).code(regCode).nom((String) rd[1])
                    .chefLieu((String) rd[2]).latitude((Double) rd[3]).longitude((Double) rd[4])
                    .actif(true).build());

            for (Object[] dd : (Object[][]) rd[5]) {
                String deptCode = (String) dd[0];
                Departement dept = departementRepository.save(Departement.builder()
                        .region(region).code(deptCode).nom((String) dd[1])
                        .chefLieu((String) dd[1]).actif(true).build());

                for (String cNom : (String[]) dd[2]) {
                    String cCode = cNom.toUpperCase().replace(" ", "_")
                            .substring(0, Math.min(10, cNom.length()));
                    if (!communeRepository.existsByCodeAndDepartementId(cCode, dept.getId())) {
                        communeRepository.save(Commune.builder()
                                .departement(dept).code(cCode).nom(cNom)
                                .type(TypeCommune.COMMUNE).zoneUrbaine(true).actif(true)
                                .build());
                        communes++;
                    }
                }
            }
        }
        log.info("   ✅ Localisation : 5 régions, {} communes créées", communes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. USERS
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    protected void seedUsers() {
        if (userRepository.existsByEmail("alamine@sunufarmasi.sn")) {
            log.info("   👤 Users déjà en base — ignorés");
            return;
        }

        Object[][] usersData = {
            {"alamine@sunufarmasi.sn", "MBENGUE", "Mohamed AL Amine", "+221771000001", RoleUser.ADMIN},
            {"admin@sunufarmasi.sn",   "DIOP",    "Amadou",           "+221771234567", RoleUser.ADMIN},
            {"employe1@sunufarmasi.sn","SOW",     "Abdoulaye",        "+221778901234", RoleUser.EMPLOYE},
        };

        int count = 0;
        for (Object[] d : usersData) {
            String email = (String) d[0];
            if (!userRepository.existsByEmail(email)) {
                userRepository.save(User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("password123"))
                        .nom((String) d[1]).prenom((String) d[2])
                        .telephone((String) d[3]).role((RoleUser) d[4])
                        .statut(StatutUser.ACTIF).emailVerifie(true)
                        .build());
                count++;
            }
        }
        log.info("   ✅ {} users créés", count);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. PHARMACIENS
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    protected void seedPharmaciens() {
        if (pharmacienRepository.count() > 0) {
            log.info("   💊 Pharmaciens déjà en base — ignorés");
            return;
        }

        Object[][] data = {
            {"FALL",   "Ibrahima", "pharmacien.plateau@gmail.com",  "+221773456789", "ORD-SN-001", "UCAD"},
            {"SARR",   "Aminata",  "pharmacien.medina@gmail.com",   "+221774567890", "ORD-SN-002", "UCAD"},
            {"GUEYE",  "Moussa",   "pharmacien.almadies@gmail.com", "+221775678901", "ORD-SN-003", "UGB"},
            {"DIALLO", "Fatou",    "pharmacien.pikine@gmail.com",   "+221776789012", "ORD-SN-004", "UCAD"},
            {"NDIAYE", "Omar",     "pharmacien.thies@gmail.com",    "+221777890123", "ORD-SN-005", "UCAD"},
        };

        int count = 0;
        for (Object[] d : data) {
            String tel = (String) d[3];
            if (!pharmacienRepository.existsByTelephone(tel)) {
                pharmacienRepository.save(Pharmacien.builder()
                        .nom((String) d[0]).prenom((String) d[1])
                        .email((String) d[2]).telephone(tel)
                        .motDePasseHash(passwordEncoder.encode("password123"))
                        .numeroOrdreNational((String) d[4])
                        .universiteFormation((String) d[5])
                        .anneeDiplome(2015 + count)
                        .type(TypePharmacien.PROPRIETAIRE)
                        .plan(PlanAbonnementPharmacie.PREMIUM)
                        .statutAbonnement(StatutAbonnement.ACTIF)
                        .montantMensuel(new BigDecimal("25000"))
                        .essaiGratuit(false)
                        .dateDebutAbonnement(LocalDate.now().minusMonths(3))
                        .dateFinAbonnement(LocalDate.now().plusMonths(9))
                        .nombreEmployesMax(5).nombrePharmaciesMax(1)
                        .nombrePharmaciesActuelles(0)
                        .actif(true).valideParOrdre(true).compteVerifie(true)
                        .sexe(count % 2 == 0 ? "M" : "F")
                        .dateNaissance(LocalDate.of(1985, 1, 1).plusYears(count))
                        .build());
                count++;
            }
        }
        log.info("   ✅ {} pharmaciens créés", count);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. SYNDICATS
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    protected void seedSyndicats() {
        if (departementRepository.count() == 0) {
            log.warn("   ⚠️ Syndicats ignorés : aucun département en base");
            return;
        }

        // [nom, sigle, code, username, type, telephone, email, zoneCode, zoneType]
        // zoneType: "dept" ou "commune"
        Object[][] data = {
            {"Syndicat des Pharmaciens de Dakar",       "SPD",   "SYN-DK-001",    "syndicat.dakar",       TypeSyndicat.DEPARTEMENT, "+221338001001", "dakar@syndicat.sn",      "DK",    "dept"},
            {"Syndicat des Pharmaciens de Thiès",       "SPT",   "SYN-TH-001",    "syndicat.thies",       TypeSyndicat.DEPARTEMENT, "+221339001001", "thies@syndicat.sn",      "TH",    "dept"},
            {"Syndicat des Pharmaciens de Mbacké",      "SPMB",  "SYN-MB-001",    "syndicat.mbacke",      TypeSyndicat.DEPARTEMENT, "+221339002001", "mbacke@syndicat.sn",     "Mbacké","dept"},
            {"Syndicat des Pharmaciens de Mbour",       "SPMbr", "SYN-MBR-001",   "syndicat.mbour",       TypeSyndicat.DEPARTEMENT, "+221339003001", "mbour@syndicat.sn",      "Mbour", "dept"},
            {"Syndicat des Pharmaciens de Saint-Louis", "SPSL",  "SYN-SL-001",    "syndicat.saintlouis",  TypeSyndicat.DEPARTEMENT, "+221338001003", "saintlouis@syndicat.sn", "SL",    "dept"},
            {"Syndicat des Pharmaciens de Kaolack",     "SPK",   "SYN-KL-001",    "syndicat.kaolack",     TypeSyndicat.DEPARTEMENT, "+221338002001", "kaolack@syndicat.sn",    "KL",    "dept"},
            {"Syndicat des Pharmaciens du Plateau",     "SPP",   "SYN-DK-002",    "syndicat.plateau",     TypeSyndicat.COMMUNE,     "+221338001002", "plateau@syndicat.sn",    "Plateau","commune"},
            {"Syndicat des Pharmaciens de Pikine",      "SPPk",  "SYN-PK-001",    "syndicat.pikine",      TypeSyndicat.COMMUNE,     "+221338001004", "pikine@syndicat.sn",     "Pikine","commune"},
            {"Syndicat Thiès — Secrétariat Nord",       "SPT-N", "SYN-TH-001-S1", "syndicat.thies.sec1",  TypeSyndicat.COMMUNE,     "+221339001011", "thies.nord@syndicat.sn", "Thiès Nord","commune"},
            {"Syndicat Thiès — Secrétariat Sud",        "SPT-S", "SYN-TH-001-S2", "syndicat.thies.sec2",  TypeSyndicat.COMMUNE,     "+221339001012", "thies.sud@syndicat.sn",  "Thiès Sud","commune"},
        };

        int count = 0;
        for (Object[] d : data) {
            String code = (String) d[2];
            if (syndicatRepository.existsByCode(code)) {
                // Correction zone si syndicat déjà existant (ex: Mbacké/Mbour avec même code "MB")
                repairSyndicatZoneIfNeeded(code, (String) d[7], (String) d[8]);
                continue;
            }

            TypeSyndicat type    = (TypeSyndicat) d[4];
            String       zone    = (String) d[7];
            String       zoneType= (String) d[8];

            Syndicat.SyndicatBuilder b = Syndicat.builder()
                    .nom((String) d[0]).sigle((String) d[1]).code(code)
                    .username((String) d[3])
                    .motDePasseHash(passwordEncoder.encode("password123"))
                    .type(type).telephone((String) d[5]).email((String) d[6])
                    .nomResponsable("Dr. " + (count % 2 == 0 ? "DIOP" : "NDIAYE"))
                    .telephoneResponsable("+22177" + (1000000 + count))
                    .plan(type == TypeSyndicat.DEPARTEMENT ? PlanAbonnementSyndicat.DEPARTEMENT : PlanAbonnementSyndicat.COMMUNE)
                    .statutAbonnement(StatutAbonnement.ACTIF)
                    .montantMensuel(type == TypeSyndicat.DEPARTEMENT ? new BigDecimal("8000") : new BigDecimal("5500"))
                    .essaiGratuit(false)
                    .dateDebutAbonnement(LocalDate.now().minusMonths(2))
                    .dateFinAbonnement(LocalDate.now().plusMonths(10))
                    .nombrePharmaciesMax(type == TypeSyndicat.DEPARTEMENT ? 200 : 50)
                    .nombrePharmaciesActuelles(0)
                    .statut(StatutSyndicat.ACTIF);

            if ("dept".equals(zoneType)) {
                departementRepository.findAll().stream()
                        .filter(dep -> dep.getCode().equals(zone) || dep.getNom().equalsIgnoreCase(zone))
                        .findFirst()
                        .ifPresent(dept -> b.departement(dept).region(dept.getRegion()));
            } else {
                communeRepository.findAll().stream()
                        .filter(com -> com.getNom().equalsIgnoreCase(zone))
                        .findFirst()
                        .ifPresent(com -> b.commune(com).region(com.getDepartement().getRegion()));
            }

            syndicatRepository.save(b.build());
            count++;
        }
        log.info("   ✅ {} syndicats créés", count);
    }

    /** Corrige la zone géographique d'un syndicat existant si elle est incorrecte. */
    private void repairSyndicatZoneIfNeeded(String code, String expectedZone, String zoneType) {
        syndicatRepository.findByCode(code).ifPresent(s -> {
            boolean needsRepair = false;
            if ("dept".equals(zoneType) && s.getDepartement() != null) {
                // Vérifier que le département actuel correspond au nom attendu
                needsRepair = !s.getDepartement().getNom().equalsIgnoreCase(expectedZone)
                           && !s.getDepartement().getCode().equals(expectedZone);
            } else if ("commune".equals(zoneType) && s.getCommune() != null) {
                needsRepair = !s.getCommune().getNom().equalsIgnoreCase(expectedZone);
            }

            if (!needsRepair) return;

            log.info("   🔧 Correction zone syndicat {} : {} → {}", code, s.getDepartement() != null ? s.getDepartement().getNom() : "?", expectedZone);
            if ("dept".equals(zoneType)) {
                departementRepository.findAll().stream()
                        .filter(dep -> dep.getNom().equalsIgnoreCase(expectedZone) || dep.getCode().equals(expectedZone))
                        .findFirst()
                        .ifPresent(dept -> {
                            s.setDepartement(dept);
                            s.setRegion(dept.getRegion());
                            syndicatRepository.save(s);
                            log.info("   ✅ Zone corrigée → {}", dept.getNom());
                        });
            } else {
                communeRepository.findAll().stream()
                        .filter(com -> com.getNom().equalsIgnoreCase(expectedZone))
                        .findFirst()
                        .ifPresent(com -> {
                            s.setCommune(com);
                            s.setRegion(com.getDepartement().getRegion());
                            syndicatRepository.save(s);
                            log.info("   ✅ Zone corrigée → {}", com.getNom());
                        });
            }
        });
    }
}
