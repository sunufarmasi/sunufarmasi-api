package sn.sunufarmasi.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.security.JwtTokenProvider;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.shared.exception.UnauthorizedException;
import sn.sunufarmasi.user.dto.UserDto.*;
import sn.sunufarmasi.user.entity.User;
import sn.sunufarmasi.user.entity.User.RoleUser;
import sn.sunufarmasi.user.entity.User.StatutUser;
import sn.sunufarmasi.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service de gestion des utilisateurs avec authentification email/password
 * Utilise JwtTokenProvider existant (compatible avec le système OTP des patients)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Connexion avec email et mot de passe
     */
    public AuthResponse login(LoginRequest request) {
        log.info("🔐 Tentative connexion User: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        // Vérifier compte verrouillé
        if (!user.isAccountNonLocked()) {
            throw new UnauthorizedException("Compte verrouillé jusqu'à " + user.getCompteVerrouilleJusqu());
        }

        // Vérifier statut
        if (!user.canLogin()) {
            throw new UnauthorizedException("Compte " + user.getStatut().getLibelle().toLowerCase());
        }

        // Vérifier mot de passe
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            user.incrementerTentativesConnexion();
            userRepository.save(user);
            log.warn("❌ Échec connexion: {} (tentative {})", request.email(), user.getTentativesConnexion());
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        // Connexion réussie
        user.connexionReussie();
        userRepository.save(user);

        log.info("✅ Connexion réussie: {} ({})", user.getEmail(), user.getRole());

        return generateAuthResponse(user);
    }

    /**
     * Inscription
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("📝 Inscription User: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Un compte avec cet email existe déjà");
        }

        if (request.telephone() != null && userRepository.existsByTelephone(request.telephone())) {
            throw new BadRequestException("Un compte avec ce numéro existe déjà");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nom(request.nom())
                .prenom(request.prenom())
                .telephone(request.telephone())
                .role(request.role() != null ? request.role() : RoleUser.CLIENT)
                .statut(StatutUser.ACTIF)
                .emailVerifie(false)
                .build();

        User saved = userRepository.save(user);
        log.info("✅ Utilisateur créé: {} ({})", saved.getEmail(), saved.getRole());

        return generateAuthResponse(saved).withMessage("Inscription réussie");
    }

    /**
     * Rafraîchir le token
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("Token invalide ou expiré");
        }

        String userId = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!user.canLogin()) {
            throw new UnauthorizedException("Compte désactivé");
        }

        log.info("🔄 Token rafraîchi pour: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    /**
     * Générer la réponse d'authentification avec JWT
     */
    private AuthResponse generateAuthResponse(User user) {
        List<String> roles = List.of(user.getRole().name());

        String role = user.getRole().name();

//        String accessToken = jwtTokenProvider.generateToken(user.getId().toString(), roles);
//        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), roles);

        // JwtTokenProvider.generateToken(userId, role) - role est un String, pas une List
        String accessToken = jwtTokenProvider.generateToken(user.getId().toString(), role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString(), role);


        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationTime() / 1000,
                user.getId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole(),
                user.getStatut(),
                user.getEmailVerifie()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GESTION UTILISATEURS
    // ═══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(String id) {
        return getById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getByRole(RoleUser role) {
        return userRepository.findByRole(role).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String query) {
        return userRepository.search(query).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (request.nom() != null) user.setNom(request.nom());
        if (request.prenom() != null) user.setPrenom(request.prenom());
        if (request.telephone() != null) {
            if (!request.telephone().equals(user.getTelephone())
                    && userRepository.existsByTelephone(request.telephone())) {
                throw new BadRequestException("Ce numéro est déjà utilisé");
            }
            user.setTelephone(request.telephone());
        }
        if (request.adresse() != null) user.setAdresse(request.adresse());
        if (request.avatar() != null) user.setAvatar(request.avatar());

        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse updateProfile(String id, UpdateProfileRequest request) {
        return updateProfile(UUID.fromString(id), request);
    }

    public void changePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.ancienMotDePasse(), user.getPassword())) {
            throw new BadRequestException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.nouveauMotDePasse()));
        userRepository.save(user);
        log.info("🔑 Mot de passe changé pour: {}", user.getEmail());
    }

    public void changePassword(String id, ChangePasswordRequest request) {
        changePassword(UUID.fromString(id), request);
    }

    public UserResponse changeStatut(UUID id, StatutUser statut) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setStatut(statut);
        return UserResponse.from(userRepository.save(user));
    }
}
