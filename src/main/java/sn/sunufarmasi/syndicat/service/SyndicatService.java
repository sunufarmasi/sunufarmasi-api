package sn.sunufarmasi.syndicat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.entity.Departement;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.localisation.repository.DepartementRepository;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
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

import java.util.List;
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
    private final PharmacienRepository pharmacienRepository;
    private final PharmacieRepositorye pharmacieRepository;
    private final SyndicatMapper syndicatMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION (Admin uniquement)
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouveau syndicat
     *
     * @param request Les données du syndicat
     * @param adminId ID de l'admin qui crée
     * @param adminNom Nom de l'admin pour historique
     * @return Le syndicat créé
     */
    public SyndicatResponse create(CreateSyndicatRequest request, UUID adminId, String adminNom) {
        log.info("Création syndicat: {} par admin: {}", request.nom(), adminId);

        // Validations
        validateCreateRequest(request);

        // Créer l'entité
        Syndicat syndicat = buildSyndicatFromRequest(request);

        // Définir les métadonnées admin
        syndicat.setCreeParId(adminId);
        syndicat.setCreeParNom(adminNom);

        // Encoder le mot de passe
        syndicat.setMotDePasseHash(passwordEncoder.encode(request.motDePasse()));

        // Générer le code unique
        syndicat.setCode(generateCode(syndicat));

        // Sauvegarder
        Syndicat saved = syndicatRepository.save(syndicat);

        log.info("Syndicat créé avec succès: {} ({})", saved.getNom(), saved.getCode());

        return syndicatMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Connexion d'un syndicat
     */
    public SyndicatAuthResponse login(LoginSyndicatRequest request) {
        log.info("Tentative de connexion syndicat: {}", request.username());

        // Trouver le syndicat
        Syndicat syndicat = syndicatRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Username ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.motDePasse(), syndicat.getMotDePasseHash())) {
            log.warn("Échec connexion syndicat - mot de passe incorrect: {}", request.username());
            throw new UnauthorizedException("Username ou mot de passe incorrect");
        }

        // Vérifier le statut
        if (syndicat.getStatut() != StatutSyndicat.ACTIF) {
            log.warn("Échec connexion syndicat - compte non actif: {}", request.username());
            throw new UnauthorizedException("Ce compte syndicat est " + syndicat.getStatut().getLibelle().toLowerCase());
        }

        // Enregistrer la connexion
        syndicat.enregistrerConnexion();
        syndicatRepository.save(syndicat);

        // Générer les tokens JWT
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

    /**
     * Changer le mot de passe
     */
    public void changePassword(UUID syndicatId, ChangePasswordSyndicatRequest request) {
        log.info("Changement mot de passe syndicat: {}", syndicatId);

        Syndicat syndicat = findByIdOrThrow(syndicatId);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.ancienMotDePasse(), syndicat.getMotDePasseHash())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!request.motDePasseCorrespond()) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        // Mettre à jour
        syndicat.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        syndicatRepository.save(syndicat);

        log.info("Mot de passe syndicat changé avec succès: {}", syndicat.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer un syndicat par ID (détails complets)
     */
    @Transactional(readOnly = true)
    public SyndicatDetailResponse getById(UUID id) {
        Syndicat syndicat = findByIdOrThrow(id);

        // Récupérer les statistiques
        Integer validees = countPharmaciesByStatut(id, StatutPharmacie.VALIDEE);
        Integer enAttente = countPharmaciesByStatut(id, StatutPharmacie.EN_ATTENTE);

        return syndicatMapper.toDetailResponse(syndicat, validees, enAttente);
    }

    /**
     * Récupérer un syndicat par code
     */
    @Transactional(readOnly = true)
    public SyndicatDetailResponse getByCode(String code) {
        Syndicat syndicat = syndicatRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicat non trouvé avec le code: " + code));

        Integer validees = countPharmaciesByStatut(syndicat.getId(), StatutPharmacie.VALIDEE);
        Integer enAttente = countPharmaciesByStatut(syndicat.getId(), StatutPharmacie.EN_ATTENTE);

        return syndicatMapper.toDetailResponse(syndicat, validees, enAttente);
    }

    /**
     * Récupérer tous les syndicats
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> getAll() {
        return syndicatRepository.findAll().stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les syndicats par type
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByType(TypeSyndicat type) {
        return syndicatRepository.findByType(type).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les syndicats par statut
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByStatut(StatutSyndicat statut) {
        return syndicatRepository.findByStatut(statut).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les syndicats d'une région
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> getByRegion(UUID regionId) {
        return syndicatRepository.findByRegionId(regionId).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher par nom
     */
    @Transactional(readOnly = true)
    public List<SyndicatResponse> searchByNom(String nom) {
        return syndicatRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(syndicatMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Trouver le syndicat qui gère une commune
     */
    @Transactional(readOnly = true)
    public SyndicatResponse findGestionnaireByCommune(UUID communeId) {
        List<Syndicat> syndicats = syndicatRepository.findSyndicatsGestionnairesByCommune(communeId);

        if (syndicats.isEmpty()) {
            return null;
        }

        // Priorité : COMMUNE > DEPARTEMENT
        return syndicatMapper.toResponse(syndicats.get(0));
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier un syndicat
     */
    public SyndicatResponse update(UUID id, UpdateSyndicatRequest request) {
        log.info("Modification syndicat: {}", id);

        Syndicat syndicat = findByIdOrThrow(id);

        // Mettre à jour les champs non null
        if (request.nom() != null) {
            syndicat.setNom(request.nom());
        }
        if (request.description() != null) {
            syndicat.setDescription(request.description());
        }
        if (request.telephone() != null) {
            syndicat.setTelephone(request.telephone());
        }
        if (request.telephoneSecondaire() != null) {
            syndicat.setTelephoneSecondaire(request.telephoneSecondaire());
        }
        if (request.email() != null) {
            // Vérifier unicité
            if (!syndicat.getEmail().equals(request.email())
                    && syndicatRepository.existsByEmail(request.email())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            syndicat.setEmail(request.email());
        }
        if (request.adresse() != null) {
            syndicat.setAdresse(request.adresse());
        }

        // Responsable
        if (request.responsableId() != null) {
            Pharmacien responsable = pharmacienRepository.findById(request.responsableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pharmacien non trouvé: " + request.responsableId()));
            syndicat.setResponsable(responsable);
            syndicat.setNomResponsable(responsable.getNomComplet());
        } else if (request.nomResponsable() != null) {
            syndicat.setNomResponsable(request.nomResponsable());
        }

        Syndicat saved = syndicatRepository.save(syndicat);

        log.info("Syndicat modifié avec succès: {}", saved.getCode());

        return syndicatMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DU STATUT
    // ═══════════════════════════════════════════════════════════

    /**
     * Suspendre un syndicat
     */
    public SyndicatResponse suspendre(UUID id, String motif) {
        log.info("Suspension syndicat: {} - Motif: {}", id, motif);

        Syndicat syndicat = findByIdOrThrow(id);
        syndicat.setStatut(StatutSyndicat.SUSPENDU);

        Syndicat saved = syndicatRepository.save(syndicat);

        log.info("Syndicat suspendu: {}", saved.getCode());

        return syndicatMapper.toResponse(saved);
    }

    /**
     * Réactiver un syndicat
     */
    public SyndicatResponse reactiver(UUID id) {
        log.info("Réactivation syndicat: {}", id);

        Syndicat syndicat = findByIdOrThrow(id);
        syndicat.setStatut(StatutSyndicat.ACTIF);

        Syndicat saved = syndicatRepository.save(syndicat);

        log.info("Syndicat réactivé: {}", saved.getCode());

        return syndicatMapper.toResponse(saved);
    }

    /**
     * Désactiver définitivement un syndicat
     */
    public void desactiver(UUID id) {
        log.info("Désactivation définitive syndicat: {}", id);

        Syndicat syndicat = findByIdOrThrow(id);
        syndicat.setStatut(StatutSyndicat.INACTIF);

        syndicatRepository.save(syndicat);

        log.info("Syndicat désactivé: {}", syndicat.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private Syndicat findByIdOrThrow(UUID id) {
        return syndicatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Syndicat non trouvé: " + id));
    }

    private void validateCreateRequest(CreateSyndicatRequest request) {
        // Vérifier unicité username
        if (syndicatRepository.existsByUsername(request.username())) {
            throw new ConflictException("Ce username est déjà utilisé");
        }

        // Vérifier unicité email
        if (syndicatRepository.existsByEmail(request.email())) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        // Vérifier unicité téléphone
        if (syndicatRepository.existsByTelephone(request.telephone())) {
            throw new ConflictException("Ce numéro de téléphone est déjà utilisé");
        }

        // Vérifier la cohérence type/zone
        if (!request.isZoneValid()) {
            throw new BadRequestException(
                    request.type() == TypeSyndicat.COMMUNE
                            ? "L'ID de la commune est obligatoire pour un syndicat de type COMMUNE"
                            : "L'ID du département est obligatoire pour un syndicat de type DEPARTEMENT"
            );
        }

        // Vérifier qu'il n'existe pas déjà un syndicat pour cette zone
        if (request.type() == TypeSyndicat.COMMUNE) {
            if (syndicatRepository.existsByCommuneId(request.communeId())) {
                throw new ConflictException("Un syndicat existe déjà pour cette commune");
            }
        } else {
            if (syndicatRepository.existsByDepartementId(request.departementId())) {
                throw new ConflictException("Un syndicat existe déjà pour ce département");
            }
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

        // Configurer la zone géographique
        if (request.type() == TypeSyndicat.COMMUNE) {
            Commune commune = communeRepository.findById(request.communeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Commune non trouvée: " + request.communeId()));
            syndicat.setCommune(commune);
            syndicat.setRegion(commune.getDepartement().getRegion());

            // Plan COMMUNE
            syndicat.setPlan(PlanAbonnementSyndicat.COMMUNE);

        } else {
            Departement departement = departementRepository.findById(request.departementId())
                    .orElseThrow(() -> new ResourceNotFoundException("Département non trouvé: " + request.departementId()));
            syndicat.setDepartement(departement);
            syndicat.setRegion(departement.getRegion());

            // Plan DEPARTEMENT
            syndicat.setPlan(PlanAbonnementSyndicat.DEPARTEMENT);
        }

        // Responsable (optionnel)
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
        } else if (syndicat.getDepartement() != null) {
            zone = syndicat.getDepartement().getNom().substring(0, Math.min(3, syndicat.getDepartement().getNom().length())).toUpperCase();
        } else {
            zone = "XXX";
        }

        // Compter les syndicats existants pour numéroter
        long count = syndicatRepository.count() + 1;

        return String.format("%s-%s-%03d", prefix, zone, count);
    }

    private Integer countPharmaciesByStatut(UUID syndicatId, StatutPharmacie statut) {
//        try {
//            return pharmacieRepository.countBySyndicatIdAndStatut(syndicatId, statut);
//        } catch (Exception e) {
//            return 0;
//        }
        return 0;
    }
}
