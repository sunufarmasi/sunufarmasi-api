package sn.sunufarmasi.patient.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.patient.dto.request.UpdatePatientRequest;
import sn.sunufarmasi.patient.dto.response.PatientResponse;
import sn.sunufarmasi.patient.entity.Patient;
import sn.sunufarmasi.patient.mapper.PatientMapper;
import sn.sunufarmasi.patient.repository.PatientRepository;
import sn.sunufarmasi.shared.constant.ErrorMessages;
import sn.sunufarmasi.shared.exception.ConflictException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.subscription.service.SubscriptionService;

import java.util.UUID;

/**
 * Service pour la gestion des patients
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;
    private final CommuneRepository communeRepository;
    private final PatientMapper patientMapper;
    private final SubscriptionService subscriptionService;

    // ═══════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Créer un patient avec essai gratuit automatique
     *
     * WORKFLOW :
     * 1. Patient s'inscrit avec Téléphone + Nom + Commune
     * 2. Patient créé en base
     * 3. Essai gratuit 15 jours activé automatiquement
     * 4. PIN de sécurité créé LOCALEMENT dans l'app (pas en base)
     */
    @Transactional
    public PatientResponse createPatient(
            String nomComplet,
            String telephone,
            String communeId,
            java.time.LocalDate dateNaissance,
            String sexe,
            String adresse
    ) {
        log.info("📝 Création patient: {}", telephone);

        // Vérifier que le téléphone n'existe pas
        if (patientRepository.existsByTelephone(telephone)) {
            throw new ConflictException(ErrorMessages.PHONE_EXISTS);
        }

        // Vérifier que la commune existe
        Commune commune = communeRepository.findById(UUID.fromString(communeId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.COMMUNE_NOT_FOUND));

        // Créer le patient
        Patient patient = Patient.builder()
                .nomComplet(nomComplet)
                .telephone(telephone)
                .commune(commune)
                .dateNaissance(dateNaissance)
                .sexe(sexe)
                .adresse(adresse)
                .telephoneVerified(false)  // À vérifier via OTP
                .emailVerified(false)
                .actif(true)
                .build();

        patient = patientRepository.save(patient);

        log.info("✅ Patient créé: {}", patient.getId());

        // Activer l'essai gratuit automatiquement
        subscriptionService.activateFreeTrial(patient);

        log.info("🎁 Essai gratuit 15 jours activé pour patient: {}", patient.getId());

        return patientMapper.toResponse(patient);
    }

    // ═══════════════════════════════════════════════════════════
    // CRUD
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir un patient par ID
     */
    public PatientResponse getPatientById(String patientId) {
        log.debug("Récupération patient: {}", patientId);

        Patient patient = getPatientEntityById(patientId);
        return patientMapper.toResponse(patient);
    }

    /**
     * Mettre à jour un patient
     */
    @Transactional
    public PatientResponse updatePatient(String patientId, UpdatePatientRequest request) {
        log.info("Mise à jour patient: {}", patientId);

        Patient patient = getPatientEntityById(patientId);
        final UUID currentPatientId = patient.getId();

        // Mettre à jour les champs fournis
        if (request.nomComplet() != null) {
            patient.setNomComplet(request.nomComplet());
        }

        if (request.telephone() != null) {
            // Vérifier que le téléphone n'est pas déjà utilisé par un autre patient
            patientRepository.findByTelephone(request.telephone())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(currentPatientId)) {
                            throw new ConflictException(ErrorMessages.PHONE_EXISTS);
                        }
                    });
            patient.setTelephone(request.telephone());
        }

        if (request.email() != null) {
            // Vérifier que l'email n'est pas déjà utilisé par un autre patient
            patientRepository.findByEmail(request.email())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(currentPatientId)) {
                            throw new ConflictException(ErrorMessages.EMAIL_EXISTS);
                        }
                    });
            patient.setEmail(request.email());
        }

        if (request.communeId() != null) {
            Commune commune = communeRepository.findById(UUID.fromString(request.communeId()))
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.COMMUNE_NOT_FOUND));
            patient.setCommune(commune);
        }

        if (request.dateNaissance() != null) {
            patient.setDateNaissance(request.dateNaissance());
        }

        if (request.sexe() != null) {
            patient.setSexe(request.sexe());
        }

        if (request.adresse() != null) {
            patient.setAdresse(request.adresse());
        }

        if (request.photoUrl() != null) {
            patient.setPhotoUrl(request.photoUrl());
        }

        patient = patientRepository.save(patient);

        log.info("✅ Patient mis à jour: {}", patient.getId());

        return patientMapper.toResponse(patient);
    }

    /**
     * Désactiver un patient
     */
    @Transactional
    public void deactivatePatient(String patientId) {
        log.info("Désactivation patient: {}", patientId);

        Patient patient = getPatientEntityById(patientId);
        patient.setActif(false);
        patientRepository.save(patient);

        log.info("✅ Patient désactivé: {}", patient.getId());
    }

    /**
     * Réactiver un patient
     */
    @Transactional
    public void reactivatePatient(String patientId) {
        log.info("Réactivation patient: {}", patientId);

        Patient patient = getPatientEntityById(patientId);
        patient.setActif(true);
        patientRepository.save(patient);

        log.info("✅ Patient réactivé: {}", patient.getId());
    }

    // ═══════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Obtenir l'entity Patient par ID
     */
    private Patient getPatientEntityById(String patientId) {
        return patientRepository.findById(UUID.fromString(patientId))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

    /**
     * Vérifier si un téléphone existe
     */
    public boolean telephoneExists(String telephone) {
        return patientRepository.existsByTelephone(telephone);
    }
}