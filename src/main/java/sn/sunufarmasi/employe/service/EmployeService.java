package sn.sunufarmasi.employe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.employe.dto.request.*;
import sn.sunufarmasi.employe.dto.response.*;
import sn.sunufarmasi.employe.entity.Employe;
import sn.sunufarmasi.employe.enums.StatutEmploye;
import sn.sunufarmasi.employe.enums.TypePermission;
import sn.sunufarmasi.employe.mapper.EmployeMapper;
import sn.sunufarmasi.employe.repository.EmployeRepository;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepository;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.security.JwtTokenProvider;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ConflictException;
import sn.sunufarmasi.shared.exception.ForbiddenException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.shared.exception.UnauthorizedException;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des employés
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final PharmacieRepository pharmacieRepository;
    private final PharmacienRepository pharmacienRepository;
    private final EmployeMapper employeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String CARACTERES_MDP = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int LONGUEUR_MDP_GENERE = 8;

    // ═══════════════════════════════════════════════════════════
    // CRÉATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un nouvel employé
     *
     * @param request Les données de l'employé
     * @param pharmacienId ID du pharmacien qui crée
     * @return L'employé créé avec le mot de passe initial
     */
    public EmployeCreatedResponse create(CreateEmployeRequest request, UUID pharmacienId) {
        log.info("Création employé par pharmacien: {} pour pharmacie: {}", pharmacienId, request.pharmacieId());

        // Récupérer le pharmacien
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacien non trouvé: " + pharmacienId));

        // Récupérer la pharmacie
        Pharmacie pharmacie = pharmacieRepository.findById(request.pharmacieId())
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée: " + request.pharmacieId()));

        // Vérifier que la pharmacie appartient au pharmacien
        if (!pharmacie.getPharmacienProprietaire().getId().equals(pharmacienId)) {
            throw new ForbiddenException("Cette pharmacie ne vous appartient pas");
        }

        // Vérifier les limites du plan
        Long nombreEmployes = employeRepository.countActifsByPharmacien(pharmacienId);
        if (pharmacien.getNombreEmployesMax() != null && nombreEmployes >= pharmacien.getNombreEmployesMax()) {
            throw new BadRequestException("Vous avez atteint la limite d'employés de votre plan ("
                    + pharmacien.getNombreEmployesMax() + " employés max)");
        }

        // Validations
        validateCreateRequest(request);

        // Générer username et code
        String code = generateCode(pharmacie);
        String username = generateUsername(pharmacie, code);

        // Générer ou utiliser le mot de passe fourni
        String motDePasseClair = request.motDePasse() != null ? request.motDePasse() : genererMotDePasse();

        // Créer l'entité
        Employe employe = Employe.builder()
                .code(code)
                .username(username)
                .nom(request.nom())
                .prenom(request.prenom())
                .telephone(request.telephone())
                .email(request.email())
                .dateNaissance(request.dateNaissance())
                .sexe(request.sexe())
                .adresse(request.adresse())
                .motDePasseHash(passwordEncoder.encode(motDePasseClair))
                .doitChangerMotDePasse(true)
                .pharmacie(pharmacie)
                .pharmacien(pharmacien)
                .poste(request.poste())
                .dateEmbauche(request.dateEmbauche())
                .dateFinContrat(request.dateFinContrat())
                .permissions(request.permissions() != null ? new HashSet<>(request.permissions()) : null)
                .statut(StatutEmploye.ACTIF)
                .build();

        // Sauvegarder
        Employe saved = employeRepository.save(employe);

        log.info("Employé créé avec succès: {} ({}) pour pharmacie: {}",
                saved.getNomComplet(), saved.getCode(), pharmacie.getCode());

        return EmployeCreatedResponse.of(
                saved.getId(),
                saved.getCode(),
                saved.getUsername(),
                saved.getNom(),
                saved.getPrenom(),
                saved.getTelephone(),
                pharmacie.getId(),
                pharmacie.getNom(),
                saved.getPermissions(),
                motDePasseClair
        );
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Connexion d'un employé
     */
    public EmployeAuthResponse login(LoginEmployeRequest request) {
        log.info("Tentative de connexion employé: {}", request.username());

        // Trouver l'employé actif
        Employe employe = employeRepository.findActiveByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Username ou mot de passe incorrect"));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.motDePasse(), employe.getMotDePasseHash())) {
            log.warn("Échec connexion employé - mot de passe incorrect: {}", request.username());
            throw new UnauthorizedException("Username ou mot de passe incorrect");
        }

        // Vérifier que la pharmacie est opérationnelle
        if (!employe.getPharmacie().estOperationnelle()) {
            throw new UnauthorizedException("La pharmacie n'est pas opérationnelle");
        }

        // Enregistrer la connexion
        employe.enregistrerConnexion();
        employeRepository.save(employe);

        // Générer les tokens JWT
        String accessToken = jwtTokenProvider.generateToken(employe.getId().toString(), "VENDEUR");
        String refreshToken = jwtTokenProvider.generateRefreshToken(employe.getId().toString(), "VENDEUR");

        log.info("Connexion employé réussie: {}", employe.getCode());

        return EmployeAuthResponse.success(
                employe.getId(),
                employe.getCode(),
                employe.getUsername(),
                employe.getNomComplet(),
                employe.getPharmacie().getId(),
                employe.getPharmacie().getNom(),
                employe.getPharmacie().getCode(),
                employe.getPermissions(),
                employe.getStatut(),
                employe.getDoitChangerMotDePasse(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationTime()
        );
    }

    /**
     * Changer le mot de passe d'un employé
     */
    public void changePassword(UUID employeId, ChangePasswordEmployeRequest request) {
        log.info("Changement mot de passe employé: {}", employeId);

        Employe employe = findByIdOrThrow(employeId);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.ancienMotDePasse(), employe.getMotDePasseHash())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }

        // Vérifier que les nouveaux mots de passe correspondent
        if (!request.motDePasseCorrespond()) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        // Mettre à jour
        employe.setMotDePasseHash(passwordEncoder.encode(request.nouveauMotDePasse()));
        employe.setDoitChangerMotDePasse(false);
        employeRepository.save(employe);

        log.info("Mot de passe employé changé avec succès: {}", employe.getCode());
    }

    /**
     * Réinitialiser le mot de passe (par le pharmacien)
     */
    public String resetPassword(UUID employeId, UUID pharmacienId) {
        log.info("Réinitialisation mot de passe employé: {} par pharmacien: {}", employeId, pharmacienId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        // Générer un nouveau mot de passe
        String nouveauMotDePasse = genererMotDePasse();

        employe.setMotDePasseHash(passwordEncoder.encode(nouveauMotDePasse));
        employe.setDoitChangerMotDePasse(true);
        employeRepository.save(employe);

        log.info("Mot de passe réinitialisé pour employé: {}", employe.getCode());

        return nouveauMotDePasse;
    }

    // ═══════════════════════════════════════════════════════════
    // LECTURE
    // ═══════════════════════════════════════════════════════════

    /**
     * Récupérer un employé par ID (détails complets)
     */
    @Transactional(readOnly = true)
    public EmployeDetailResponse getById(UUID id) {
        Employe employe = findByIdOrThrow(id);
        return employeMapper.toDetailResponse(employe);
    }

    /**
     * Récupérer un employé par code
     */
    @Transactional(readOnly = true)
    public EmployeDetailResponse getByCode(String code) {
        Employe employe = employeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec le code: " + code));
        return employeMapper.toDetailResponse(employe);
    }

    /**
     * Récupérer les employés d'une pharmacie
     */
    @Transactional(readOnly = true)
    public List<EmployeResponse> getByPharmacie(UUID pharmacieId) {
        return employeRepository.findByPharmacieId(pharmacieId).stream()
                .map(employeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les employés actifs d'une pharmacie
     */
    @Transactional(readOnly = true)
    public List<EmployeResponse> getActifsByPharmacie(UUID pharmacieId) {
        return employeRepository.findByPharmacieIdAndStatut(pharmacieId, StatutEmploye.ACTIF).stream()
                .map(employeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les employés d'un pharmacien (toutes ses pharmacies)
     */
    @Transactional(readOnly = true)
    public List<EmployeResponse> getByPharmacien(UUID pharmacienId) {
        return employeRepository.findByPharmacienId(pharmacienId).stream()
                .map(employeMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des employés par nom dans une pharmacie
     */
    @Transactional(readOnly = true)
    public List<EmployeResponse> searchByNom(UUID pharmacieId, String search) {
        return employeRepository.searchByPharmacieAndNom(pharmacieId, search).stream()
                .map(employeMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // MODIFICATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Modifier un employé
     */
    public EmployeResponse update(UUID employeId, UpdateEmployeRequest request, UUID pharmacienId) {
        log.info("Modification employé: {} par pharmacien: {}", employeId, pharmacienId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        // Mettre à jour les champs non null
        if (request.nom() != null) {
            employe.setNom(request.nom());
        }
        if (request.prenom() != null) {
            employe.setPrenom(request.prenom());
        }
        if (request.telephone() != null) {
            if (!employe.getTelephone().equals(request.telephone())
                    && employeRepository.existsByTelephone(request.telephone())) {
                throw new ConflictException("Ce numéro de téléphone est déjà utilisé");
            }
            employe.setTelephone(request.telephone());
        }
        if (request.email() != null) {
            if (employe.getEmail() != null && !employe.getEmail().equals(request.email())
                    && employeRepository.existsByEmail(request.email())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            employe.setEmail(request.email());
        }
        if (request.dateNaissance() != null) {
            employe.setDateNaissance(request.dateNaissance());
        }
        if (request.sexe() != null) {
            employe.setSexe(request.sexe());
        }
        if (request.adresse() != null) {
            employe.setAdresse(request.adresse());
        }
        if (request.poste() != null) {
            employe.setPoste(request.poste());
        }
        if (request.dateFinContrat() != null) {
            employe.setDateFinContrat(request.dateFinContrat());
        }
        if (request.photoUrl() != null) {
            employe.setPhotoUrl(request.photoUrl());
        }

        Employe saved = employeRepository.save(employe);

        log.info("Employé modifié avec succès: {}", saved.getCode());

        return employeMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DES PERMISSIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Mettre à jour les permissions d'un employé
     */
    public EmployeResponse updatePermissions(UUID employeId, UpdatePermissionsRequest request, UUID pharmacienId) {
        log.info("Mise à jour permissions employé: {} par pharmacien: {}", employeId, pharmacienId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.setToutesPermissions(request.permissions());

        Employe saved = employeRepository.save(employe);

        log.info("Permissions mises à jour pour employé: {} - {} permissions",
                saved.getCode(), saved.getPermissions().size());

        return employeMapper.toResponse(saved);
    }

    /**
     * Ajouter une permission à un employé
     */
    public EmployeResponse addPermission(UUID employeId, TypePermission permission, UUID pharmacienId) {
        log.info("Ajout permission {} à employé: {}", permission, employeId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.ajouterPermission(permission);

        Employe saved = employeRepository.save(employe);

        return employeMapper.toResponse(saved);
    }

    /**
     * Retirer une permission à un employé
     */
    public EmployeResponse removePermission(UUID employeId, TypePermission permission, UUID pharmacienId) {
        log.info("Retrait permission {} de employé: {}", permission, employeId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.retirerPermission(permission);

        Employe saved = employeRepository.save(employe);

        return employeMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════════
    // GESTION DU STATUT
    // ═══════════════════════════════════════════════════════════

    /**
     * Suspendre un employé
     */
    public EmployeResponse suspendre(UUID employeId, String motif, UUID pharmacienId) {
        log.info("Suspension employé: {} - Motif: {}", employeId, motif);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.setStatut(StatutEmploye.SUSPENDU);
        employe.setMotifSuspension(motif);

        Employe saved = employeRepository.save(employe);

        log.info("Employé suspendu: {}", saved.getCode());

        return employeMapper.toResponse(saved);
    }

    /**
     * Réactiver un employé
     */
    public EmployeResponse reactiver(UUID employeId, UUID pharmacienId) {
        log.info("Réactivation employé: {}", employeId);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.setStatut(StatutEmploye.ACTIF);
        employe.setMotifSuspension(null);

        Employe saved = employeRepository.save(employe);

        log.info("Employé réactivé: {}", saved.getCode());

        return employeMapper.toResponse(saved);
    }

    /**
     * Désactiver définitivement un employé (licenciement/démission)
     */
    public void desactiver(UUID employeId, String motif, UUID pharmacienId) {
        log.info("Désactivation définitive employé: {} - Motif: {}", employeId, motif);

        Employe employe = findByIdOrThrow(employeId);
        verifierProprietaire(employe, pharmacienId);

        employe.setStatut(StatutEmploye.INACTIF);
        employe.setMotifSuspension(motif);

        employeRepository.save(employe);

        log.info("Employé désactivé: {}", employe.getCode());
    }

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES PRIVÉES
    // ═══════════════════════════════════════════════════════════

    private Employe findByIdOrThrow(UUID id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé: " + id));
    }

    private void verifierProprietaire(Employe employe, UUID pharmacienId) {
        if (!employe.getPharmacien().getId().equals(pharmacienId)) {
            throw new ForbiddenException("Cet employé ne vous appartient pas");
        }
    }

    private void validateCreateRequest(CreateEmployeRequest request) {
        // Vérifier unicité téléphone
        if (employeRepository.existsByTelephone(request.telephone())) {
            throw new ConflictException("Ce numéro de téléphone est déjà utilisé");
        }

        // Vérifier unicité email si fourni
        if (request.email() != null && employeRepository.existsByEmail(request.email())) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
    }

    private String generateCode(Pharmacie pharmacie) {
        String prefixPharmacie = pharmacie.getCode().replace("PHAR-", "").substring(0, Math.min(6, pharmacie.getCode().length()));
        long count = employeRepository.countByPharmacieId(pharmacie.getId()) + 1;
        return String.format("EMP-%s-%03d", prefixPharmacie, count);
    }

    private String generateUsername(Pharmacie pharmacie, String code) {
        // Format: emp.codepharmacie.numero (ex: emp.dkr001.001)
        String pharmacieCode = pharmacie.getCode().replace("PHAR-", "").toLowerCase();
        String numero = code.substring(code.lastIndexOf("-") + 1);
        return String.format("emp.%s.%s", pharmacieCode, numero);
    }

    private String genererMotDePasse() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LONGUEUR_MDP_GENERE);
        for (int i = 0; i < LONGUEUR_MDP_GENERE; i++) {
            sb.append(CARACTERES_MDP.charAt(random.nextInt(CARACTERES_MDP.length())));
        }
        return sb.toString();
    }
}