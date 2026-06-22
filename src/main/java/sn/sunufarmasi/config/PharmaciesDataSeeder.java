package sn.sunufarmasi.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.garde.entity.Garde;
import sn.sunufarmasi.garde.entity.PlanningGarde;
import sn.sunufarmasi.garde.entity.StatutPlanning;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;
import sn.sunufarmasi.garde.repository.GardeRepository;
import sn.sunufarmasi.garde.repository.PlanningGardeRepository;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepository;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed automatique : 3 pharmacies par commune + gardes réparties sur 2 plannings.
 * Activé via app.seed.pharmacies=true dans application.yml.
 *
 * Résultat :
 *  - ~3 pharmacies par commune (toutes ACTIVE)
 *  - Planning semaine courante  (PUBLIE) → gardes pour communes paires
 *  - Planning semaine prochaine (PUBLIE) → gardes pour communes impaires
 *  - La pharmacie n°2 de chaque commune n'est jamais de garde (non assignée)
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.seed.pharmacies", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PharmaciesDataSeeder implements ApplicationRunner {

    private final PharmacieRepository       pharmacieRepository;
    private final PharmacienRepository      pharmacienRepository;
    private final CommuneRepository         communeRepository;
    private final SyndicatRepository        syndicatRepository;
    private final PlanningGardeRepository   planningGardeRepository;
    private final GardeRepository           gardeRepository;

    // Coordonnées par code de région (fallback si commune sans coordonnées)
    private static final Map<String, double[]> REGION_COORDS = new HashMap<>() {{
        put("DK", new double[]{14.7167, -17.4677});  // Dakar
        put("TH", new double[]{14.7886, -16.9260});  // Thiès
        put("SL", new double[]{16.0326, -16.4818});  // Saint-Louis
        put("ZG", new double[]{12.5833, -16.2719});  // Ziguinchor
        put("KL", new double[]{14.1652, -16.0758});  // Kaolack
    }};

    // 3 modèles de noms par commune
    private static final String[] NOM_PREFIXES = {
            "Pharmacie Centrale de ",
            "Pharmacie Nouvelle ",
            "Pharmacie "
    };

    // Suffixes d'adresse variés
    private static final String[] ADRESSE_SUFFIXES = {
            "Avenue Principale",
            "Rue du Marché",
            "Quartier Centre"
    };

    @Override
    public void run(ApplicationArguments args) {
        try {
            seedPharmaciesEtGardes();
        } catch (Exception e) {
            log.error("❌ Seed pharmacies échoué : {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected void seedPharmaciesEtGardes() {
        long existantes = pharmacieRepository.count();
        if (existantes > 0) {
            log.info("✅ {} pharmacies déjà en base — seed ignoré", existantes);
            return;
        }

        log.info("🏥 Démarrage du seed : pharmacies par commune + plannings de garde...");

        // ── 1. Charger les communes ────────────────────────────────────────────
        List<Commune> communes = communeRepository.findAll();
        if (communes.isEmpty()) {
            log.warn("⚠️ Aucune commune — exécutez /api/v1/init/seed-localisation d'abord");
            return;
        }
        log.info("   {} communes trouvées", communes.size());

        List<Pharmacien> pharmaciens = pharmacienRepository.findAll();

        // ── 2. Créer 3 pharmacies par commune ──────────────────────────────────── ─ ─ ─
        // pharmaciesParCommune[communeIdx][0..2] = les 3 pharmacies créées
        List<List<Pharmacie>> pharmaciesParCommune = new ArrayList<>();
        int totalCrees = 0;
        int pharmacienIndex = 0;

        for (int ci = 0; ci < communes.size(); ci++) {
            Commune commune = communes.get(ci);
            List<Pharmacie> groupeCommune = new ArrayList<>();

            String regionCode = getRegionCode(commune);
            double[] regionCoords = REGION_COORDS.getOrDefault(regionCode, new double[]{14.7167, -17.4677});

            for (int pi = 0; pi < 3; pi++) {
                String nomPharmacie = NOM_PREFIXES[pi] + commune.getNom();
                String code = "PHAR-" + sanitize(commune.getCode()) + "-" + (pi + 1);

                if (pharmacieRepository.existsByCode(code)) {
                    log.debug("   ⚠️ {} déjà en base — ignorée", code);
                    continue;
                }

                Pharmacien proprio = pharmaciens.isEmpty() ? null
                        : pharmaciens.get(pharmacienIndex++ % pharmaciens.size());

                // Décalage léger des coordonnées pour chaque pharmacie
                double latOffset = (ci * 0.003) + (pi * 0.001);
                double lngOffset = (ci * 0.002) + (pi * 0.001);

                double lat = commune.getLatitude() != null
                        ? commune.getLatitude() + (pi * 0.001)
                        : regionCoords[0] + latOffset;
                double lng = commune.getLongitude() != null
                        ? commune.getLongitude() + (pi * 0.001)
                        : regionCoords[1] + lngOffset;

                // Trouver le syndicat qui couvre cette commune
                Syndicat syndicatCommune = trouverSyndicatPourCommune(commune);

                Pharmacie pharmacie = Pharmacie.builder()
                        .nom(nomPharmacie)
                        .code(code)
                        .adresseComplete(ADRESSE_SUFFIXES[pi] + ", " + commune.getNom())
                        .quartier(commune.getNom())
                        .telephone("+2213382" + String.format("%05d", totalCrees + 10000))
                        .email("contact." + code.toLowerCase().replace("-", ".") + "@sunufarmasi.sn")
                        .latitude(lat)
                        .longitude(lng)
                        .commune(commune)
                        .syndicat(syndicatCommune)  // peut être null si zone non couverte
                        .pharmacienProprietaire(proprio)
                        .statut(StatutPharmacie.ACTIVE)
                        .dateValidation(LocalDateTime.now().minusDays(30 + ci))
                        .accepteCommandes(true)
                        .proposeLivraison(pi == 0)
                        .rayonLivraisonKm(pi == 0 ? 5 : null)
                        .notificationsActives(true)
                        .build();

                pharmacieRepository.save(pharmacie);
                groupeCommune.add(pharmacie);
                totalCrees++;
            }

            pharmaciesParCommune.add(groupeCommune);
            log.info("   ✅ Commune {} : {} pharmacies créées", commune.getNom(), groupeCommune.size());
        }

        log.info("📦 {} pharmacies créées au total ({} communes)", totalCrees, communes.size());

        // ── 3. Vérifier qu'il existe un syndicat ──────────────────────────────
        List<Syndicat> syndicats = syndicatRepository.findAll();
        if (syndicats.isEmpty()) {
            log.warn("⚠️ Aucun syndicat — plannings de garde non créés");
            log.warn("   → Exécutez /api/v1/init/seed-syndicats puis relancez le seed");
            return;
        }
        Syndicat syndicat = syndicats.get(0);

        // ── 4. Créer 2 plannings : semaine courante + semaine prochaine ────────
        LocalDate today        = LocalDate.now();
        LocalDate debutCourant = Garde.getDebutSemaine(today);
        LocalDate finCourant   = debutCourant.plusDays(6);
        int semaineCourante    = debutCourant.get(WeekFields.ISO.weekOfWeekBasedYear());

        LocalDate debutProchain = debutCourant.plusWeeks(1);
        LocalDate finProchain   = debutProchain.plusDays(6);
        int semaineProchaine    = debutProchain.get(WeekFields.ISO.weekOfWeekBasedYear());

        PlanningGarde planningCourant  = creerPlanning(syndicat, debutCourant,  finCourant,  semaineCourante,  "courante");
        PlanningGarde planningProchain = creerPlanning(syndicat, debutProchain, finProchain, semaineProchaine, "prochaine");

        // ── 5. Assigner les gardes ─────────────────────────────────────────────
        // Communes paires  → garde semaine COURANTE  (pharmacie[0] = Centrale)
        // Communes impaires → garde semaine PROCHAINE (pharmacie[1] = Nouvelle)
        // Pharmacie[2] = "Pharmacie [Commune]" → jamais de garde dans ce seed
        int gardesCourant  = 0;
        int gardesProchain = 0;

        for (int ci = 0; ci < pharmaciesParCommune.size(); ci++) {
            List<Pharmacie> groupe = pharmaciesParCommune.get(ci);
            if (groupe.isEmpty()) continue;

            Commune commune = communes.get(ci);
            boolean estPaire = (ci % 2 == 0);

            PlanningGarde planning    = estPaire ? planningCourant : planningProchain;
            LocalDate     dateDebut   = estPaire ? debutCourant    : debutProchain;
            LocalDate     dateFin     = estPaire ? finCourant       : finProchain;
            int           numSemaine  = estPaire ? semaineCourante  : semaineProchaine;
            Pharmacie     pharmacie   = groupe.get(0);  // toujours la "Centrale"

            try {
                Garde garde = Garde.builder()
                        .planning(planning)
                        .pharmacie(pharmacie)
                        .dateDebut(dateDebut)
                        .dateFin(dateFin)
                        .numeroSemaine(numSemaine)
                        .commune(commune)
                        .zoneNom(commune.getNom())
                        .typeGarde(TypeGarde.JOUR_ET_NUIT)
                        .statut(StatutGarde.CONFIRMEE)
                        .confirmeParPharmacie(true)
                        .build();

                gardeRepository.save(garde);

                if (estPaire) gardesCourant++;
                else          gardesProchain++;

            } catch (Exception e) {
                log.warn("   ⚠️ Garde ignorée pour {} : {}", commune.getNom(), e.getMessage());
            }
        }

        log.info("🎉 Seed terminé :");
        log.info("   📦 {} pharmacies ({} communes × ~3)", totalCrees, communes.size());
        log.info("   📅 Planning semaine courante  : {} gardes", gardesCourant);
        log.info("   📅 Planning semaine prochaine : {} gardes", gardesProchain);
        log.info("   🏪 Pharmacies sans garde cette semaine : {}",
                totalCrees - gardesCourant);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private PlanningGarde creerPlanning(Syndicat syndicat,
                                        LocalDate debut, LocalDate fin,
                                        int numSemaine, String label) {
        // Vérifier si ce planning existe déjà (contrainte unique syndicat+debut+fin)
        return planningGardeRepository
                .findBySyndicatIdAndDateDebutAndDateFin(syndicat.getId(), debut, fin)
                .orElseGet(() -> {
                    PlanningGarde p = PlanningGarde.builder()
                            .syndicat(syndicat)
                            .titre("Planning de garde - Semaine " + numSemaine + " (" + label + ")")
                            .description("Planning de test généré automatiquement - semaine " + label)
                            .dateDebut(debut)
                            .dateFin(fin)
                            .statut(StatutPlanning.PUBLIE)
                            .datePublication(LocalDateTime.now())
                            .notifierPharmacies(false)
                            .build();
                    PlanningGarde saved = planningGardeRepository.save(p);
                    log.info("   📅 Planning créé : {} ({} → {})", saved.getTitre(), debut, fin);
                    return saved;
                });
    }

    private String getRegionCode(Commune commune) {
        try {
            return commune.getDepartement().getRegion().getCode();
        } catch (Exception e) {
            return "DK";
        }
    }

    private String sanitize(String s) {
        if (s == null) return "XX";
        return s.toUpperCase().replace(" ", "").substring(0, Math.min(6, s.length()));
    }

    /**
     * Trouve le syndicat qui couvre une commune :
     * 1. Syndicat de type COMMUNE couvrant exactement cette commune
     * 2. Sinon, syndicat de type DEPARTEMENT couvrant le département de la commune
     * 3. Sinon null (zone non couverte)
     */
    private Syndicat trouverSyndicatPourCommune(Commune commune) {
        try {
            List<Syndicat> tous = syndicatRepository.findAll();
            // 1. Syndicat COMMUNE exact
            for (Syndicat s : tous) {
                if (s.getCommune() != null && s.getCommune().getId().equals(commune.getId())) {
                    return s;
                }
            }
            // 2. Syndicat DEPARTEMENT
            if (commune.getDepartement() != null) {
                for (Syndicat s : tous) {
                    if (s.getDepartement() != null
                            && s.getDepartement().getId().equals(commune.getDepartement().getId())) {
                        return s;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("   ⚠️ Impossible de trouver le syndicat pour {}: {}", commune.getNom(), e.getMessage());
        }
        return null;
    }
}
