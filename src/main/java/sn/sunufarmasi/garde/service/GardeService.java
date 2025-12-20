package sn.sunufarmasi.garde.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.garde.dto.GardeDTO.*;
import sn.sunufarmasi.garde.entity.*;
import sn.sunufarmasi.garde.enums.StatutGarde;
import sn.sunufarmasi.garde.enums.TypeGarde;
import sn.sunufarmasi.garde.repository.GardeRepository;
import sn.sunufarmasi.garde.repository.PlanningGardeRepository;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.localisation.repository.DepartementRepository;
import sn.sunufarmasi.notification.service.NotificationService;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepository;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;

import jakarta.persistence.EntityNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion des gardes
 * Géré par le syndicat - basé sur les pharmacies
 *
 * NOUVEAU MODÈLE : Gardes par SEMAINE (samedi à vendredi) et par ZONE (commune/département)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GardeService {

    private final PlanningGardeRepository planningRepository;
    private final GardeRepository gardeRepository;
    private final SyndicatRepository syndicatRepository;
    private final PharmacieRepository pharmacieRepository;
    private final CommuneRepository communeRepository;
    private final DepartementRepository departementRepository;
    private final NotificationService notificationService;

    // ═══════════════════════════════════════════════════════════
    // GESTION DES PLANNINGS
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau planning de garde
     */
    public PlanningResponse createPlanning(UUID syndicatId, CreatePlanningRequest request, UUID userId, String userName) {
        Syndicat syndicat = syndicatRepository.findById(syndicatId)
                .orElseThrow(() -> new EntityNotFoundException("Syndicat non trouvé"));

        // Vérifier chevauchement de dates
        if (planningRepository.existsChevauchement(syndicatId, request.dateDebut(), request.dateFin(), UUID.randomUUID())) {
            throw new IllegalArgumentException("Un planning existe déjà pour cette période");
        }

        // Validation des dates
        if (request.dateFin().isBefore(request.dateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }

        PlanningGarde planning = PlanningGarde.builder()
                .syndicat(syndicat)
                .titre(request.titre())
                .description(request.description())
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .statut(StatutPlanning.BROUILLON)
                .publicationAuto(request.publicationAuto() != null ? request.publicationAuto() : false)
                .datePublicationPrevue(request.datePublicationPrevue())
                .notifierPharmacies(request.notifierPharmacies() != null ? request.notifierPharmacies() : true)
                .notifierSms(request.notifierSms() != null ? request.notifierSms() : false)
                .notifierEmail(request.notifierEmail() != null ? request.notifierEmail() : true)
                .creeParId(userId)
                .creeParNom(userName)
                .build();

        planning = planningRepository.save(planning);
        log.info("Planning créé: {} par {}", planning.getId(), userName);

        return toPlanningResponse(planning);
    }

    /**
     * Mettre à jour un planning
     */
    public PlanningResponse updatePlanning(UUID planningId, UpdatePlanningRequest request) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));

        if (!planning.isModifiable()) {
            throw new IllegalStateException("Ce planning ne peut plus être modifié");
        }

        if (request.titre() != null) planning.setTitre(request.titre());
        if (request.description() != null) planning.setDescription(request.description());
        if (request.dateDebut() != null) planning.setDateDebut(request.dateDebut());
        if (request.dateFin() != null) planning.setDateFin(request.dateFin());
        if (request.publicationAuto() != null) planning.setPublicationAuto(request.publicationAuto());
        if (request.datePublicationPrevue() != null) planning.setDatePublicationPrevue(request.datePublicationPrevue());
        if (request.notifierPharmacies() != null) planning.setNotifierPharmacies(request.notifierPharmacies());
        if (request.notifierSms() != null) planning.setNotifierSms(request.notifierSms());
        if (request.notifierEmail() != null) planning.setNotifierEmail(request.notifierEmail());

        planning = planningRepository.save(planning);
        return toPlanningResponse(planning);
    }

    /**
     * Soumettre un planning pour validation
     */
    public PlanningResponse soumettrePourValidation(UUID planningId) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));

        if (planning.getStatut() != StatutPlanning.BROUILLON) {
            throw new IllegalStateException("Seul un brouillon peut être soumis");
        }

        if (planning.getGardes().isEmpty()) {
            throw new IllegalStateException("Le planning doit contenir au moins une garde");
        }

        planning.soumettrePourValidation();
        planning = planningRepository.save(planning);
        log.info("Planning {} soumis pour validation", planningId);

        return toPlanningResponse(planning);
    }

    /**
     * Valider un planning
     */
    public PlanningResponse validerPlanning(UUID planningId) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));

        if (planning.getStatut() != StatutPlanning.EN_VALIDATION) {
            throw new IllegalStateException("Le planning doit être en validation");
        }

        planning.valider();
        planning = planningRepository.save(planning);
        log.info("Planning {} validé", planningId);

        return toPlanningResponse(planning);
    }

    /**
     * Publier un planning
     */
    public PlanningResponse publierPlanning(UUID planningId, UUID publieParId) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));

        if (planning.getStatut() != StatutPlanning.VALIDE) {
            throw new IllegalStateException("Le planning doit être validé avant publication");
        }

        planning.publier(publieParId);
        planning = planningRepository.save(planning);
        log.info("Planning {} publié", planningId);

        // Notifier les pharmacies
        if (planning.getNotifierPharmacies()) {
            notifierPharmaciesPlanning(planning);
        }

        return toPlanningResponse(planning);
    }

    /**
     * Obtenir un planning par ID
     */
    @Transactional(readOnly = true)
    public PlanningResponse getPlanning(UUID planningId) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));
        return toPlanningResponse(planning);
    }

    /**
     * Liste des plannings d'un syndicat
     */
    @Transactional(readOnly = true)
    public List<PlanningResumeResponse> getPlanningsBySyndicat(UUID syndicatId) {
        return planningRepository.findBySyndicatIdOrderByDateDebutDesc(syndicatId)
                .stream()
                .map(this::toPlanningResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Planning actuel (publié et en cours)
     */
    @Transactional(readOnly = true)
    public PlanningResponse getPlanningActuel(UUID syndicatId) {
        return planningRepository.findPlanningActuel(syndicatId, LocalDate.now())
                .map(this::toPlanningResponse)
                .orElse(null);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DES GARDES (NOUVEAU MODÈLE PAR SEMAINE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Ajouter une garde au planning (par semaine)
     */
    public GardeResponse addGarde(UUID planningId, CreateGardeRequest request) {
        PlanningGarde planning = planningRepository.findById(planningId)
                .orElseThrow(() -> new EntityNotFoundException("Planning non trouvé"));

        if (!planning.isModifiable()) {
            throw new IllegalStateException("Ce planning ne peut plus être modifié");
        }

        Pharmacie pharmacie = pharmacieRepository.findById(request.pharmacieId())
                .orElseThrow(() -> new EntityNotFoundException("Pharmacie non trouvée"));

        // Calculer les dates de la semaine (samedi à vendredi)
        LocalDate dateDebut = getDebutSemaine(request.dateGarde());
        LocalDate dateFin = dateDebut.plusDays(6);

        // Vérifier que la semaine est dans la période du planning
        if (dateDebut.isBefore(planning.getDateDebut()) || dateFin.isAfter(planning.getDateFin())) {
            throw new IllegalArgumentException("La semaine de garde doit être dans la période du planning");
        }

        // Récupérer la zone (commune ou département)
        Commune commune = null;
        Departement departement = null;
        String zoneNom = null;

        if (request.communeId() != null) {
            commune = communeRepository.findById(request.communeId())
                    .orElseThrow(() -> new EntityNotFoundException("Commune non trouvée"));
            zoneNom = commune.getNom();

            // Vérifier si une garde existe déjà pour cette semaine/commune/type
            if (gardeRepository.existsBySemaineAndCommuneAndType(dateDebut, dateFin, commune.getId(), request.typeGarde())) {
                throw new IllegalArgumentException("Une garde existe déjà pour cette semaine et cette commune");
            }
        } else if (request.departementId() != null) {
            departement = departementRepository.findById(request.departementId())
                    .orElseThrow(() -> new EntityNotFoundException("Département non trouvé"));
            zoneNom = departement.getNom();

            // Vérifier si une garde existe déjà pour cette semaine/département/type
            if (gardeRepository.existsBySemaineAndDepartementAndType(dateDebut, dateFin, departement.getId(), request.typeGarde())) {
                throw new IllegalArgumentException("Une garde existe déjà pour cette semaine et ce département");
            }
        } else {
            throw new IllegalArgumentException("Une commune ou un département doit être spécifié");
        }

        Garde garde = Garde.builder()
                .planning(planning)
                .pharmacie(pharmacie)
                .dateDebut(dateDebut)
                .dateFin(dateFin)
                .numeroSemaine(dateDebut.get(WeekFields.ISO.weekOfWeekBasedYear()))
                .commune(commune)
                .departement(departement)
                .zoneNom(zoneNom)
                .typeGarde(request.typeGarde() != null ? request.typeGarde() : TypeGarde.JOUR_ET_NUIT)
                .heureDebut(request.heureDebut())
                .heureFin(request.heureFin())
                .notes(request.notes())
                .latitude(pharmacie.getLatitude())
                .longitude(pharmacie.getLongitude())
                .build();

        garde = gardeRepository.save(garde);
        log.info("Garde ajoutée: {} - Semaine {} ({} au {}) - Zone: {} - Pharmacie: {}",
                garde.getId(), garde.getNumeroSemaine(), dateDebut, dateFin, zoneNom, pharmacie.getNom());

        return toGardeResponse(garde);
    }

    /**
     * Ajouter plusieurs gardes en lot
     */
    public List<GardeResponse> addGardesBatch(UUID planningId, CreateGardesBatchRequest request) {
        return request.gardes().stream()
                .map(req -> addGarde(planningId, req))
                .collect(Collectors.toList());
    }

    /**
     * Confirmer une garde (par la pharmacie)
     */
    public GardeResponse confirmerGarde(UUID gardeId) {
        Garde garde = gardeRepository.findById(gardeId)
                .orElseThrow(() -> new EntityNotFoundException("Garde non trouvée"));

        garde.confirmer();
        garde = gardeRepository.save(garde);
        log.info("Garde {} confirmée par la pharmacie", gardeId);

        return toGardeResponse(garde);
    }

    /**
     * Annuler une garde
     */
    public GardeResponse annulerGarde(UUID gardeId, String motif) {
        Garde garde = gardeRepository.findById(gardeId)
                .orElseThrow(() -> new EntityNotFoundException("Garde non trouvée"));

        garde.annuler(motif);
        garde = gardeRepository.save(garde);
        log.info("Garde {} annulée: {}", gardeId, motif);

        return toGardeResponse(garde);
    }

    /**
     * Supprimer une garde
     */
    public void deleteGarde(UUID gardeId) {
        Garde garde = gardeRepository.findById(gardeId)
                .orElseThrow(() -> new EntityNotFoundException("Garde non trouvée"));

        if (!garde.getPlanning().isModifiable()) {
            throw new IllegalStateException("Le planning ne peut plus être modifié");
        }

        gardeRepository.delete(garde);
        log.info("Garde {} supprimée", gardeId);
    }

    // ═══════════════════════════════════════════════════════════
    // RECHERCHE DE PHARMACIES DE GARDE (NOUVEAU MODÈLE)
    // ═══════════════════════════════════════════════════════════

    /**
     * Pharmacies de garde pour la semaine en cours
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeSemaineCourante() {
        LocalDate today = LocalDate.now();
        return gardeRepository.findGardesSemaineEnCours(today)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde pour une semaine donnée dans une commune
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeParSemaineEtCommune(
            LocalDate dateDebut, LocalDate dateFin, UUID communeId) {
        return gardeRepository.findBySemaineAndCommune(dateDebut, dateFin, communeId)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde pour une semaine donnée dans un département
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeParSemaineEtDepartement(
            LocalDate dateDebut, LocalDate dateFin, UUID departementId) {
        return gardeRepository.findBySemaineAndDepartement(dateDebut, dateFin, departementId)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde pour une date spécifique dans une commune
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeParDateEtCommune(LocalDate date, UUID communeId) {
        return gardeRepository.findByDateAndCommune(date, communeId)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde pour une date spécifique dans un département
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeParDateEtDepartement(LocalDate date, UUID departementId) {
        return gardeRepository.findByDateAndDepartement(date, departementId)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde aujourd'hui (toutes zones)
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGardeAujourdhui() {
        LocalDate today = LocalDate.now();
        return gardeRepository.findGardesSemaineEnCours(today)
                .stream()
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde pour une date (compatibilité ancienne API)
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGarde(LocalDate date) {
        return gardeRepository.findAll().stream()
                .filter(g -> g.contientDate(date))
                .filter(g -> g.getStatut() != StatutGarde.ANNULEE)
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde par type (compatibilité ancienne API)
     */
    @Transactional(readOnly = true)
    public List<GardeResumeResponse> getPharmaciesDeGarde(LocalDate date, TypeGarde type) {
        return gardeRepository.findAll().stream()
                .filter(g -> g.contientDate(date))
                .filter(g -> g.getTypeGarde() == type)
                .filter(g -> g.getStatut() != StatutGarde.ANNULEE)
                .map(this::toGardeResumeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Pharmacies de garde à proximité
     */
    @Transactional(readOnly = true)
    public List<GardeProximiteResponse> getPharmaciesDeGardeProximite(
            LocalDate date, Double latitude, Double longitude, Double rayonKm) {
        return gardeRepository.findAll().stream()
                .filter(g -> g.contientDate(date))
                .filter(g -> g.getStatut() != StatutGarde.ANNULEE)
                .filter(g -> {
                    if (g.getLatitude() == null || g.getLongitude() == null) return false;
                    double distance = calculerDistance(latitude, longitude, g.getLatitude(), g.getLongitude());
                    return distance <= rayonKm;
                })
                .map(g -> {
                    GardeResumeResponse resume = toGardeResumeResponse(g);
                    double distance = calculerDistance(latitude, longitude, g.getLatitude(), g.getLongitude());
                    return new GardeProximiteResponse(resume, Math.round(distance * 100.0) / 100.0);
                })
                .sorted((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()))
                .collect(Collectors.toList());
    }

    /**
     * Gardes à venir d'une pharmacie
     */
    @Transactional(readOnly = true)
    public List<GardeResponse> getGardesAVenirPharmacie(UUID pharmacieId) {
        return gardeRepository.findGardesAVenirByPharmacie(pharmacieId, LocalDate.now())
                .stream()
                .map(this::toGardeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Historique des gardes d'une pharmacie
     */
    @Transactional(readOnly = true)
    public Page<GardeResponse> getHistoriqueGardesPharmacie(UUID pharmacieId, Pageable pageable) {
        return gardeRepository.findHistoriqueByPharmacie(pharmacieId, pageable)
                .map(this::toGardeResponse);
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES DATES (SEMAINE SÉNÉGALAISE : SAMEDI À VENDREDI)
    // ═══════════════════════════════════════════════════════════

    /**
     * Calculer le début de semaine (samedi) pour une date donnée
     */
    public static LocalDate getDebutSemaine(LocalDate date) {
        DayOfWeek jour = date.getDayOfWeek();
        if (jour == DayOfWeek.SATURDAY) {
            return date;
        } else if (jour == DayOfWeek.SUNDAY) {
            return date.minusDays(1);
        } else {
            // Lundi à Vendredi : retourner le samedi précédent
            return date.with(TemporalAdjusters.previous(DayOfWeek.SATURDAY));
        }
    }

    /**
     * Calculer la fin de semaine (vendredi) pour une date donnée
     */
    public static LocalDate getFinSemaine(LocalDate date) {
        LocalDate debutSemaine = getDebutSemaine(date);
        return debutSemaine.plusDays(6);
    }

    // ═══════════════════════════════════════════════════════════
    // TÂCHES PLANIFIÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Publication automatique des plannings (toutes les heures)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void publicationAutomatique() {
        List<PlanningGarde> plannings = planningRepository.findPlanningsAPublierAuto(LocalDateTime.now());
        for (PlanningGarde planning : plannings) {
            try {
                planning.publier(null);
                planningRepository.save(planning);
                if (planning.getNotifierPharmacies()) {
                    notifierPharmaciesPlanning(planning);
                }
                log.info("Planning {} publié automatiquement", planning.getId());
            } catch (Exception e) {
                log.error("Erreur publication automatique planning {}: {}", planning.getId(), e.getMessage());
            }
        }
    }

    /**
     * Envoi des rappels avant début de semaine (vendredi à 18h)
     */
    @Scheduled(cron = "0 0 18 * * FRI")
    public void envoyerRappelsGardes() {
        // Rappeler les pharmacies de garde la semaine prochaine
        LocalDate samediProchain = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        LocalDate vendrediProchain = samediProchain.plusDays(6);

        List<Garde> gardes = gardeRepository.findAll().stream()
                .filter(g -> g.getDateDebut().equals(samediProchain))
                .filter(g -> !g.getRappelEnvoye())
                .toList();

        for (Garde garde : gardes) {
            try {
                notificationService.rappelerGarde(
                        garde.getPharmacie().getId(),
                        null,
                        garde.getDateDebut()
                );
                garde.marquerRappelEnvoye();
                gardeRepository.save(garde);
                log.info("Rappel envoyé pour garde {} - Pharmacie {} - Semaine du {}",
                        garde.getId(), garde.getPharmacie().getNom(), garde.getDateDebut());
            } catch (Exception e) {
                log.error("Erreur envoi rappel garde {}: {}", garde.getId(), e.getMessage());
            }
        }
    }

    /**
     * Mise à jour des statuts (tous les jours à minuit)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void mettreAJourStatutsGardes() {
        LocalDate today = LocalDate.now();

        // Mettre à jour les statuts des gardes
        List<Garde> gardes = gardeRepository.findAll();
        int terminees = 0;
        int enCours = 0;

        for (Garde garde : gardes) {
            if (garde.getStatut() == StatutGarde.ANNULEE) continue;

            if (garde.getDateFin().isBefore(today) && garde.getStatut() != StatutGarde.TERMINEE) {
                garde.setStatut(StatutGarde.TERMINEE);
                gardeRepository.save(garde);
                terminees++;
            } else if (garde.contientDate(today) && garde.getStatut() == StatutGarde.PLANIFIEE) {
                garde.setStatut(StatutGarde.EN_COURS);
                gardeRepository.save(garde);
                enCours++;
            }
        }

        if (terminees > 0) log.info("{} gardes terminées automatiquement", terminees);
        if (enCours > 0) log.info("{} gardes démarrées automatiquement", enCours);
    }

    /**
     * Archivage des plannings terminés (tous les lundis à minuit)
     */
    @Scheduled(cron = "0 0 0 * * MON")
    public void archiverPlanningsTermines() {
        LocalDate dateArchivage = LocalDate.now().minusDays(7);
        List<PlanningGarde> plannings = planningRepository.findPlanningsAArchiver(dateArchivage);

        for (PlanningGarde planning : plannings) {
            planning.archiver();
            planningRepository.save(planning);
            log.info("Planning {} archivé", planning.getId());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private void notifierPharmaciesPlanning(PlanningGarde planning) {
        for (Garde garde : planning.getGardes()) {
            try {
                notificationService.notifierPharmacie(
                        garde.getPharmacie().getId(),
                        null,
                        "Nouvelle garde planifiée",
                        String.format("Vous êtes de garde du %s au %s (%s) - Zone: %s",
                                garde.getDateDebut(), garde.getDateFin(),
                                garde.getTypeGarde().getLibelle(), garde.getZoneNom()),
                        "/gardes/" + garde.getId()
                );
                garde.marquerNotifiee();
                gardeRepository.save(garde);
            } catch (Exception e) {
                log.error("Erreur notification pharmacie {}: {}", garde.getPharmacie().getId(), e.getMessage());
            }
        }
    }

    private double calculerDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return Double.MAX_VALUE;
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // ═══════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════

    private PlanningResponse toPlanningResponse(PlanningGarde p) {
        long confirmees = p.getGardes().stream().filter(Garde::getConfirmeParPharmacie).count();
        return new PlanningResponse(
                p.getId(), p.getTitre(), p.getDescription(),
                p.getDateDebut(), p.getDateFin(),
                p.getStatut(), p.getStatut().getLibelle(),
                p.getPublicationAuto(), p.getDatePublicationPrevue(), p.getDatePublication(),
                p.getNotifierPharmacies(), p.getNotifierSms(), p.getNotifierEmail(),
                p.getCreeParId(), p.getCreeParNom(), p.getPublieParId(),
                p.getNombreGardes(), (int) confirmees, p.getNombreJours(),
                p.getCreatedAt(), p.getUpdatedAt(),
                p.getGardes().stream().map(this::toGardeResponse).collect(Collectors.toList())
        );
    }

    private PlanningResumeResponse toPlanningResumeResponse(PlanningGarde p) {
        return new PlanningResumeResponse(
                p.getId(), p.getTitre(), p.getDateDebut(), p.getDateFin(),
                p.getStatut(), p.getStatut().getLibelle(),
                p.getNombreGardes(), p.getPublicationAuto(), p.getDatePublication()
        );
    }

    private GardeResponse toGardeResponse(Garde g) {
        Pharmacie ph = g.getPharmacie();
        return new GardeResponse(
                g.getId(),
                g.getPlanning().getId(),
                new PharmacieGardeInfo(
                        ph.getId(), ph.getCode(), ph.getNom(), ph.getAdresseComplete(),
                        ph.getTelephone(), ph.getEmail(),
                        ph.getLatitude(), ph.getLongitude(),
                        ph.getCommune() != null ? ph.getCommune().getNom() : null,
                        ph.getCommune() != null && ph.getCommune().getDepartement() != null
                                ? ph.getCommune().getDepartement().getNom() : null
                ),
                g.getDateDebut(),
                g.getDateFin(),
                g.getNumeroSemaine(),
                g.getZoneNom(),
                g.getTypeGarde(),
                g.getTypeGarde().getLibelle(),
                g.getStatut(),
                g.getStatut().getLibelle(),
                g.getHeureDebut(),
                g.getHeureFin(),
                g.getNotes(),
                g.getConfirmeParPharmacie(),
                g.getDateConfirmation(),
                g.getNotificationEnvoyee(),
                g.getRappelEnvoye(),
                g.getLatitude(),
                g.getLongitude(),
                g.estSemaineEnCours(),
                g.estSemaineProchaine(),
                g.estPassee(),
                g.getNombreJours()
        );
    }

    private GardeResumeResponse toGardeResumeResponse(Garde g) {
        Pharmacie ph = g.getPharmacie();
        return new GardeResumeResponse(
                g.getId(),
                ph.getNom(),
                ph.getAdresseComplete(),
                ph.getTelephone(),
                g.getDateDebut(),
                g.getDateFin(),
                g.getNumeroSemaine(),
                g.getZoneNom(),
                g.getTypeGarde(),
                g.getTypeGarde().getHorairesDefaut(),
                g.getStatut(),
                g.getLatitude(),
                g.getLongitude(),
                g.estSemaineEnCours()
        );
    }
}