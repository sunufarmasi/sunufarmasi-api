package sn.sunufarmasi.pharmacie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.localisation.entity.Commune;
import sn.sunufarmasi.localisation.repository.CommuneRepository;
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

import java.time.LocalDateTime;
import java.util.List;
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
                .email(request.email())
                .siteWeb(request.siteWeb())
                .numeroAgrementMinistere(request.numeroAgrementMinistere())
                .numeroOrdre(request.numeroOrdre())
                .dateOuverture(request.dateOuverture())
                .horaires(request.horaires())
                .pharmacienProprietaire(pharmacien)
                .statut(StatutPharmacie.EN_ATTENTE)
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
     * Générer un code unique pour une pharmacie
     * Format: PHAR-{CODE_COMMUNE}-{NUMERO}
     * Exemple: PHAR-DKR-001
     */
    private String generatePharmacieCode(Commune commune) {
        // Récupérer le code de la commune (3 premiers caractères en majuscules)
        String communeCode = commune.getNom().substring(0, Math.min(3, commune.getNom().length())).toUpperCase();

        // Compter les pharmacies existantes dans cette commune
        long count = pharmacieRepository.findByCommuneId(commune.getId()).size() + 1;

        // Générer le code
        return String.format("PHAR-%s-%03d", communeCode, count);
    }
}