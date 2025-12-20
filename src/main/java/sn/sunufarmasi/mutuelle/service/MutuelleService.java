package sn.sunufarmasi.mutuelle.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.sunufarmasi.mutuelle.dto.request.*;
import sn.sunufarmasi.mutuelle.dto.response.*;
import sn.sunufarmasi.mutuelle.entity.*;
import sn.sunufarmasi.mutuelle.enums.StatutDemande;
import sn.sunufarmasi.mutuelle.mapper.MutuelleMapper;
import sn.sunufarmasi.mutuelle.repository.*;
import sn.sunufarmasi.pharmacie.entity.Pharmacie;
import sn.sunufarmasi.pharmacie.repository.PharmacieRepositorye;
import sn.sunufarmasi.shared.exception.BadRequestException;
import sn.sunufarmasi.shared.exception.ResourceNotFoundException;
import sn.sunufarmasi.vente.entity.Vente;
import sn.sunufarmasi.vente.repository.VenteRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MutuelleService {

    private final MutuelleRepository mutuelleRepository;
    private final ContratMutuelleRepository contratRepository;
    private final AdherentRepository adherentRepository;
    private final DemandeRemboursementRepository demandeRepository;
    private final PharmacieRepositorye pharmacieRepository;
    private final VenteRepository venteRepository;
    private final MutuelleMapper mapper;

    // ═══════════════════════════════════════════════════════════
    // MUTUELLES
    // ═══════════════════════════════════════════════════════════

    public MutuelleResponse createMutuelle(CreateMutuelleRequest req) {
        log.info("Création mutuelle: {}", req.nom());

        String code = generateMutuelleCode();

        Mutuelle m = Mutuelle.builder()
                .code(code).nom(req.nom()).nomCourt(req.nomCourt()).type(req.type())
                .adresse(req.adresse()).ville(req.ville()).telephone(req.telephone())
                .telephoneUrgence(req.telephoneUrgence()).email(req.email()).siteWeb(req.siteWeb())
                .ninea(req.ninea()).numeroAgrement(req.numeroAgrement())
                .contactNom(req.contactNom()).contactTelephone(req.contactTelephone())
                .contactEmail(req.contactEmail()).contactPoste(req.contactPoste())
                .typeCouverture(req.typeCouverture()).tauxCouvertureDefaut(req.tauxCouvertureDefaut())
                .plafondAnnuel(req.plafondAnnuel()).plafondParActe(req.plafondParActe())
                .franchise(req.franchise()).delaiCarenceJours(req.delaiCarenceJours())
                .delaiPaiementJours(req.delaiPaiementJours()).modePaiement(req.modePaiement())
                .jourPaiement(req.jourPaiement())
                .exigeOrdonnance(req.exigeOrdonnance()).exigeCarteAdherent(req.exigeCarteAdherent())
                .exigeFactureDetaillee(req.exigeFactureDetaillee()).delaiSoumissionJours(req.delaiSoumissionJours())
                .couvreMedicaments(req.couvreMedicaments()).couvreGeneriques(req.couvreGeneriques())
                .couvreParamedical(req.couvreParamedical()).couvreCosmetique(req.couvreCosmetique())
                .listeExclusions(req.listeExclusions())
                .notes(req.notes()).logoUrl(req.logoUrl()).estActif(true)
                .build();

        return mapper.toMutuelleResponse(mutuelleRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MutuelleResponse> getAllMutuelles() {
        return mutuelleRepository.findByEstActif(true).stream()
                .map(mapper::toMutuelleResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MutuelleResponse> getMutuellesConventionnees(UUID pharmacieId) {
        return mutuelleRepository.findMutuellesConventionnees(pharmacieId).stream()
                .map(mapper::toMutuelleResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // CONTRATS
    // ═══════════════════════════════════════════════════════════

    public ContratResponse createContrat(UUID pharmacieId, CreateContratRequest req) {
        log.info("Création contrat: pharmacie={}, mutuelle={}", pharmacieId, req.mutuelleId());

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));
        Mutuelle mutuelle = mutuelleRepository.findById(req.mutuelleId())
                .orElseThrow(() -> new ResourceNotFoundException("Mutuelle non trouvée"));

        ContratMutuelle c = ContratMutuelle.builder()
                .pharmacie(pharmacie).mutuelle(mutuelle)
                .numeroContrat(req.numeroContrat()).numeroConventionnement(req.numeroConventionnement())
                .dateDebut(req.dateDebut()).dateFin(req.dateFin())
                .renouvellementAuto(req.renouvellementAuto())
                .tauxCouverture(req.tauxCouverture())
                .plafondMensuel(req.plafondMensuel()).plafondAnnuel(req.plafondAnnuel())
                .delaiPaiementJours(req.delaiPaiementJours())
                .frequenceFacturation(req.frequenceFacturation())
                .jourFacturation(req.jourFacturation()).emailFacturation(req.emailFacturation())
                .tiersPayantAutorise(req.tiersPayantAutorise())
                .montantAvanceMax(req.montantAvanceMax())
                .notes(req.notes()).estActif(true)
                .build();

        return mapper.toContratResponse(contratRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ContratResponse> getContratsActifs(UUID pharmacieId) {
        return contratRepository.findContratsActifs(pharmacieId, LocalDate.now()).stream()
                .map(mapper::toContratResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // ADHÉRENTS
    // ═══════════════════════════════════════════════════════════

    public AdherentResponse createAdherent(CreateAdherentRequest req) {
        log.info("Création adhérent: {} {} - {}", req.prenom(), req.nom(), req.numeroAdherent());

        Mutuelle mutuelle = mutuelleRepository.findById(req.mutuelleId())
                .orElseThrow(() -> new ResourceNotFoundException("Mutuelle non trouvée"));

        Adherent a = Adherent.builder()
                .mutuelle(mutuelle).numeroAdherent(req.numeroAdherent())
                .numeroCarte(req.numeroCarte()).typeBeneficiaire(req.typeBeneficiaire())
                .adherentPrincipalId(req.adherentPrincipalId())
                .nom(req.nom()).prenom(req.prenom()).dateNaissance(req.dateNaissance())
                .sexe(req.sexe()).numeroCni(req.numeroCni())
                .telephone(req.telephone()).email(req.email()).adresse(req.adresse())
                .employeur(req.employeur()).matriculeEmploye(req.matriculeEmploye())
                .tauxCouverture(req.tauxCouverture()).plafondAnnuel(req.plafondAnnuel())
                .dateAdhesion(req.dateAdhesion()).dateFinDroits(req.dateFinDroits())
                .estActif(true)
                .build();

        return mapper.toAdherentResponse(adherentRepository.save(a));
    }

    @Transactional(readOnly = true)
    public AdherentResponse getAdherentByNumero(String numero) {
        Adherent a = adherentRepository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Adhérent non trouvé: " + numero));
        return mapper.toAdherentResponse(a);
    }

    @Transactional(readOnly = true)
    public List<AdherentResponse> searchAdherents(UUID mutuelleId, String query) {
        return adherentRepository.search(mutuelleId, query).stream()
                .map(mapper::toAdherentResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // DEMANDES DE REMBOURSEMENT
    // ═══════════════════════════════════════════════════════════

    public DemandeResponse createDemande(UUID pharmacieId, CreateDemandeRequest req,
                                          UUID userId, String userName) {
        log.info("Création demande remboursement: vente={}", req.venteId());

        Pharmacie pharmacie = pharmacieRepository.findById(pharmacieId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacie non trouvée"));
        Vente vente = venteRepository.findById(req.venteId())
                .orElseThrow(() -> new ResourceNotFoundException("Vente non trouvée"));
        Adherent adherent = adherentRepository.findById(req.adherentId())
                .orElseThrow(() -> new ResourceNotFoundException("Adhérent non trouvé"));

        if (!adherent.droitsOuverts()) {
            throw new BadRequestException("Les droits de l'adhérent ne sont pas ouverts");
        }

        Mutuelle mutuelle = adherent.getMutuelle();

        // Vérifier le contrat
        ContratMutuelle contrat = contratRepository
                .findContratValide(pharmacieId, mutuelle.getId(), LocalDate.now())
                .orElse(null);

        // Calculer le montant
        BigDecimal taux = adherent.getTauxEffectif();
        BigDecimal montantDemande = mutuelle.calculerPriseEnCharge(vente.getMontantNetTTC());

        DemandeRemboursement d = DemandeRemboursement.builder()
                .numero(generateDemandeNumero(pharmacie))
                .pharmacie(pharmacie).mutuelle(mutuelle).adherent(adherent)
                .vente(vente).contrat(contrat)
                .statut(StatutDemande.BROUILLON)
                .dateDemande(LocalDateTime.now())
                .montantVente(vente.getMontantNetTTC())
                .montantDemande(montantDemande)
                .tauxApplique(taux)
                .numeroOrdonnance(req.numeroOrdonnance())
                .medecinPrescripteur(req.medecinPrescripteur())
                .dateOrdonnance(req.dateOrdonnance())
                .notes(req.notes())
                .creeParId(userId).creeParNom(userName)
                .build();

        return mapper.toDemandeResponse(demandeRepository.save(d));
    }

    public DemandeResponse soumettreDemande(UUID demandeId) {
        log.info("Soumission demande {}", demandeId);

        DemandeRemboursement d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        if (!d.getStatut().peutEtreSoumise()) {
            throw new BadRequestException("Cette demande ne peut pas être soumise");
        }

        d.soumettre();
        return mapper.toDemandeResponse(demandeRepository.save(d));
    }

    public DemandeResponse traiterDemande(UUID demandeId, TraiterDemandeRequest req) {
        log.info("Traitement demande {}: {}", demandeId, req.action());

        DemandeRemboursement d = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));

        switch (req.action().toUpperCase()) {
            case "ACCEPTER" -> {
                d.accepter(req.montant(), req.referenceMutuelle());
                d.setObservationsMutuelle(req.observations());
                // Mettre à jour la consommation de l'adhérent
                d.getAdherent().ajouterConsommation(req.montant());
                adherentRepository.save(d.getAdherent());
            }
            case "REJETER" -> d.rejeter(req.motifRejet());
            case "PAYER" -> d.enregistrerPaiement(req.montant(), req.referencePaiement(), req.modePaiement());
            default -> throw new BadRequestException("Action inconnue: " + req.action());
        }

        return mapper.toDemandeResponse(demandeRepository.save(d));
    }

    @Transactional(readOnly = true)
    public Page<DemandeResponse> getDemandesByPharmacie(UUID pharmacieId, Pageable pageable) {
        return demandeRepository.findByPharmacieIdOrderByDateDemandeDesc(pharmacieId, pageable)
                .map(mapper::toDemandeResponse);
    }

    @Transactional(readOnly = true)
    public List<DemandeResponse> getDemandesEnAttente(UUID pharmacieId) {
        return demandeRepository.findEnAttente(pharmacieId).stream()
                .map(mapper::toDemandeResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getCreances(UUID pharmacieId) {
        return demandeRepository.sumCreances(pharmacieId);
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVÉ
    // ═══════════════════════════════════════════════════════════

    private String generateMutuelleCode() {
        return String.format("MUT-%05d", mutuelleRepository.count() + 1);
    }

    private String generateDemandeNumero(Pharmacie pharmacie) {
        LocalDateTime now = LocalDateTime.now();
        Long count = demandeRepository.countDemandesDuJour(pharmacie.getId(), now) + 1;
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("RMB-%s-%s-%04d", pharmacie.getCode(), date, count);
    }
}
