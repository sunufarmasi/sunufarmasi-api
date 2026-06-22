package sn.sunufarmasi.pharmacie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
import sn.sunufarmasi.pharmacie.dto.request.AdminCreatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.request.CreatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.request.UpdatePharmacieRequest;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieDetailResponse;
import sn.sunufarmasi.pharmacie.dto.response.PharmacieResponse;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.entity.Pharmacien;
import sn.sunufarmasi.pharmacie.enums.StatutPharmacie;
import sn.sunufarmasi.pharmacie.exception.BusinessException;
import sn.sunufarmasi.pharmacie.exception.PharmacieAlreadyExistsException;
import sn.sunufarmasi.pharmacie.exception.PharmacieNotFoundException;
import sn.sunufarmasi.pharmacie.exception.PharmacienNotFoundException;
import sn.sunufarmasi.pharmacie.mapper.PharmacieMapper;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.pharmacie.repository.PharmacienRepository;
import sn.sunufarmasi.syndicat.entity.Syndicat;
import sn.sunufarmasi.syndicat.repository.SyndicatRepository;
import sn.sunufarmasi.notification.service.EmailService;
import sn.sunufarmasi.garde.repository.GardeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service métier pour la gestion des pharmacies
 *
 * @author WeCan
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PharmacieService {

    private final PharmacieRepositorye pharmacieRepository;
    private final PharmacienRepository pharmacienRepository;
    private final CommuneRepository communeRepository;
    private final PharmacieMapper pharmacieMapper;
    private final PharmacienService pharmacienService;
    private final SyndicatRepository syndicatRepository;
    private final EmailService emailService;
    private final GardeRepository gardeRepository;

    /**
     * Créer une nouvelle pharmacie
     * La pharmacie est automatiquement liée au pharmacien connecté
     */
    @Transactional
    public PharmacieResponse create(CreatePharmacieRequest request, UUID pharmacienId) {
        log.info("Création d'une pharmacie par le pharmacien: {}", pharmacienId);

        // Vérifier que le pharmacien existe
        Pharmacien pharmacien = pharmacienRepository.findById(pharmacienId)
                .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));

        // Vérifier que le pharmacien peut créer une pharmacie
        if (!pharmacien.peutCreerPharmacie()) {
            throw new BusinessException("Vous avez atteint la limite de pharmacies pour votre plan. Passez au plan ENTERPRISE.");
        }

        // Vérifier que l'abonnement est actif
        if (!pharmacien.abonnementActif()) {
            throw new BusinessException("Votre abonnement a expiré. Veuillez le renouveler.");
        }

        // Vérifier que le téléphone n'existe pas déjà
        if (pharmacieRepository.existsByTelephone(request.telephone())) {
            throw new PharmacieAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
        }

        // Vérifier que le numéro d'ordre n'existe pas déjà
        if (pharmacieRepository.existsByNumeroOrdre(request.numeroOrdre())) {
            throw new PharmacieAlreadyExistsException("Ce numéro d'ordre est déjà utilisé");
        }

        // Récupérer la commune
        Commune commune = communeRepository.findById(request.communeId())
                .orElseThrow(() -> new BusinessException("Commune non trouvée"));

        // Générer le code unique de la pharmacie
        String code = generatePharmacieCode(commune);

        // Créer la pharmacie
        Pharmacie pharmacie = Pharmacie.builder()
                .code(code)
                .nom(request.nom())
                .raisonSociale(request.raisonSociale())
                .commune(commune)
                .adresseComplete(request.adresseComplete())
                .quartier(request.quartier())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .telephone(request.telephone())
                .telephoneSecondaire(request.telephoneSecondaire())
                .email(request.email() != null && !request.email().isBlank() ? request.email() : null)
                .siteWeb(request.siteWeb())
                .numeroAgrementMinistere(request.numeroAgrementMinistere())
                .numeroOrdre(request.numeroOrdre())
                .dateOuverture(request.dateOuverture())
                .horaires(request.horaires())
                .pharmacienProprietaire(pharmacien)
                .statut(StatutPharmacie.EN_ATTENTE)
                .dateDernierPaiement(LocalDateTime.now())  // inscription = premier contact
                .accepteCommandes(request.accepteCommandes())
                .proposeLivraison(request.proposeLivraison())
                .rayonLivraisonKm(request.rayonLivraisonKm())
                .build();

        // Générer le lien Google Maps
        pharmacie.setCoordonnees(request.latitude(), request.longitude());

        pharmacie = pharmacieRepository.save(pharmacie);

        // Incrémenter le compteur de pharmacies du pharmacien
        pharmacienService.incrementPharmaciesCount(pharmacienId);

        log.info("Pharmacie créée avec succès: {} - Code: {}", pharmacie.getNom(), pharmacie.getCode());

        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Modifier une pharmacie (Admin — sans vérification propriétaire)
     */
    @Transactional
    public PharmacieResponse adminUpdate(UUID pharmacieId, UpdatePharmacieRequest request) {
        log.info("Admin - Modification de la pharmacie: {}", pharmacieId);
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        if (request.nom() != null) pharmacie.setNom(request.nom());
        if (request.adresseComplete() != null) pharmacie.setAdresseComplete(request.adresseComplete());
        if (request.quartier() != null) pharmacie.setQuartier(request.quartier());
        if (request.latitude() != null && request.longitude() != null)
            pharmacie.setCoordonnees(request.latitude(), request.longitude());
        if (request.telephone() != null) {
            if (!request.telephone().equals(pharmacie.getTelephone())
                    && pharmacieRepository.existsByTelephone(request.telephone())) {
                throw new PharmacieAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
            }
            pharmacie.setTelephone(request.telephone());
        }
        if (request.telephoneSecondaire() != null) pharmacie.setTelephoneSecondaire(request.telephoneSecondaire());
        if (request.email() != null) pharmacie.setEmail(request.email().isBlank() ? null : request.email());
        if (request.siteWeb() != null) pharmacie.setSiteWeb(request.siteWeb());
        if (request.horaires() != null) pharmacie.setHoraires(request.horaires());
        if (request.accepteCommandes() != null) pharmacie.setAccepteCommandes(request.accepteCommandes());
        if (request.proposeLivraison() != null) pharmacie.setProposeLivraison(request.proposeLivraison());
        if (request.rayonLivraisonKm() != null) pharmacie.setRayonLivraisonKm(request.rayonLivraisonKm());
        if (request.description() != null) pharmacie.setDescription(request.description());
        if (request.services() != null) pharmacie.setServices(request.services());
        if (request.pharmacienId() != null) {
            Pharmacien pharmacien = pharmacienRepository.findById(request.pharmacienId())
                    .orElseThrow(() -> new RuntimeException("Pharmacien non trouvé"));
            pharmacie.setPharmacienProprietaire(pharmacien);
        }

        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Pharmacie modifiée (admin) avec succès: {}", pharmacie.getCode());
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Modifier une pharmacie
     */
    @Transactional
    public PharmacieResponse update(UUID pharmacieId, UpdatePharmacieRequest request, UUID pharmacienId) {
        log.info("Modification de la pharmacie: {} par le pharmacien: {}", pharmacieId, pharmacienId);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        // Vérifier que c'est bien le propriétaire
        if (!pharmacie.getPharmacienProprietaire().getId().equals(pharmacienId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à modifier cette pharmacie");
        }

        // Mettre à jour les champs non nulls
        if (request.nom() != null) {
            pharmacie.setNom(request.nom());
        }
        if (request.adresseComplete() != null) {
            pharmacie.setAdresseComplete(request.adresseComplete());
        }
        if (request.quartier() != null) {
            pharmacie.setQuartier(request.quartier());
        }
        if (request.latitude() != null && request.longitude() != null) {
            pharmacie.setCoordonnees(request.latitude(), request.longitude());
        }
        if (request.telephone() != null) {
            // Vérifier que le téléphone n'existe pas déjà pour une autre pharmacie
            if (!pharmacie.getTelephone().equals(request.telephone())
                    && pharmacieRepository.existsByTelephone(request.telephone())) {
                throw new PharmacieAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
            }
            pharmacie.setTelephone(request.telephone());
        }
        if (request.telephoneSecondaire() != null) {
            pharmacie.setTelephoneSecondaire(request.telephoneSecondaire());
        }
        if (request.email() != null) {
            pharmacie.setEmail(request.email());
        }
        if (request.siteWeb() != null) {
            pharmacie.setSiteWeb(request.siteWeb());
        }
        if (request.horaires() != null) {
            pharmacie.setHoraires(request.horaires());
        }
        if (request.accepteCommandes() != null) {
            pharmacie.setAccepteCommandes(request.accepteCommandes());
        }
        if (request.proposeLivraison() != null) {
            pharmacie.setProposeLivraison(request.proposeLivraison());
        }
        if (request.rayonLivraisonKm() != null) {
            pharmacie.setRayonLivraisonKm(request.rayonLivraisonKm());
        }

        pharmacie = pharmacieRepository.save(pharmacie);

        log.info("Pharmacie modifiée avec succès: {}", pharmacie.getCode());

        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Récupérer une pharmacie par ID
     */
    public PharmacieDetailResponse getById(UUID pharmacieId) {
        log.info("Récupération de la pharmacie: {}", pharmacieId);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        return pharmacieMapper.toDetailResponse(pharmacie);
    }

    /**
     * Récupérer une pharmacie par code
     */
    public PharmacieDetailResponse getByCode(String code) {
        log.info("Récupération de la pharmacie par code: {}", code);

        Pharmacie pharmacie = pharmacieRepository.findByCode(code)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        return pharmacieMapper.toDetailResponse(pharmacie);
    }

    /**
     * Récupérer toutes les pharmacies d'un pharmacien
     */
    public List<PharmacieResponse> getByPharmacien(UUID pharmacienId) {
        log.info("Récupération des pharmacies du pharmacien: {}", pharmacienId);

        List<Pharmacie> pharmacies = pharmacieRepository.findByPharmacienProprietaireId(pharmacienId);

        return pharmacies.stream()
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les pharmacies d'une commune
     */
    public List<PharmacieResponse> getByCommune(UUID communeId) {
        log.info("Récupération des pharmacies de la commune: {}", communeId);

        List<Pharmacie> pharmacies = pharmacieRepository.findByCommuneId(communeId);

        return pharmacies.stream()
                .filter(p -> p.getStatut() == StatutPharmacie.VALIDEE)
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les pharmacies d'un syndicat
     */
    public List<PharmacieResponse> getBySyndicat(UUID syndicatId) {
        log.info("Récupération des pharmacies du syndicat: {}", syndicatId);

        List<Pharmacie> pharmacies = pharmacieRepository.findBySyndicatId(syndicatId);

        return pharmacies.stream()
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des pharmacies proches d'un point GPS
     */
    public List<PharmacieResponse> findNearby(double latitude, double longitude, double radiusKm) {
        log.info("Recherche de pharmacies proches de ({}, {}) dans un rayon de {} km",
                latitude, longitude, radiusKm);

        List<Pharmacie> pharmacies = pharmacieRepository.findNearby(latitude, longitude, radiusKm);

        return pharmacies.stream()
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Rechercher des pharmacies par nom
     */
    public List<PharmacieResponse> searchByNom(String nom) {
        log.info("Recherche de pharmacies par nom: {}", nom);

        List<Pharmacie> pharmacies = pharmacieRepository.findByNomContainingIgnoreCase(nom);

        return pharmacies.stream()
                .filter(p -> p.getStatut() == StatutPharmacie.VALIDEE)
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Créer une pharmacie (Admin) — sans vérifications d'abonnement, avec syndicat
     */
    @Transactional
    public PharmacieResponse adminCreate(AdminCreatePharmacieRequest request) {
        log.info("Création admin d'une pharmacie: {}", request.nom());

        Commune commune = communeRepository.findById(request.communeId())
                .orElseThrow(() -> new BusinessException("Commune non trouvée"));

        // Vérifier unicité téléphone
        if (pharmacieRepository.existsByTelephone(request.telephone())) {
            throw new PharmacieAlreadyExistsException("Ce numéro de téléphone est déjà utilisé");
        }

        // Choisir ou trouver le pharmacien (optionnel)
        Pharmacien pharmacien = null;
        if (request.pharmacienId() != null) {
            pharmacien = pharmacienRepository.findById(request.pharmacienId())
                    .orElseThrow(() -> new PharmacienNotFoundException("Pharmacien non trouvé"));
        }

        // Trouver le syndicat si précisé
        Syndicat syndicat = null;
        if (request.syndicatId() != null) {
            syndicat = syndicatRepository.findById(request.syndicatId()).orElse(null);
        }

        // Générer le code
        String code = generatePharmacieCode(commune);

        // Gérer les valeurs par défaut (null si vide — PostgreSQL accepte plusieurs null sur unique)
        String rawOrdre = request.numeroOrdre();
        String numeroOrdre = (rawOrdre != null && !rawOrdre.isBlank()) ? rawOrdre : null;

        String rawAgrement = request.numeroAgrementMinistere();
        String numeroAgrement = (rawAgrement != null && !rawAgrement.isBlank()) ? rawAgrement : null;

        Pharmacie pharmacie = Pharmacie.builder()
                .code(code)
                .nom(request.nom())
                .commune(commune)
                .adresseComplete(request.adresseComplete() != null ? request.adresseComplete() : commune.getNom())
                .quartier(request.quartier())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .telephone(request.telephone())
                .email(request.email() != null && !request.email().isBlank() ? request.email() : null)
                .siteWeb(request.siteWeb())
                .numeroAgrementMinistere(numeroAgrement)
                .numeroOrdre(numeroOrdre)
                .dateOuverture(request.dateOuverture() != null ? request.dateOuverture() : LocalDate.now())
                .horaires(request.horaires())
                .pharmacienProprietaire(pharmacien)
                .syndicat(syndicat)
                .statut(StatutPharmacie.ACTIVE)
                .dateValidation(LocalDateTime.now())
                .valideParNom("Super Admin")
                .dateDernierPaiement(LocalDateTime.now())  // inscription = premier contact
                .accepteCommandes(request.accepteCommandes() != null ? request.accepteCommandes() : true)
                .proposeLivraison(request.proposeLivraison() != null ? request.proposeLivraison() : false)
                .rayonLivraisonKm(request.rayonLivraisonKm())
                .build();

        if (request.latitude() != null && request.longitude() != null) {
            pharmacie.setCoordonnees(request.latitude(), request.longitude());
        }

        pharmacie = pharmacieRepository.save(pharmacie);

        // Incrémenter compteur pharmacien (optionnel)
        if (pharmacien != null) {
            pharmacien.ajouterPharmacie();
            pharmacienRepository.save(pharmacien);
        }

        // Incrémenter compteur syndicat
        if (syndicat != null) {
            syndicat.setNombrePharmaciesActuelles(syndicat.getNombrePharmaciesActuelles() + 1);
            syndicatRepository.save(syndicat);
        }

        log.info("Pharmacie admin créée: {} - Code: {}", pharmacie.getNom(), pharmacie.getCode());

        // Emails envoyés UNIQUEMENT après commit réussi (évite envoi si transaction rollback)
        final String pharmacienEmail = pharmacien != null ? pharmacien.getEmail() : null;
        final String pharmacieEmail = pharmacie.getEmail();
        final String nomComplet = pharmacien != null ? pharmacien.getPrenom() + " " + pharmacien.getNom() : "—";
        final String nomPharmacie = pharmacie.getNom();
        final String codePharm = pharmacie.getCode();
        final String nomCommune = commune.getNom();
        final String nomSyndicat = syndicat != null ? syndicat.getNom() : "—";

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (pharmacienEmail != null && !pharmacienEmail.isBlank()) {
                    String html = buildPharmacieCreeeEmail(nomComplet, nomPharmacie, codePharm, nomCommune, nomSyndicat);
                    emailService.sendEmail(pharmacienEmail, "Votre pharmacie a été enregistrée sur SunuFarmasi", html);
                }
                if (pharmacieEmail != null && !pharmacieEmail.isBlank()
                        && !pharmacieEmail.equals(pharmacienEmail)) {
                    String html = buildPharmacieCreeeEmail(nomPharmacie, nomPharmacie, codePharm, nomCommune, nomSyndicat);
                    emailService.sendEmail(pharmacieEmail, "Votre pharmacie a été enregistrée sur SunuFarmasi", html);
                }
            }
        });

        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Assigner une pharmacie à un syndicat (Admin)
     */
    @Transactional
    public PharmacieResponse adminAssignerSyndicat(UUID pharmacieId, UUID syndicatId) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));
        Syndicat syndicat = syndicatRepository.findById(syndicatId)
                .orElseThrow(() -> new BusinessException("Syndicat non trouvé"));

        // Décrémenter l'ancien syndicat si présent
        if (pharmacie.getSyndicat() != null && !pharmacie.getSyndicat().getId().equals(syndicatId)) {
            Syndicat ancien = pharmacie.getSyndicat();
            ancien.setNombrePharmaciesActuelles(Math.max(0, ancien.getNombrePharmaciesActuelles() - 1));
            syndicatRepository.save(ancien);
        }

        pharmacie.setSyndicat(syndicat);
        pharmacie = pharmacieRepository.save(pharmacie);

        syndicat.setNombrePharmaciesActuelles(syndicat.getNombrePharmaciesActuelles() + 1);
        syndicatRepository.save(syndicat);

        log.info("Pharmacie {} assignée au syndicat {}", pharmacie.getCode(), syndicat.getCode());
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Récupérer toutes les pharmacies (Admin)
     */
    public List<PharmacieResponse> getAll() {
        log.info("Récupération de toutes les pharmacies");
        return pharmacieRepository.findAll().stream()
                .map(pharmacieMapper::toResponse)
                .toList();
    }

    /**
     * Récupérer les pharmacies par statut
     */
    public List<PharmacieResponse> getByStatut(StatutPharmacie statut) {
        log.info("Récupération des pharmacies avec le statut: {}", statut);

        List<Pharmacie> pharmacies = pharmacieRepository.findByStatut(statut);

        return pharmacies.stream()
                .map(pharmacieMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Supprimer une pharmacie
     */
    /**
     * Supprimer une pharmacie (Admin — sans vérification propriétaire)
     */
    @Transactional
    public void adminDelete(UUID pharmacieId) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        // Supprimer les gardes liées (cascade manuelle)
        gardeRepository.deleteByPharmacieId(pharmacieId);

        // Décrémenter le compteur du pharmacien propriétaire si présent
        if (pharmacie.getPharmacienProprietaire() != null) {
            pharmacienService.decrementPharmaciesCount(pharmacie.getPharmacienProprietaire().getId());
        }

        // Décrémenter le compteur du syndicat si présent
        if (pharmacie.getSyndicat() != null) {
            Syndicat syndicat = pharmacie.getSyndicat();
            syndicat.setNombrePharmaciesActuelles(Math.max(0, syndicat.getNombrePharmaciesActuelles() - 1));
            syndicatRepository.save(syndicat);
        }

        pharmacieRepository.delete(pharmacie);
        log.info("Pharmacie supprimée par admin: {}", pharmacie.getCode());
    }

    @Transactional
    public void delete(UUID pharmacieId, UUID pharmacienId) {
        log.info("Suppression de la pharmacie: {} par le pharmacien: {}", pharmacieId, pharmacienId);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        // Vérifier que c'est bien le propriétaire
        if (!pharmacie.getPharmacienProprietaire().getId().equals(pharmacienId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à supprimer cette pharmacie");
        }

        pharmacieRepository.delete(pharmacie);

        // Décrémenter le compteur de pharmacies du pharmacien
        pharmacienService.decrementPharmaciesCount(pharmacienId);

        log.info("Pharmacie supprimée avec succès: {}", pharmacie.getCode());
    }

    /**
     * Valider une pharmacie (par syndicat ou admin)
     */
    @Transactional
    public PharmacieResponse valider(UUID pharmacieId, String validePar) {
        log.info("Validation de la pharmacie: {} par: {}", pharmacieId, validePar);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        if (pharmacie.getStatut() != StatutPharmacie.EN_ATTENTE) {
            throw new BusinessException("Seules les pharmacies en attente peuvent être validées");
        }

        pharmacie.setStatut(StatutPharmacie.VALIDEE);
        pharmacie.setDateValidation(LocalDateTime.now());
        pharmacie.setValideParNom(validePar);

        pharmacie = pharmacieRepository.save(pharmacie);

        log.info("Pharmacie validée avec succès: {}", pharmacie.getCode());

        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Rejeter une pharmacie
     */
    @Transactional
    public PharmacieResponse rejeter(UUID pharmacieId, String motif) {
        log.info("Rejet de la pharmacie: {} - Motif: {}", pharmacieId, motif);

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        if (pharmacie.getStatut() != StatutPharmacie.EN_ATTENTE) {
            throw new BusinessException("Seules les pharmacies en attente peuvent être rejetées");
        }

        pharmacie.setStatut(StatutPharmacie.REJETEE);
        pharmacie.setMotifRejet(motif);

        pharmacie = pharmacieRepository.save(pharmacie);

        log.info("Pharmacie rejetée: {}", pharmacie.getCode());

        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Activer une pharmacie (VALIDEE ou SUSPENDUE → ACTIVE)
     */
    @Transactional
    public PharmacieResponse activer(UUID pharmacieId) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));
        pharmacie.setStatut(StatutPharmacie.ACTIVE);
        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Pharmacie activée: {}", pharmacie.getCode());
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Suspendre une pharmacie pour non-paiement (syndicat ou admin)
     */
    @Transactional
    public PharmacieResponse suspendreForNonPaiement(UUID pharmacieId, String motif) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));
        if (pharmacie.getStatut() == StatutPharmacie.FERMEE) {
            throw new BusinessException("Impossible de suspendre une pharmacie fermée");
        }
        pharmacie.setStatut(StatutPharmacie.SUSPENDUE);
        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Pharmacie suspendue (non-paiement): {} - Motif: {}", pharmacie.getCode(), motif);
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Renouveler l'abonnement d'une pharmacie (Admin)
     * POST /api/v1/pharmacies/{id}/renouveler-abonnement?mois=12
     */
    @Transactional
    public PharmacieResponse renouvelerAbonnement(UUID pharmacieId, int mois, String reference) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        LocalDateTime base = (pharmacie.getDateFinAbonnement() != null && pharmacie.getDateFinAbonnement().isAfter(LocalDateTime.now()))
                ? pharmacie.getDateFinAbonnement()
                : LocalDateTime.now();

        pharmacie.setDateDernierPaiement(LocalDateTime.now());
        pharmacie.setDateFinAbonnement(base.plusMonths(mois));
        if (reference != null && !reference.isBlank()) {
            pharmacie.setReferencePaiement(reference);
        }

        // Si suspendue, on la réactive
        if (pharmacie.getStatut() == StatutPharmacie.SUSPENDUE) {
            pharmacie.setStatut(StatutPharmacie.ACTIVE);
        }

        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Abonnement renouvelé pour pharmacie {} — expire le {}", pharmacie.getCode(), pharmacie.getDateFinAbonnement());

        if (pharmacie.getEmail() != null) {
            try {
                emailService.notifierConfirmationPaiementPharmacie(
                        pharmacie.getEmail(),
                        pharmacie.getNom(),
                        pharmacie.getTelephone() != null ? pharmacie.getTelephone() : "—",
                        pharmacie.getDateFinAbonnement().toLocalDate().toString(),
                        "5 000"
                );
            } catch (Exception ignored) {}
        }
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Envoyer un e-mail de rappel de renouvellement (Admin)
     * POST /api/v1/pharmacies/{id}/relancer-rappel
     */
    @Transactional
    public void relancerRappel(UUID pharmacieId) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        String dest = pharmacie.getEmail();
        if (dest == null || dest.isBlank()) {
            log.warn("Aucun email pour la pharmacie {}, rappel non envoyé", pharmacie.getCode());
            return;
        }

        String expiration = pharmacie.getDateFinAbonnement() != null
                ? pharmacie.getDateFinAbonnement().toLocalDate().toString()
                : "expirée";

        emailService.notifierRappelAbonnementPharmacie(
                dest,
                pharmacie.getNom(),
                pharmacie.getTelephone() != null ? pharmacie.getTelephone() : "—",
                expiration,
                "5 000"
        );

        log.info("Rappel de renouvellement envoyé à {} ({})", dest, pharmacie.getCode());
    }

    /**
     * Enregistrer un paiement pour une pharmacie (Admin)
     * Passe EN_ATTENTE → ACTIVE, met à jour dateDernierPaiement et dateFinAbonnement
     * POST /api/v1/pharmacies/{id}/enregistrer-paiement
     */
    @Transactional
    public PharmacieResponse enregistrerPaiement(UUID pharmacieId, String reference) {
        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new PharmacieNotFoundException("Pharmacie non trouvée"));

        LocalDateTime now = LocalDateTime.now();
        pharmacie.setDateDernierPaiement(now);
        pharmacie.setDateFinAbonnement(now.plusYears(1));
        if (reference != null && !reference.isBlank()) {
            pharmacie.setReferencePaiement(reference);
        }

        // Passe EN_ATTENTE ou VALIDEE → ACTIVE
        if (pharmacie.getStatut() == StatutPharmacie.EN_ATTENTE
                || pharmacie.getStatut() == StatutPharmacie.VALIDEE
                || pharmacie.getStatut() == StatutPharmacie.SUSPENDUE) {
            pharmacie.setStatut(StatutPharmacie.ACTIVE);
        }

        pharmacie = pharmacieRepository.save(pharmacie);
        log.info("Paiement enregistré pharmacie {} — expire le {}", pharmacie.getCode(), pharmacie.getDateFinAbonnement());

        if (pharmacie.getEmail() != null) {
            try {
                emailService.notifierConfirmationPaiementPharmacie(
                        pharmacie.getEmail(),
                        pharmacie.getNom(),
                        pharmacie.getTelephone() != null ? pharmacie.getTelephone() : "—",
                        pharmacie.getDateFinAbonnement().toLocalDate().toString(),
                        "5 000"
                );
            } catch (Exception ignored) {}
        }
        return pharmacieMapper.toResponse(pharmacie);
    }

    /**
     * Générer un code unique pour une pharmacie
     * Format: PHAR-{CODE_COMMUNE}-{NUMERO}
     * Exemple: PHAR-DKR-001
     */
    private String generatePharmacieCode(Commune commune) {
        // Récupérer le code de la commune (3 premiers caractères en majuscules)
        String communeCode = commune.getNom().substring(0, Math.min(3, commune.getNom().length())).toUpperCase();
        String prefix = "PHAR-" + communeCode + "-";

        // Trouver le numéro max existant pour ce préfixe pour éviter les doublons
        long maxNum = pharmacieRepository.findAll().stream()
                .map(Pharmacie::getCode)
                .filter(c -> c != null && c.startsWith(prefix))
                .mapToLong(c -> {
                    try { return Long.parseLong(c.substring(prefix.length())); }
                    catch (NumberFormatException e) { return 0L; }
                })
                .max().orElse(0L);

        return String.format("PHAR-%s-%03d", communeCode, maxNum + 1);
    }

    // ── Templates email ────────────────────────────────────────────────────────

    private String buildPharmacieCreeeEmail(String destinataire, String nomPharmacie,
                                             String code, String commune, String syndicat) {
        return """
                <html><body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:20px;margin:0;">
                <div style="max-width:600px;margin:0 auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);">
                  <div style="background:linear-gradient(135deg,#10b981,#059669);padding:32px 30px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:26px;letter-spacing:1px;">SunuFarmasi</h1>
                    <p style="color:#d1fae5;margin:8px 0 0;font-size:14px;">La pharmacie de garde, toujours à portée de main</p>
                  </div>
                  <div style="padding:32px 30px;">
                    <h2 style="color:#1f2937;margin-top:0;">Bonjour %s, votre pharmacie est en ligne ! 🎉</h2>
                    <p style="color:#374151;line-height:1.7;font-size:15px;">
                      <strong>%s</strong> est maintenant enregistrée sur <strong>SunuFarmasi</strong>
                      et visible par des milliers de patients qui cherchent une pharmacie de garde près de chez eux.
                    </p>
                    <div style="background:#f3f4f6;border-radius:10px;padding:20px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#6b7280;font-size:13px;text-transform:uppercase;font-weight:600;">Récapitulatif</p>
                      <p style="margin:8px 0 4px;font-size:14px;"><strong>Pharmacie :</strong> %s</p>
                      <p style="margin:4px 0;font-size:14px;"><strong>Code :</strong> <code style="background:#e5e7eb;padding:2px 8px;border-radius:4px;">%s</code></p>
                      <p style="margin:4px 0;font-size:14px;"><strong>Commune :</strong> %s</p>
                      <p style="margin:4px 0 0;font-size:14px;"><strong>Syndicat :</strong> %s</p>
                    </div>
                    <div style="background:#ecfdf5;border-left:4px solid #10b981;padding:16px 20px;margin:20px 0;border-radius:0 10px 10px 0;">
                      <p style="margin:0;color:#065f46;font-size:14px;line-height:1.7;">
                        ✅ Votre pharmacie est <strong>active et visible</strong> sur l'application mobile.<br>
                        📅 Votre syndicat peut maintenant vous inclure dans les plannings de garde.
                      </p>
                    </div>
                    <div style="background:#fffbeb;border:2px solid #fbbf24;border-radius:10px;padding:18px 20px;margin:20px 0;">
                      <p style="margin:0 0 6px;color:#92400e;font-weight:700;font-size:14px;">🚧 Phase de lancement — Accès offert</p>
                      <p style="margin:0;color:#78350f;line-height:1.7;font-size:13px;">
                        Vous faites partie des premières pharmacies sur la plateforme.
                        Votre présence est <strong>gratuite pendant cette phase</strong>. À la mise en production,
                        un abonnement annuel très accessible sera proposé pour maintenir votre visibilité
                        et votre participation aux gardes. Votre syndicat vous informera en temps voulu.
                      </p>
                    </div>
                    <p style="color:#6b7280;font-size:13px;">Des questions ? <a href="mailto:sunufarmasi@gmail.com" style="color:#10b981;">sunufarmasi@gmail.com</a></p>
                  </div>
                  <div style="background:#f9fafb;padding:20px 30px;text-align:center;border-top:1px solid #e5e7eb;">
                    <p style="color:#9ca3af;font-size:12px;margin:0;">© 2026 SunuFarmasi · Notre Pharmacie, Votre Santé</p>
                  </div>
                </div></body></html>
                """.formatted(destinataire, nomPharmacie, nomPharmacie, code, commune, syndicat);
    }
}