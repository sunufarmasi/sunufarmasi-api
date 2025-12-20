package sn.sunufarmasi.pharmacie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.pharmacie.dto.request.LoginPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.request.RegisterPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.response.AuthResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacienResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.PlanAbonnementPharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;
import sn.sunufarmasi.pharmacie.exception.PharmacienAlreadyExistsException;
import sn.sunufarmasi.pharmacie.exception.PharmacienNotFoundException;
import sn.sunufarmasi.pharmacie.mapper.PharmacienMapper;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service métier pour la gestion des pharmaciens
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacienService {

    private final PharmacienRepository pharmacienRepository;
    private final PharmacienMapper pharmacienMapper;
    // private final JwtService jwtService; // À implémenter plus tard

    /**
     * Inscription d'un nouveau pharmacien
     */
    @Transactional
    public AuthResponse register(RegisterPharmacienRequest request) {
        log.info("Inscription d'un nouveau pharmacien: {}", request.telephone());

        // Vérifier si le téléphone existe déjà
        if (pharmacienRepository.existsByTelephone(request.telephone())) {
            throw new PharmacienAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
        }

        // Vérifier si l'email existe déjà
        if (request.email() != null && pharmacienRepository.existsByEmail(request.email())) {
            throw new PharmacienAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Vérifier si le numéro d'ordre existe déjà
        if (pharmacienRepository.existsByNumeroOrdreNational(request.numeroOrdreNational())) {
            throw new PharmacienAlreadyExistsException("Ce numéro d'ordre est déjà utilisé");
        }

        // Créer le pharmacien
        Pharmacien pharmacien = Pharmacien.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .telephone(request.telephone())
                .email(request.email())
                .dateNaissance(request.dateNaissance())
                .sexe(request.sexe())
                .numeroOrdreNational(request.numeroOrdreNational())
                .universiteFormation(request.universiteFormation())
                .anneeDiplome(request.anneeDiplome())
                .motDePasseHash("TEMP_HASH") // TODO: Hasher avec BCrypt
                .type(TypePharmacien.PROPRIETAIRE)
                .plan(PlanAbonnementPharmacie.TRIAL)
                .statutAbonnement(StatutAbonnement.ACTIF)
                .montantMensuel(PlanAbonnementPharmacie.TRIAL.getMontantMensuel())
                .essaiGratuit(true)
                .dateDebutEssai(LocalDate.now())
                .dateFinEssai(LocalDate.now().plusMonths(1))
                .nombrePharmaciesMax(1)
                .nombrePharmaciesActuelles(0)
                .nombreEmployesMax(1)
                .actif(true)
                .valideParOrdre(false)
                .compteVerifie(false)
                .build();

        pharmacien = pharmacienRepository.save(pharmacien);

        log.info("Pharmacien créé avec succès: {} - ID: {}", pharmacien.getNomComplet(), pharmacien.getId());

        // Générer le token JWT
        String token = "TEMP_TOKEN"; // TODO: Générer avec JwtService

        PharmacienResponse response = pharmacienMapper.toResponse(pharmacien);
        return new AuthResponse(token, response);
    }

    /**
     * Connexion d'un pharmacien
     */
    @Transactional
    public AuthResponse login(LoginPharmacienRequest request) {
        log.info("Tentative de connexion: {}", request.telephone());

        Pharmacien pharmacien = pharmacienRepository.findByTelephone(request.telephone())
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        // Vérifier le statut
        if (!pharmacien.getActif()) {
            throw new IllegalStateException("Compte désactivé");
        }

        // TODO: Vérifier le mot de passe

        // Enregistrer la connexion
        pharmacien.enregistrerConnexion();
        pharmacienRepository.save(pharmacien);

        // Générer le token JWT
        String token = "TEMP_TOKEN"; // TODO: Générer avec JwtService

        PharmacienResponse response = pharmacienMapper.toResponse(pharmacien);
        return new AuthResponse(token, response);
    }

    /**
     * Récupérer le profil d'un pharmacien
     */
    public PharmacienResponse getProfile(UUID pharmacienId) {
        log.info("Récupération du profil: {}", pharmacienId);

        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        return pharmacienMapper.toResponse(pharmacien);
    }

    /**
     * Vérifier si un pharmacien peut créer une pharmacie
     */
    public boolean canCreatePharmacie(UUID pharmacienId) {
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        return pharmacien.peutCreerPharmacie() && pharmacien.peutGerer();
    }

    /**
     * Incrémenter le nombre de pharmacies d'un pharmacien
     */
    @Transactional
    public void incrementPharmaciesCount(UUID pharmacienId) {
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        pharmacien.ajouterPharmacie();
        pharmacienRepository.save(pharmacien);
    }

    /**
     * Décrémenter le nombre de pharmacies d'un pharmacien
     */
    @Transactional
    public void decrementPharmaciesCount(UUID pharmacienId) {
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        pharmacien.retirerPharmacie();
        pharmacienRepository.save(pharmacien);
    }
}