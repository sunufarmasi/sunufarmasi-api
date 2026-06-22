package sn.sunufarmasi.pharmacie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.pharmacie.dto.request.LoginPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.request.RegisterPharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.request.UpdatePharmacienRequest;
import sn.sunufarmasi.pharmacie.dto.response.AuthResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacienResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.PlanAbonnementPharmacie;
import sn.sunufarmasi.pharmacie.enums.StatutAbonnement;
import sn.sunufarmasi.pharmacie.enums.TypePharmacien;
import sn.sunufarmasi.pharmacie.exception.PharmacienAlreadyExistsException;
import sn.sunufarmasi.pharmacie.exception.PharmacienNotFoundException;
import sn.sunufarmasi.pharmacie.mapper.PharmacienMapper;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.notification.service.EmailService;
import sn.sunufarmasi.security.JwtTokenProvider;

import java.time.LocalDate;
import java.util.List;
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
    private final PharmacieRepositorye pharmacieRepository;
    private final PharmacienMapper pharmacienMapper;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

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

        // Vérifier si l'email existe déjà (ignorer les emails vides)
        if (request.email() != null && !request.email().isBlank() && pharmacienRepository.existsByEmail(request.email())) {
            throw new PharmacienAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Vérifier si le numéro d'ordre existe déjà (seulement s'il est renseigné)
        if (request.numeroOrdreNational() != null && !request.numeroOrdreNational().isBlank()
                && pharmacienRepository.existsByNumeroOrdreNational(request.numeroOrdreNational())) {
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
                .motDePasseHash(passwordEncoder.encode(request.telephone()))
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

        // Envoyer email de bienvenue
        if (pharmacien.getEmail() != null && !pharmacien.getEmail().isBlank()) {
            String nomComplet = pharmacien.getPrenom() + " " + pharmacien.getNom();
            String html = buildPharmacienWelcomeEmail(nomComplet, pharmacien.getEmail(), pharmacien.getTelephone());
            emailService.sendEmail(pharmacien.getEmail(), "Bienvenue sur SunuFarmasi — Votre compte pharmacien", html);
        }

        // Générer le token JWT
        String token = jwtTokenProvider.generateToken(pharmacien.getId().toString(), "PHARMACIEN");

        PharmacienResponse response = pharmacienMapper.toResponse(pharmacien);
        return new AuthResponse(token, response);
    }

    /**
     * Connexion d'un pharmacien
     */
    @Transactional
    public AuthResponse login(LoginPharmacienRequest request) {
        String identifier = request.email() != null && !request.email().isBlank()
                ? request.email() : request.telephone();
        log.info("Tentative de connexion pharmacien: {}", identifier);

        Pharmacien pharmacien;
        if (request.email() != null && !request.email().isBlank()) {
            pharmacien = pharmacienRepository.findByEmail(request.email())
                    .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));
        } else {
            pharmacien = pharmacienRepository.findByTelephone(request.telephone())
                    .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));
        }

        // Vérifier le statut
        if (!pharmacien.getActif()) {
            throw new IllegalStateException("Compte désactivé");
        }

        // Vérifier le mot de passe (mot de passe par défaut = numéro de téléphone)
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            if (!passwordEncoder.matches(request.motDePasse(), pharmacien.getMotDePasseHash())) {
                throw new IllegalArgumentException("Mot de passe incorrect");
            }
        }

        // Enregistrer la connexion
        pharmacien.enregistrerConnexion();
        pharmacienRepository.save(pharmacien);

        // Générer le token JWT
        String token = jwtTokenProvider.generateToken(pharmacien.getId().toString(), "PHARMACIEN");

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
     * Modifier un pharmacien (Admin)
     */
    @Transactional
    public PharmacienResponse update(UUID pharmacienId, UpdatePharmacienRequest request) {
        log.info("Admin - Modification pharmacien: {}", pharmacienId);
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        if (request.nom() != null) pharmacien.setNom(request.nom());
        if (request.prenom() != null) pharmacien.setPrenom(request.prenom());
        if (request.email() != null) {
            if (!request.email().equals(pharmacien.getEmail())
                    && pharmacienRepository.existsByEmail(request.email())) {
                throw new PharmacienAlreadyExistsException("Cet email est déjà utilisé");
            }
            pharmacien.setEmail(request.email());
        }
        if (request.telephone() != null) {
            if (!request.telephone().equals(pharmacien.getTelephone())
                    && pharmacienRepository.existsByTelephone(request.telephone())) {
                throw new PharmacienAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
            }
            pharmacien.setTelephone(request.telephone());
        }
        if (request.numeroOrdreNational() != null) {
            if (!request.numeroOrdreNational().equals(pharmacien.getNumeroOrdreNational())
                    && pharmacienRepository.existsByNumeroOrdreNational(request.numeroOrdreNational())) {
                throw new PharmacienAlreadyExistsException("Ce numéro d'ordre est déjà utilisé");
            }
            pharmacien.setNumeroOrdreNational(request.numeroOrdreNational());
        }
        if (request.universiteFormation() != null) pharmacien.setUniversiteFormation(request.universiteFormation());
        if (request.anneeDiplome() != null) pharmacien.setAnneeDiplome(request.anneeDiplome());
        if (request.type() != null) pharmacien.setType(request.type());
        if (request.actif() != null) pharmacien.setActif(request.actif());
        if (request.sexe() != null) pharmacien.setSexe(request.sexe());

        pharmacien = pharmacienRepository.save(pharmacien);
        log.info("Pharmacien modifié avec succès: {}", pharmacien.getNomComplet());
        return pharmacienMapper.toResponse(pharmacien);
    }

    /**
     * Lister tous les pharmaciens (Admin)
     */
    public List<PharmacienResponse> getAll() {
        log.info("Récupération de tous les pharmaciens");
        return pharmacienRepository.findAll().stream()
                .map(pharmacienMapper::toResponse)
                .toList();
    }

    /**
     * Supprimer un pharmacien (Admin)
     * Détache ses pharmacies avant suppression
     */
    @Transactional
    public void supprimer(UUID id) {
        Pharmacien pharmacien = pharmacienRepository.findById(id)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        // Détacher les pharmacies appartenant à ce pharmacien
        List<Pharmacie> pharmacies = pharmacieRepository.findByPharmacienProprietaireId(id);
        for (Pharmacie pharmacie : pharmacies) {
            pharmacie.setPharmacienProprietaire(null);
            pharmacieRepository.save(pharmacie);
        }

        pharmacienRepository.delete(pharmacien);
        log.info("Pharmacien supprimé: {} {}", pharmacien.getPrenom(), pharmacien.getNom());
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

    // ── Templates email ────────────────────────────────────────────────────────

    private String buildPharmacienWelcomeEmail(String nomComplet, String email, String telephone) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;margin:0;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#10b981,#059669);padding:32px 30px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:26px;letter-spacing:1px;">SunuFarmasi</h1>
                    <p style="color:#d1fae5;margin:8px 0 0;font-size:14px;">La plateforme numérique de la pharmacie au Sénégal</p>
                  </div>
                  <div style="padding:32px 30px;">
                    <h2 style="color:#1f2937;margin-top:0;">Bienvenue, %s !</h2>
                    <p style="color:#374151;line-height:1.7;font-size:15px;">
                      Votre pharmacie a été enregistrée sur <strong>SunuFarmasi</strong> et est désormais
                      visible par les patients qui cherchent une pharmacie ou une pharmacie de garde dans votre zone.
                    </p>
                    <div style="background:#f3f4f6;border-radius:10px;padding:20px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#6b7280;font-size:13px;text-transform:uppercase;font-weight:600;">Vos informations</p>
                      <p style="margin:8px 0 4px;font-size:14px;"><strong>Email :</strong> %s</p>
                      <p style="margin:4px 0 0;font-size:14px;"><strong>Téléphone :</strong> %s</p>
                    </div>
                    <div style="background:#ecfdf5;border-left:4px solid #10b981;padding:16px 20px;margin:20px 0;border-radius:0 10px 10px 0;">
                      <p style="margin:0;color:#065f46;font-size:14px;line-height:1.7;">
                        ✅ Votre pharmacie est <strong>visible sur l'application mobile SunuFarmasi</strong>.<br>
                        📱 Les patients peuvent vous trouver et consulter vos informations et horaires de garde.
                      </p>
                    </div>
                    <div style="background:#fffbeb;border:2px solid #fbbf24;border-radius:10px;padding:18px 20px;margin:20px 0;">
                      <p style="margin:0 0 8px;color:#92400e;font-weight:700;font-size:14px;">🚀 Phase de lancement — Référencement offert</p>
                      <p style="margin:0;color:#78350f;line-height:1.7;font-size:13px;">
                        Nous sommes en phase de lancement. Votre visibilité sur la plateforme est
                        <strong>entièrement gratuite</strong> pendant cette période d'ouverture.
                      </p>
                    </div>
                    <div style="background:#eff6ff;border:1px solid #bfdbfe;border-radius:10px;padding:18px 20px;margin:20px 0;">
                      <p style="margin:0 0 8px;color:#1e40af;font-weight:700;font-size:14px;">📲 Bientôt — Votre espace pharmacien</p>
                      <p style="margin:0 0 10px;color:#1e3a8a;font-size:13px;line-height:1.7;">
                        Un espace dédié aux pharmaciens est en cours de développement. Il vous permettra de gérer
                        votre pharmacie directement depuis votre téléphone ou ordinateur :
                      </p>
                      <ul style="margin:0;padding-left:18px;color:#1e3a8a;font-size:13px;line-height:1.9;">
                        <li>💊 Renseigner la <strong>disponibilité de vos médicaments</strong> en temps réel</li>
                        <li>🛒 Recevoir et gérer des <strong>commandes de patients</strong></li>
                        <li>📦 Proposer la <strong>livraison ou le retrait en pharmacie</strong></li>
                        <li>💬 Communiquer directement avec vos patients</li>
                        <li>📊 Suivre votre activité et vos statistiques</li>
                      </ul>
                      <p style="margin:10px 0 0;color:#1e40af;font-size:13px;font-style:italic;">
                        Vous serez notifié dès que cet espace est disponible.
                      </p>
                    </div>
                    <div style="background:#f0fdf4;border:1px solid #86efac;border-radius:10px;padding:18px 20px;margin:20px 0;">
                      <p style="margin:0 0 10px;color:#14532d;font-weight:700;font-size:14px;">💡 Pourquoi renouveler votre visibilité ?</p>
                      <p style="margin:0 0 10px;color:#166534;font-size:13px;line-height:1.7;">
                        Après la phase de lancement, la visibilité de votre pharmacie sur SunuFarmasi sera maintenue
                        pour seulement <strong>15 000 FCFA / an</strong>.
                      </p>
                      <p style="margin:0 0 6px;color:#166534;font-size:13px;font-weight:600;">Ce que vous obtenez :</p>
                      <ul style="margin:0;padding-left:18px;color:#166534;font-size:13px;line-height:1.9;">
                        <li>📍 Votre pharmacie visible par <strong>tous les patients</strong> de votre zone</li>
                        <li>🌙 Participation automatique aux <strong>plannings de garde</strong> de votre syndicat</li>
                        <li>📞 Vos coordonnées et horaires accessibles 24h/24 sur mobile</li>
                        <li>📈 Plus de visibilité = plus de patients qui vous trouvent</li>
                        <li>🔔 Notifications envoyées aux patients lors de vos gardes</li>
                      </ul>
                      <p style="margin:12px 0 0;color:#14532d;font-size:13px;line-height:1.7;border-top:1px solid #bbf7d0;padding-top:10px;">
                        <strong>15 000 FCFA par an</strong>, c'est le prix d'une ordonnance. Pour votre pharmacie,
                        c'est une vitrine ouverte en permanence sur toute votre commune.
                      </p>
                    </div>
                    <p style="color:#6b7280;font-size:13px;">Des questions ? Contactez votre syndicat ou écrivez-nous à <a href="mailto:sunufarmasi@gmail.com" style="color:#10b981;">sunufarmasi@gmail.com</a></p>
                  </div>
                  <div style="background:#f9fafb;padding:20px 30px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi · Notre Pharmacie, Votre Santé</p>
                  </div>
                </div></body></html>
                """.formatted(nomComplet, email, telephone);
    }
}