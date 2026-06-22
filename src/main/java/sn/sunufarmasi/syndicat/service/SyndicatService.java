package sn.sunufarmasi.syndicat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.localisation.entity.Region;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.localisation.repository.DepartementRepository;
import sn.sunufarmasi.localisation.repository.RegionRepository;
import sn.sunufarmasi.notification.service.EmailService;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.pharmacie.mapper.PharmacieMapper;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.security.JwtTokenProvider;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ConflictException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.shared.exception.UnauthorizedException;
import sn.sunufarmasi.syndicat.dto.request.ChangePasswordSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.CreateSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.LoginSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.request.UpdateSyndicatRequest;
import sn.sunufarmasi.syndicat.dto.response.SyndicatAuthResponse;
import sn.sunufarmasi.syndicat.dto.response.SyndicatDetailResponse;
import sn.sunufarmasi.syndicat.dto.response.SyndicatResponse;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.enums.PlanAbonnementSyndicat;
import sn.sunufarmasi.syndicat.enums.StatutSyndicat;
import sn.sunufarmasi.syndicat.enums.TypeSyndicat;
import sn.sunufarmasi.syndicat.mapper.SyndicatMapper;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;

import sn.sunufarmasi.syndicat.dto.request.EnregistrerPaiementRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des syndicats
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SyndicatService {

    private final SyndicatRepository syndicatRepository;
    private final CommuneRepository communeRepository;
    private final DepartementRepository departementRepository;
    private final RegionRepository regionRepository;
    private final PharmacienRepository pharmacienRepository;
    private final PharmacieRepositorye pharmacieRepository;
    private final PharmacieMapper pharmacieMapper;
    private final SyndicatMapper syndicatMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    public SyndicatResponse create(CreateSyndicatRequest request, UUID adminId, String adminNom) {
        log.info("Création syndicat: {} par admin: {}", request.nom(), adminId);
        validateCreateRequest(request);
        Syndicat syndicat = buildSyndicatFromRequest(request);
        syndicat.setCreeParId(adminId);
        syndicat.setCreeParNom(adminNom);
        syndicat.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));
        syndicat.setCode(generateCode(syndicat));
        Syndicat saved = syndicatRepository.save(syndicat);
        log.info("Syndicat créé avec succès: {} ({})", saved.getNom(), saved.getCode());
        // Notifier le syndicat par email avec ses identifiants
        emailService.notifierNouveauSyndicat(saved.getEmail(), saved.getNom(),
                saved.getUsername(), request.motDePasse());
        return syndicatMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    public SyndicatAuthResponse login(LoginSyndicatRequest request) {
        log.info("Tentative de connexion syndicat: {}", request.username());

        Syndicat syndicat = syndicatRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Username ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.motDePasse(), syndicat.getMotDePasseHash())) {
            log.warn("Échec connexion syndicat - mot de passe incorrect: {}", request.username());
            throw new UnauthorizedException("Username ou mot de passe incorrect");
        }

        if (syndicat.getStatut() != StatutSyndicat.ACTIF) {
            log.warn("Échec connexion syndicat - compte non actif: {}", request.username());
            throw new UnauthorizedException("Ce compte syndicat est " + syndicat.getStatut().getLibelle().toLowerCase());
        }

        syndicat.enregistrerConnexion();
        syndicatRepository.save(syndicat);

        String accessToken = jwtTokenProvider.generateToken(syndicat.getId().toString(), "SYNDICAT");
        String refreshToken = jwtTokenProvider.generateRefreshToken(syndicat.getId().toString(), "SYNDICAT");

        log.info("Connexion syndicat réussie: {}", syndicat.getCode());

        return SyndicatAuthResponse.success(
                syndicat.getId(),
                syndicat.getCode(),
                syndicat.getNom(),
                syndicat.getUsername(),
                syndicat.getType(),
                syndicat.getNomZone(),
                syndicat.getRegion() != null ? syndicat.getRegion().getNom() : null,
                syndicat.getStatut(),
                syndicat.abonnementActif(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationTime()
        );
    }

    public void changePassword(UUID syndicatId, ChangePasswordSyndicatRequest request) {
        log.info("Changement mot de passe syndicat: {}", syndicatId);
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        if (!passwordEncoder.matches(request.ancienMotDePasse(), syndicat.getMotDePasseHash())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }
        if (!request.motDePasseCorrespond()) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }
        syndicat.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        syndicatRepository.save(syndicat);
        log.info("Mot de passe syndicat changé avec succès: {}", syndicat.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public SyndicatDetailResponse getById(UUID id) {
        Syndicat syndicat = findByIdOrThrow(id);
        List<sn.sunufarmasi.pharmacie.entity.Pharmacie> pharmacies = pharmacieRepository.findBySyndicatId(id);
        Integer validees = (int) pharmacies.stream().filter(p -> p.getStatut() == StatutPharmacie.VALIDEE).count();
        Integer enAttente = (int) pharmacies.stream().filter(p -> p.getStatut() == StatutPharmacie.EN_ATTENTE).count();
        return syndicatMapper.toDetailResponse(syndicat, validees, enAttente);
    }

    @Transactional(readOnly = true)
    public SyndicatDetailResponse getByCode(String code) {
        Syndicat syndicat = syndicatRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicat non trouvé avec le code: " + code));
        List<sn.sunufarmasi.pharmacie.entity.Pharmacie> pharmacies = pharmacieRepository.findBySyndicatId(syndicat.getId());
        Integer validees = (int) pharmacies.stream().filter(p -> p.getStatut() == StatutPharmacie.VALIDEE).count();
        Integer enAttente = (int) pharmacies.stream().filter(p -> p.getStatut() == StatutPharmacie.EN_ATTENTE).count();
        return syndicatMapper.toDetailResponse(syndicat, validees, enAttente);
    }

    @Transactional(readOnly = true)
    public List<SyndicatResponse> getAll() {
        return syndicatRepository.findAll().stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByType(TypeSyndicat type) {
        return syndicatRepository.findByType(type).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByStatut(StatutSyndicat statut) {
        return syndicatRepository.findByStatut(statut).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Comptes associés au même syndicat (même préfixe de code)
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> getComptesAssocies(UUID syndicatId) {
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        // Stratégie 1 : préfixe de code   ex: "SYN-DK-001" → "SYN-DK-001-S1"
        String codePrefix     = syndicat.getCode() + "-";
        // Stratégie 2 : préfixe d'username ex: "syndicat.dakar" → "syndicat.dakar.sec1"
        String usernamePrefix = syndicat.getUsername() + ".";
        return syndicatRepository.findAll().stream()
                .filter(s -> !s.getId().equals(syndicatId)) // exclure soi-même
                .filter(s -> s.getCode().startsWith(codePrefix)
                          || s.getUsername().startsWith(usernamePrefix))
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Réinitialiser le mot de passe d'un syndicat (Admin uniquement)
     */
    public void resetPassword(UUID syndicatId, String nouveauMotDePasse) {
        if (nouveauMotDePasse == null || nouveauMotDePasse.length() < 8) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 8 caractères");
        }
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        syndicat.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        syndicatRepository.save(syndicat);
        log.info("Mot de passe réinitialisé pour syndicat: {}", syndicat.getCode());
    }

    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByRegion(UUID regionId) {
        return syndicatRepository.findByRegionId(regionId).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SyndicatResponse> searchByNom(String nom) {
        return syndicatRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SyndicatResponse findGestionnaireByCommune(UUID communeId) {
        List<Syndicat> syndicats = syndicatRepository.findSyndicatsGestionnairesByCommune(communeId);
        if (syndicats.isEmpty()) return null;
        return syndicatMapper.toResponse(syndicats.get(0));
    }

    /**
     * Liste des pharmacies du syndicat
     */
    @Transactional(readOnly = true)
    public List<PharmacieResponse> getPharmaciesBySyndicat(UUID syndicatId) {
        findByIdOrThrow(syndicatId);
        return pharmacieRepository.findBySyndicatId(syndicatId).stream()
                .filter(p -> p.getStatut() != sn.sunufarmasi.pharmacie.enums.StatutPharmacie.SUSPENDUE)
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    public SyndicatResponse update(UUID id, UpdateSyndicatRequest request) {
        log.info("Modification syndicat: {}", id);
        Syndicat syndicat = findByIdOrThrow(id);

        if (request.nom() != null) syndicat.setNom(request.nom());
        if (request.description() != null) syndicat.setDescription(request.description());
        if (request.telephone() != null) syndicat.setTelephone(request.telephone());
        if (request.telephoneSecondaire() != null) syndicat.setTelephoneSecondaire(request.telephoneSecondaire());
        if (request.email() != null) {
            if (!syndicat.getEmail().equals(request.email())
                    && syndicatRepository.existsByEmail(request.email())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            syndicat.setEmail(request.email());
        }
        if (request.adresse() != null) syndicat.setAdresse(request.adresse());
        if (request.responsableId() != null) {
            Pharmacien responsable = pharmacienRepository.findById(request.responsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacien non trouvé: " + request.responsableId()));
            syndicat.setResponsable(responsable);
            syndicat.setNomResponsable(responsable.getNomComplet());
        } else if (request.nomResponsable() != null) {
            syndicat.setNomResponsable(request.nomResponsable());
        }
        if (request.telephoneResponsable() != null) syndicat.setTelephoneResponsable(request.telephoneResponsable());

        // ── Changement de zone géographique ──────────────────────
        if (request.type() != null) {
            syndicat.setType(request.type());
            // Réinitialiser les anciennes zones
            syndicat.setCommune(null);
            syndicat.setDepartement(null);
            syndicat.getCommunesZone().clear();

            switch (request.type()) {
                case COMMUNE -> {
                    if (request.communeId() == null) throw new BadRequestException("communeId requis pour type COMMUNE");
                    Commune commune = communeRepository.findById(request.communeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Commune non trouvée"));
                    syndicat.setCommune(commune);
                    syndicat.setRegion(commune.getDepartement() != null ? commune.getDepartement().getRegion() : null);
                }
                case ZONE -> {
                    if (request.communeIds() == null || request.communeIds().isEmpty())
                        throw new BadRequestException("communeIds requis pour type ZONE");
                    for (UUID cId : request.communeIds()) {
                        Commune c = communeRepository.findById(cId)
                                .orElseThrow(() -> new ResourceNotFoundException("Commune non trouvée: " + cId));
                        syndicat.getCommunesZone().add(c);
                    }
                    // Région = région des communes (supposées dans le même département)
                    syndicat.getCommunesZone().stream().findFirst().ifPresent(c ->
                        syndicat.setRegion(c.getDepartement() != null ? c.getDepartement().getRegion() : null));
                }
                case DEPARTEMENT -> {
                    if (request.departementId() == null) throw new BadRequestException("departementId requis pour type DEPARTEMENT");
                    Departement dept = departementRepository.findById(request.departementId())
                            .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé"));
                    syndicat.setDepartement(dept);
                    syndicat.setRegion(dept.getRegion());
                }
                case REGION -> {
                    if (request.regionId() == null) throw new BadRequestException("regionId requis pour type REGION");
                    Region region = regionRepository.findById(request.regionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Région non trouvée"));
                    syndicat.setRegion(region);
                }
            }
        }

        Syndicat saved = syndicatRepository.save(syndicat);
        log.info("Syndicat modifié avec succès: {}", saved.getCode());
        return syndicatMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DU STATUT
    // ═══════════════════════════════════════════════════════════

    public SyndicatResponse suspendre(UUID id, String motif) {
        Syndicat syndicat = findByIdOrThrow(id);
        syndicat.setStatut(StatutSyndicat.SUSPENDU);
        return syndicatMapper.toResponse(syndicatRepository.save(syndicat));
    }

    public SyndicatResponse reactiver(UUID id) {
        Syndicat syndicat = findByIdOrThrow(id);
        syndicat.setStatut(StatutSyndicat.ACTIF);
        return syndicatMapper.toResponse(syndicatRepository.save(syndicat));
    }

    public void supprimer(UUID id) {
        Syndicat syndicat = findByIdOrThrow(id);

        // Détacher toutes les pharmacies de ce syndicat avant suppression
        List<Pharmacie> pharmacies = pharmacieRepository.findBySyndicatId(id);
        for (Pharmacie pharmacie : pharmacies) {
            pharmacie.setSyndicat(null);
            pharmacieRepository.save(pharmacie);
        }

        syndicatRepository.delete(syndicat);
        log.info("Syndicat supprimé définitivement: {} ({})", syndicat.getNom(), syndicat.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    /**
     * Assigner une pharmacie existante à un syndicat
     * Envoie un email à la pharmacie si elle a un email renseigné
     */
    public PharmacieResponse assignerPharmacie(UUID syndicatId, UUID pharmacieId) {
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée: " + pharmacieId));

        if (pharmacie.getSyndicat() != null && pharmacie.getSyndicat().getId().equals(syndicatId)) {
            throw new ConflictException("Cette pharmacie est déjà dans votre réseau");
        }

        pharmacie.setSyndicat(syndicat);
        syndicat.ajouterPharmacie();
        pharmacieRepository.save(pharmacie);
        syndicatRepository.save(syndicat);

        // Notifier la pharmacie par email
        if (pharmacie.getEmail() != null && !pharmacie.getEmail().isBlank()) {
            emailService.notifierPharmacieDansSyndicat(
                    pharmacie.getEmail(), pharmacie.getNom(),
                    syndicat.getNom(), syndicat.getCode());
        }

        log.info("Pharmacie {} assignée au syndicat {}", pharmacie.getCode(), syndicat.getCode());
        return pharmacieMapper.toResponse(pharmacie);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DES PAIEMENTS (Admin)
    // ═══════════════════════════════════════════════════════════

    /**
     * Enregistrer un paiement reçu pour un syndicat
     * Met à jour dateDernierPaiement et étend dateFinAbonnement d'un mois
     */
    public SyndicatResponse enregistrerPaiement(UUID syndicatId, EnregistrerPaiementRequest request) {
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        LocalDate datePaiement = request.datePaiement() != null ? request.datePaiement() : LocalDate.now();
        syndicat.setDateDernierPaiement(datePaiement);

        // Étendre ou initialiser la date de fin d'abonnement
        LocalDate baseDate = syndicat.getDateFinAbonnement() != null
                && syndicat.getDateFinAbonnement().isAfter(LocalDate.now())
                ? syndicat.getDateFinAbonnement()
                : LocalDate.now();
        syndicat.setDateFinAbonnement(baseDate.plusMonths(1));
        syndicat.setProchainPaiement(baseDate.plusMonths(1));
        syndicat.setStatutAbonnement(sn.sunufarmasi.pharmacie.enums.StatutAbonnement.ACTIF);
        syndicat.setEssaiGratuit(false);

        // Réactiver si suspendu pour impayé
        if (syndicat.getStatut() == StatutSyndicat.SUSPENDU) {
            syndicat.setStatut(StatutSyndicat.ACTIF);
        }

        log.info("Paiement enregistré pour syndicat {} - Fin abonnement: {}", syndicat.getCode(), syndicat.getDateFinAbonnement());
        return syndicatMapper.toResponse(syndicatRepository.save(syndicat));
    }

    /**
     * Renouveler l'abonnement d'un syndicat (extension d'un mois)
     */
    public SyndicatResponse renouvelerAbonnement(UUID syndicatId, int mois) {
        Syndicat syndicat = findByIdOrThrow(syndicatId);
        LocalDate baseDate = syndicat.getDateFinAbonnement() != null
                && syndicat.getDateFinAbonnement().isAfter(LocalDate.now())
                ? syndicat.getDateFinAbonnement()
                : LocalDate.now();
        syndicat.setDateFinAbonnement(baseDate.plusMonths(mois));
        syndicat.setProchainPaiement(baseDate.plusMonths(mois + 1));
        syndicat.setStatutAbonnement(sn.sunufarmasi.pharmacie.enums.StatutAbonnement.ACTIF);
        log.info("Abonnement renouvelé pour syndicat {} - Nouvelle fin: {}", syndicat.getCode(), syndicat.getDateFinAbonnement());
        return syndicatMapper.toResponse(syndicatRepository.save(syndicat));
    }

    /**
     * Assigner une pharmacie à un syndicat (version Admin)
     */
    public PharmacieResponse assignerPharmacieAdmin(UUID syndicatId, UUID pharmacieId) {
        return assignerPharmacie(syndicatId, pharmacieId);
    }

    // ═══════════════════════════════════════════════════════════
    // ZONES DISPONIBLES
    // ═══════════════════════════════════════════════════════════

    /**
     * DTO léger pour une zone géographique disponible
     */
    public record ZoneDisponibleDto(UUID id, String nom, String type) {}

    /**
     * Retourne les communes / départements / régions qui ne sont pas encore
     * assignés à un syndicat actif.
     *
     * @param type COMMUNE | DEPARTEMENT | REGION
     */
    @Transactional(readOnly = true)
    public List<ZoneDisponibleDto> getZonesDisponibles(TypeSyndicat type) {
        // Départements déjà couverts par un syndicat DEPARTEMENT actif
        Set<UUID> deptsCouvertsParSyndicat = syndicatRepository.findByType(TypeSyndicat.DEPARTEMENT).stream()
                .filter(s -> s.getStatut() != StatutSyndicat.INACTIF && s.getDepartement() != null)
                .map(s -> s.getDepartement().getId())
                .collect(Collectors.toSet());

        // Régions déjà couvertes par un syndicat REGION actif
        Set<UUID> regionsCouvertes = syndicatRepository.findByType(TypeSyndicat.REGION).stream()
                .filter(s -> s.getStatut() != StatutSyndicat.INACTIF && s.getRegion() != null)
                .map(s -> s.getRegion().getId())
                .collect(Collectors.toSet());

        return switch (type) {
            case COMMUNE -> {
                // Communes déjà prises par un syndicat COMMUNE
                Set<UUID> communesPrisesParCommuneSyndicat = syndicatRepository.findByType(TypeSyndicat.COMMUNE).stream()
                        .filter(s -> s.getStatut() != StatutSyndicat.INACTIF && s.getCommune() != null)
                        .map(s -> s.getCommune().getId())
                        .collect(Collectors.toSet());
                // Communes déjà dans un syndicat ZONE
                Set<UUID> communesPrisesParZone = syndicatRepository.findByType(TypeSyndicat.ZONE).stream()
                        .filter(s -> s.getStatut() != StatutSyndicat.INACTIF)
                        .flatMap(s -> s.getCommunesZone().stream().map(c -> c.getId()))
                        .collect(Collectors.toSet());

                yield communeRepository.findByActifTrueOrderByNomAsc().stream()
                        .filter(c -> !communesPrisesParCommuneSyndicat.contains(c.getId()))
                        .filter(c -> !communesPrisesParZone.contains(c.getId()))
                        // Exclure les communes dont le département est déjà couvert
                        .filter(c -> c.getDepartement() == null || !deptsCouvertsParSyndicat.contains(c.getDepartement().getId()))
                        // Exclure les communes dont la région est déjà couverte
                        .filter(c -> c.getDepartement() == null || c.getDepartement().getRegion() == null
                                || !regionsCouvertes.contains(c.getDepartement().getRegion().getId()))
                        .map(c -> new ZoneDisponibleDto(c.getId(), c.getNom(), "COMMUNE"))
                        .collect(Collectors.toList());
            }
            case ZONE -> {
                // Pour ZONE : retourner toutes les communes non encore totalement couvertes
                // (communes dont ni le dept ni la région ne sont couverts par un syndicat dept/region)
                Set<UUID> communesPrisesParCommuneSyndicat = syndicatRepository.findByType(TypeSyndicat.COMMUNE).stream()
                        .filter(s -> s.getStatut() != StatutSyndicat.INACTIF && s.getCommune() != null)
                        .map(s -> s.getCommune().getId())
                        .collect(Collectors.toSet());

                yield communeRepository.findByActifTrueOrderByNomAsc().stream()
                        .filter(c -> !communesPrisesParCommuneSyndicat.contains(c.getId()))
                        .filter(c -> c.getDepartement() == null || !deptsCouvertsParSyndicat.contains(c.getDepartement().getId()))
                        .filter(c -> c.getDepartement() == null || c.getDepartement().getRegion() == null
                                || !regionsCouvertes.contains(c.getDepartement().getRegion().getId()))
                        .map(c -> new ZoneDisponibleDto(c.getId(), c.getNom(), "ZONE"))
                        .collect(Collectors.toList());
            }
            case DEPARTEMENT -> {
                Set<UUID> deptsCouverts = new java.util.HashSet<>(deptsCouvertsParSyndicat);
                yield departementRepository.findByActifTrueOrderByNomAsc().stream()
                        .filter(d -> !deptsCouverts.contains(d.getId()))
                        // Exclure si la région est déjà couverte
                        .filter(d -> d.getRegion() == null || !regionsCouvertes.contains(d.getRegion().getId()))
                        .map(d -> new ZoneDisponibleDto(d.getId(), d.getNom(), "DEPARTEMENT"))
                        .collect(Collectors.toList());
            }
            case REGION -> {
                yield regionRepository.findByActifTrueOrderByNomAsc().stream()
                        .filter(r -> !regionsCouvertes.contains(r.getId()))
                        .map(r -> new ZoneDisponibleDto(r.getId(), r.getNom(), "REGION"))
                        .collect(Collectors.toList());
            }
        };
    }

    private Syndicat findByIdOrThrow(UUID id) {
        return syndicatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicat non trouvé: " + id));
    }

    private void validateCreateRequest(CreateSyndicatRequest request) {
        if (syndicatRepository.existsByUsername(request.username()))
            throw new ConflictException("Ce username est déjà utilisé");
        if (syndicatRepository.existsByEmail(request.email()))
            throw new ConflictException("Cet email est déjà utilisé");
        if (syndicatRepository.existsByTelephone(request.telephone()))
            throw new ConflictException("Ce numéro de téléphone est déjà utilisé");
        if (!request.isZoneValid()) {
            String msg = switch (request.type()) {
                case COMMUNE -> "L'ID de la commune est obligatoire pour un syndicat de type COMMUNE";
                case ZONE -> "Au moins une commune est obligatoire pour un syndicat de type ZONE";
                case DEPARTEMENT -> "L'ID du département est obligatoire pour un syndicat de type DEPARTEMENT";
                case REGION -> "L'ID de la région est obligatoire pour un syndicat de type REGION";
            };
            throw new BadRequestException(msg);
        }
        // Unicité de zone : vérifier qu'aucun autre syndicat actif ne couvre cette zone
        if (request.type() == TypeSyndicat.COMMUNE) {
            syndicatRepository.findByCommuneId(request.communeId()).ifPresent(existing ->
                    { throw new BadRequestException("Cette zone est déjà couverte par le syndicat " + existing.getNom()); }
            );
        } else if (request.type() == TypeSyndicat.DEPARTEMENT) {
            syndicatRepository.findByDepartementId(request.departementId()).ifPresent(existing ->
                    { throw new BadRequestException("Cette zone est déjà couverte par le syndicat " + existing.getNom()); }
            );
        } else if (request.type() == TypeSyndicat.REGION) {
            syndicatRepository.findRegionSyndicatByRegionId(request.regionId()).ifPresent(existing ->
                    { throw new BadRequestException("Cette zone est déjà couverte par le syndicat " + existing.getNom()); }
            );
        }
    }

    private Syndicat buildSyndicatFromRequest(CreateSyndicatRequest request) {
        Syndicat syndicat = Syndicat.builder()
                .nom(request.nom())
                .description(request.description())
                .username(request.username())
                .type(request.type())
                .telephone(request.telephone())
                .telephoneSecondaire(request.telephoneSecondaire())
                .email(request.email())
                .adresse(request.adresse())
                .nomResponsable(request.nomResponsable())
                .statut(StatutSyndicat.ACTIF)
                .build();

        if (request.type() == TypeSyndicat.COMMUNE) {
            Commune commune = communeRepository.findById(request.communeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commune non trouvée: " + request.communeId()));
            syndicat.setCommune(commune);
            syndicat.setRegion(commune.getDepartement().getRegion());
            syndicat.setPlan(PlanAbonnementSyndicat.COMMUNE);
        } else if (request.type() == TypeSyndicat.ZONE) {
            for (UUID cId : request.communeIds()) {
                Commune c = communeRepository.findById(cId)
                        .orElseThrow(() -> new ResourceNotFoundException("Commune non trouvée: " + cId));
                syndicat.getCommunesZone().add(c);
            }
            syndicat.getCommunesZone().stream().findFirst().ifPresent(c ->
                syndicat.setRegion(c.getDepartement() != null ? c.getDepartement().getRegion() : null));
            syndicat.setPlan(PlanAbonnementSyndicat.COMMUNE);
        } else if (request.type() == TypeSyndicat.DEPARTEMENT) {
            Departement departement = departementRepository.findById(request.departementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé: " + request.departementId()));
            syndicat.setDepartement(departement);
            syndicat.setRegion(departement.getRegion());
            syndicat.setPlan(PlanAbonnementSyndicat.DEPARTEMENT);
        } else {
            // REGION
            Region region = regionRepository.findById(request.regionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Région non trouvée: " + request.regionId()));
            syndicat.setRegion(region);
            syndicat.setPlan(PlanAbonnementSyndicat.REGION);
        }

        if (request.responsableId() != null) {
            Pharmacien responsable = pharmacienRepository.findById(request.responsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacien non trouvé: " + request.responsableId()));
            syndicat.setResponsable(responsable);
            syndicat.setNomResponsable(responsable.getNomComplet());
        }

        return syndicat;
    }

    private String generateCode(Syndicat syndicat) {
        String prefix = "SYN";
        String zone;
        if (syndicat.getType() == TypeSyndicat.COMMUNE && syndicat.getCommune() != null) {
            zone = syndicat.getCommune().getNom().substring(0, Math.min(3, syndicat.getCommune().getNom().length())).toUpperCase();
        } else if (syndicat.getType() == TypeSyndicat.DEPARTEMENT && syndicat.getDepartement() != null) {
            zone = syndicat.getDepartement().getNom().substring(0, Math.min(3, syndicat.getDepartement().getNom().length())).toUpperCase();
        } else if (syndicat.getType() == TypeSyndicat.REGION && syndicat.getRegion() != null) {
            zone = syndicat.getRegion().getNom().substring(0, Math.min(3, syndicat.getRegion().getNom().length())).toUpperCase();
        } else {
            zone = "XXX";
        }
        long count = syndicatRepository.count() + 1;
        return String.format("%s-%s-%03d", prefix, zone, count);
    }
}
