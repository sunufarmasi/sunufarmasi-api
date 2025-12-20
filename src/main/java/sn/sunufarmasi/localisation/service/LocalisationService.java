package sn.sunufarmasi.localisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.dto.request.*;
import sn.sunufarmasi.localisation.dto.response.*;
import sn.sunufarmasi.localisation.entity.*;
import sn.sunufarmasi.localisation.enums.TypeCommune;
import sn.sunufarmasi.localisation.mapper.LocalisationMapper;
import sn.sunufarmasi.localisation.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion de la localisation
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LocalisationService {

    private final PaysRepository paysRepository;
    private final RegionRepository regionRepository;
    private final DepartementRepository departementRepository;
    private final CommuneRepository communeRepository;
    private final LocalisationMapper mapper;

    // ═══════════════════════════════════════════════════════════
    // PAYS
    // ═══════════════════════════════════════════════════════════

    public PaysResponse creerPays(CreatePaysRequest req) {
        log.info("Création pays: {} - {}", req.code(), req.nom());

        if (paysRepository.existsByCode(req.code())) {
            throw new IllegalArgumentException("Un pays avec ce code existe déjà: " + req.code());
        }
        if (paysRepository.existsByCodeIso2(req.codeIso2())) {
            throw new IllegalArgumentException("Un pays avec ce code ISO2 existe déjà: " + req.codeIso2());
        }

        Pays pays = Pays.builder()
                .code(req.code().toUpperCase())
                .codeIso2(req.codeIso2().toUpperCase())
                .codeIso3(req.codeIso3() != null ? req.codeIso3().toUpperCase() : null)
                .nom(req.nom())
                .nomEn(req.nomEn())
                .capitale(req.capitale())
                .indicatifTelephonique(req.indicatifTelephonique())
                .devise(req.devise())
                .fuseauHoraire(req.fuseauHoraire())
                .drapeau(req.drapeau())
                .actif(true)
                .build();

        return mapper.toResponse(paysRepository.save(pays));
    }

    @Transactional(readOnly = true)
    public List<PaysResponse> getAllPays() {
        return paysRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaysResponse getPaysById(UUID id) {
        return paysRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Pays non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public PaysResponse getPaysByCode(String code) {
        return paysRepository.findByCode(code.toUpperCase())
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Pays non trouvé: " + code));
    }

    public void supprimerPays(UUID id) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pays non trouvé: " + id));
        pays.setActif(false);
        paysRepository.save(pays);
        log.info("Pays désactivé: {}", pays.getNom());
    }

    // ═══════════════════════════════════════════════════════════
    // RÉGIONS
    // ═══════════════════════════════════════════════════════════

    public RegionResponse creerRegion(CreateRegionRequest req) {
        log.info("Création région: {} - {}", req.code(), req.nom());

        Pays pays = paysRepository.findById(req.paysId())
                .orElseThrow(() -> new IllegalArgumentException("Pays non trouvé: " + req.paysId()));

        if (regionRepository.existsByCodeAndPaysId(req.code(), req.paysId())) {
            throw new IllegalArgumentException("Une région avec ce code existe déjà dans ce pays");
        }

        Region region = Region.builder()
                .pays(pays)
                .code(req.code().toUpperCase())
                .nom(req.nom())
                .chefLieu(req.chefLieu())
                .population(req.population())
                .superficie(req.superficie())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .ordre(req.ordre() != null ? req.ordre() : 0)
                .actif(true)
                .build();

        return mapper.toResponse(regionRepository.save(region));
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> getRegionsByPays(UUID paysId) {
        return regionRepository.findByPaysIdAndActifTrueOrderByOrdreAscNomAsc(paysId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> getRegionsByPaysCode(String paysCode) {
        return regionRepository.findByPaysCodeOrderByOrdreAscNomAsc(paysCode.toUpperCase())
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegionResponse getRegionById(UUID id) {
        return regionRepository.findByIdWithPays(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Région non trouvée: " + id));
    }

    @Transactional(readOnly = true)
    public List<RegionResponse> getAllRegions() {
        return regionRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public void supprimerRegion(UUID id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Région non trouvée: " + id));
        region.setActif(false);
        regionRepository.save(region);
        log.info("Région désactivée: {}", region.getNom());
    }

    // ═══════════════════════════════════════════════════════════
    // DÉPARTEMENTS
    // ═══════════════════════════════════════════════════════════

    public DepartementResponse creerDepartement(CreateDepartementRequest req) {
        log.info("Création département: {} - {}", req.code(), req.nom());

        Region region = regionRepository.findById(req.regionId())
                .orElseThrow(() -> new IllegalArgumentException("Région non trouvée: " + req.regionId()));

        if (departementRepository.existsByCodeAndRegionId(req.code(), req.regionId())) {
            throw new IllegalArgumentException("Un département avec ce code existe déjà dans cette région");
        }

        Departement departement = Departement.builder()
                .region(region)
                .code(req.code().toUpperCase())
                .nom(req.nom())
                .chefLieu(req.chefLieu())
                .population(req.population())
                .superficie(req.superficie())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .ordre(req.ordre() != null ? req.ordre() : 0)
                .actif(true)
                .build();

        return mapper.toResponse(departementRepository.save(departement));
    }

    @Transactional(readOnly = true)
    public List<DepartementResponse> getDepartementsByRegion(UUID regionId) {
        return departementRepository.findByRegionIdAndActifTrueOrderByOrdreAscNomAsc(regionId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartementResponse getDepartementById(UUID id) {
        return departementRepository.findByIdWithRegionAndPays(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Département non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<DepartementResponse> getAllDepartements() {
        return departementRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public void supprimerDepartement(UUID id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Département non trouvé: " + id));
        departement.setActif(false);
        departementRepository.save(departement);
        log.info("Département désactivé: {}", departement.getNom());
    }

    // ═══════════════════════════════════════════════════════════
    // COMMUNES
    // ═══════════════════════════════════════════════════════════

    public CommuneResponse creerCommune(CreateCommuneRequest req) {
        log.info("Création commune: {} - {}", req.code(), req.nom());

        Departement departement = departementRepository.findById(req.departementId())
                .orElseThrow(() -> new IllegalArgumentException("Département non trouvé: " + req.departementId()));

        if (communeRepository.existsByCodeAndDepartementId(req.code(), req.departementId())) {
            throw new IllegalArgumentException("Une commune avec ce code existe déjà dans ce département");
        }

        Commune commune = Commune.builder()
                .departement(departement)
                .code(req.code().toUpperCase())
                .nom(req.nom())
                .type(req.type() != null ? req.type() : TypeCommune.COMMUNE)
                .codePostal(req.codePostal())
                .population(req.population())
                .superficie(req.superficie())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .altitude(req.altitude())
                .ordre(req.ordre() != null ? req.ordre() : 0)
                .zoneUrbaine(req.zoneUrbaine() != null ? req.zoneUrbaine() : false)
                .arrondissement(req.arrondissement())
                .actif(true)
                .build();

        return mapper.toResponse(communeRepository.save(commune));
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> getCommunesByDepartement(UUID departementId) {
        return communeRepository.findByDepartementIdAndActifTrueOrderByOrdreAscNomAsc(departementId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> getCommunesByRegion(UUID regionId) {
        return communeRepository.findByRegionId(regionId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommuneResponse getCommuneById(UUID id) {
        return communeRepository.findByIdWithAll(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Commune non trouvée: " + id));
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> getAllCommunes() {
        return communeRepository.findByActifTrueOrderByNomAsc()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> searchCommunes(String search) {
        return communeRepository.searchActives(search)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommuneResponse> getCommunesProches(Double latitude, Double longitude, Double rayonKm) {
        log.debug("Recherche communes proches: lat={}, lng={}, rayon={}km", latitude, longitude, rayonKm);
        
        // Calcul du bounding box approximatif
        double latDelta = rayonKm / 111.0; // 1 degré ≈ 111 km
        double lngDelta = rayonKm / (111.0 * Math.cos(Math.toRadians(latitude)));
        
        return communeRepository.findInBoundingBox(
                latitude - latDelta,
                latitude + latDelta,
                longitude - lngDelta,
                longitude + lngDelta
        ).stream()
         .map(c -> mapper.toResponse(c, latitude, longitude))
         .filter(c -> c.distanceKm() != null && c.distanceKm() <= rayonKm)
         .sorted((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()))
         .collect(Collectors.toList());
    }

    public void supprimerCommune(UUID id) {
        Commune commune = communeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commune non trouvée: " + id));
        commune.setActif(false);
        communeRepository.save(commune);
        log.info("Commune désactivée: {}", commune.getNom());
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Initialiser les données du Sénégal
     */
    public void initialiserSenegal() {
        if (paysRepository.existsByCode("SN")) {
            log.info("Le Sénégal est déjà initialisé");
            return;
        }

        log.info("Initialisation des données du Sénégal...");

        // Créer le Sénégal
        Pays senegal = Pays.builder()
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
        senegal = paysRepository.save(senegal);

        // Créer les 14 régions
        creerRegionsSenegal(senegal);

        log.info("Données du Sénégal initialisées avec succès");
    }

    private void creerRegionsSenegal(Pays senegal) {
        // Les 14 régions du Sénégal avec leurs départements principaux
        String[][] regionsData = {
            {"DK", "Dakar", "Dakar", "14.6937", "-17.4441"},
            {"TH", "Thiès", "Thiès", "14.7886", "-16.9260"},
            {"DL", "Diourbel", "Diourbel", "14.6546", "-16.2321"},
            {"SL", "Saint-Louis", "Saint-Louis", "16.0200", "-16.5000"},
            {"KL", "Kaolack", "Kaolack", "14.1652", "-16.0726"},
            {"LG", "Louga", "Louga", "15.6144", "-16.2281"},
            {"FK", "Fatick", "Fatick", "14.3390", "-16.4041"},
            {"ZG", "Ziguinchor", "Ziguinchor", "12.5833", "-16.2719"},
            {"KD", "Kolda", "Kolda", "12.8983", "-14.9408"},
            {"TB", "Tambacounda", "Tambacounda", "13.7707", "-13.6673"},
            {"MT", "Matam", "Matam", "15.6559", "-13.2555"},
            {"KF", "Kaffrine", "Kaffrine", "14.1059", "-15.5508"},
            {"KG", "Kédougou", "Kédougou", "12.5605", "-12.1747"},
            {"SD", "Sédhiou", "Sédhiou", "12.7081", "-15.5569"}
        };

        int ordre = 1;
        for (String[] data : regionsData) {
            Region region = Region.builder()
                    .pays(senegal)
                    .code(data[0])
                    .nom(data[1])
                    .chefLieu(data[2])
                    .latitude(Double.parseDouble(data[3]))
                    .longitude(Double.parseDouble(data[4]))
                    .ordre(ordre++)
                    .actif(true)
                    .build();
            regionRepository.save(region);

            // Créer un département par défaut (même nom que la région)
            Departement dept = Departement.builder()
                    .region(region)
                    .code(data[0] + "-" + data[0])
                    .nom(data[1])
                    .chefLieu(data[2])
                    .latitude(Double.parseDouble(data[3]))
                    .longitude(Double.parseDouble(data[4]))
                    .ordre(1)
                    .actif(true)
                    .build();
            departementRepository.save(dept);

            // Créer une commune chef-lieu
            Commune commune = Commune.builder()
                    .departement(dept)
                    .code(data[0] + "-VILLE")
                    .nom(data[1])
                    .type(data[0].equals("DK") ? TypeCommune.CAPITALE : TypeCommune.CHEF_LIEU_REGION)
                    .latitude(Double.parseDouble(data[3]))
                    .longitude(Double.parseDouble(data[4]))
                    .zoneUrbaine(true)
                    .ordre(1)
                    .actif(true)
                    .build();
            communeRepository.save(commune);
        }
    }
}
