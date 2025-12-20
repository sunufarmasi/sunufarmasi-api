package sn.sunufarmasi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.auth.dto.request.LoginRequest;
import sn.sunufarmasi.auth.dto.request.VerifyOtpRequest;
import sn.sunufarmasi.auth.dto.response.LoginResponse;
import sn.sunufarmasi.auth.entity.OtpCode;
import sn.sunufarmasi.auth.entity.OtpType;
import sn.sunufarmasi.patient.dto.request.RegisterPatientRequest;
import sn.sunufarmasi.patient.dto.response.PatientResponse;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.patient.service.PatientService;
import sn.sunufarmasi.security.JwtTokenProvider;
import sn.sunufarmasi.shared.constant.ErrorMessages;
import sn.sunufarmasi.shared.constant.SuccessMessages;
import sn.sunufarmasi.shared.enums.UserType;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.shared.exception.UnauthorizedException;

import java.util.List;
import java.util.UUID;

/**
 * Service d'authentification
 * Gère le login, l'inscription et la vérification OTP
 *
 * NOUVEAU WORKFLOW :
 * - Inscription : Téléphone + Nom + Commune
 * - OTP envoyé automatiquement
 * - Vérification OTP → Compte activé + Essai gratuit 15 jours
 * - Login : Téléphone → OTP → JWT token
 * - PAS de mot de passe en base (PIN local uniquement)
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final OtpService otpService;
    private final JwtTokenProvider jwtTokenProvider;

    // ═══════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Inscription d'un nouveau patient
     *
     * WORKFLOW :
     * 1. Créer patient en base (téléphone + nom + commune)
     * 2. Essai gratuit 15 jours activé automatiquement
     * 3. Envoyer OTP au téléphone
     *
     * @param request Données d'inscription
     * @return Code OTP (pour dev/test uniquement)
     */
    @Transactional
    public String registerPatient(RegisterPatientRequest request) {
        log.info("📝 Inscription patient: {}", request.telephone());

        // 1. Créer le patient (+ activation essai gratuit auto)
        PatientResponse patientResponse = patientService.createPatient(
                request.nomComplet(),
                request.telephone(),
                request.communeId(),
                request.dateNaissance(),
                request.sexe(),
                request.adresse()
        );

        // 2. Générer et envoyer l'OTP
        OtpCode otpCode = otpService.generateAndSendOtp(
                patientResponse.telephone(),
                OtpType.REGISTRATION
        );

        log.info("✅ Patient inscrit: {} - OTP envoyé", patientResponse.id());

        return otpCode.getCode(); // TODO: Ne pas retourner en production
    }

    /**
     * Vérifier le code OTP après inscription
     *
     * @param request Données de vérification
     * @return JWT token + infos utilisateur
     */
    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest request) {
        log.info("🔐 Vérification OTP pour: {}", request.telephone());

        // 1. Vérifier l'OTP
        otpService.verifyOtp(request.telephone(), request.codeOtp(), OtpType.REGISTRATION);

        // 2. Trouver le patient
        Patient patient = patientRepository.findByTelephone(request.telephone())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // 3. Marquer le téléphone comme vérifié
        patient.setTelephoneVerified(true);
        patientRepository.save(patient);

        log.info("✅ Téléphone vérifié: {}", patient.getTelephone());

        // 4. Générer les tokens JWT
        return generateLoginResponse(patient);
    }

    /**
     * Renvoyer un code OTP
     *
     * @param telephone Téléphone de l'utilisateur
     * @return Code OTP (pour dev/test uniquement)
     */
    @Transactional
    public String resendOtp(String telephone) {
        log.info("🔄 Renvoi OTP pour: {}", telephone);

        // Vérifier que l'utilisateur existe
        if (!patientRepository.existsByTelephone(telephone)) {
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        // Générer et envoyer un nouvel OTP
        OtpCode otpCode = otpService.generateAndSendOtp(telephone, OtpType.REGISTRATION);

        return otpCode.getCode();
    }

    // ═══════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════

    /**
     * Demander un OTP pour se connecter
     *
     * @param request Contient le téléphone
     * @return Code OTP (pour dev/test uniquement)
     */
    @Transactional
    public String requestLoginOtp(LoginRequest request) {
        log.info("📱 Demande OTP login pour: {}", request.telephone());

        // Vérifier que l'utilisateur existe
        Patient patient = patientRepository.findByTelephone(request.telephone())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // Vérifier que le compte est actif
        if (!patient.isActif()) {
            throw new UnauthorizedException(ErrorMessages.AUTH_ACCOUNT_DISABLED);
        }

        // Générer et envoyer l'OTP
        OtpCode otpCode = otpService.generateAndSendOtp(
                patient.getTelephone(),
                OtpType.LOGIN
        );

        log.info("✅ OTP envoyé à: {}", patient.getTelephone());

        return otpCode.getCode(); // TODO: Ne pas retourner en production
    }

    /**
     * Se connecter avec OTP
     *
     * @param request Téléphone + code OTP
     * @return LoginResponse avec tokens JWT
     */
    @Transactional
    public LoginResponse loginWithOtp(VerifyOtpRequest request) {
        log.info("🔐 Connexion avec OTP: {}", request.telephone());

        // 1. Vérifier l'OTP
        otpService.verifyOtp(request.telephone(), request.codeOtp(), OtpType.LOGIN);

        // 2. Récupérer le patient
        Patient patient = patientRepository.findByTelephone(request.telephone())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // 3. Vérifier que le compte est vérifié
        if (!patient.isTelephoneVerified()) {
            throw new UnauthorizedException(ErrorMessages.AUTH_ACCOUNT_NOT_VERIFIED);
        }

        // 4. Vérifier que le compte est actif
        if (!patient.isActif()) {
            throw new UnauthorizedException(ErrorMessages.AUTH_ACCOUNT_DISABLED);
        }

        // 5. Mettre à jour la dernière connexion
        patient.updateLastLogin();
        patientRepository.save(patient);

        log.info("✅ Connexion réussie: {}", patient.getTelephone());

        // 6. Générer la réponse
        return generateLoginResponse(patient);
    }

    /**
     * Rafraîchir un token JWT
     *
     * @param refreshToken Refresh token
     * @return Nouveaux tokens
     */
    public LoginResponse refreshToken(String refreshToken) {
        log.info("🔄 Rafraîchissement du token");

        // 1. Valider le refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException(ErrorMessages.AUTH_TOKEN_INVALID);
        }

        // 2. Extraire l'ID patient
        String patientId = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 3. Récupérer le patient
        Patient patient = patientRepository.findById(UUID.fromString(patientId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        // 4. Vérifier que le compte est toujours actif
        if (!patient.canLogin()) {
            throw new UnauthorizedException(ErrorMessages.AUTH_ACCOUNT_DISABLED);
        }

        log.info("✅ Token rafraîchi pour: {}", patient.getTelephone());

        // 5. Générer de nouveaux tokens
        return generateLoginResponse(patient);
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Générer une LoginResponse avec JWT tokens
     */
    private LoginResponse generateLoginResponse(Patient patient) {
        // Générer les tokens JWT (on utilise l'ID comme subject)
        List<String> roles = List.of(UserType.PATIENT.name());
        String accessToken = jwtTokenProvider.generateAccessToken(
                patient.getId().toString(),
                roles
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                patient.getId().toString()
        );

        // Créer les infos utilisateur
        LoginResponse.UserInfoDto userInfo = new LoginResponse.UserInfoDto(
                patient.getNomComplet(),
                patient.getTelephone(),
                patient.getPhotoUrl(),
                patient.isTelephoneVerified()
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtTokenProvider.getTimeToExpiration(accessToken),
                patient.getId().toString(),
                patient.getTelephone(),
                UserType.PATIENT.name(),
                userInfo
        );
    }
}