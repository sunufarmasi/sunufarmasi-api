package sn.sunufarmasi.mutuelle.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sn.sunufarmasi.mutuelle.dto.response.*;
import sn.sunufarmasi.mutuelle.entity.*;

@Component
@RequiredArgsConstructor
public class MutuelleMapper {

    public MutuelleResponse toMutuelleResponse(Mutuelle m) {
        if (m == null) return null;
        return new MutuelleResponse(
                m.getId(), m.getCode(), m.getNom(), m.getNomCourt(), m.getNomAffiche(),
                m.getType(), m.getType() != null ? m.getType().getLibelle() : null,
                m.getAdresse(), m.getVille(), m.getTelephone(), m.getEmail(),
                m.getContactNom(), m.getContactTelephone(),
                m.getTypeCouverture(), m.getTauxCouvertureDefaut(),
                m.getPlafondAnnuel(), m.getFranchise(),
                m.getDelaiPaiementJours(),
                m.getExigeOrdonnance(), m.getExigeCarteAdherent(),
                m.getCouvreMedicaments(), m.getCouvreGeneriques(), m.getCouvreParamedical(),
                m.getEstActif(), m.getLogoUrl()
        );
    }

    public ContratResponse toContratResponse(ContratMutuelle c) {
        if (c == null) return null;
        Mutuelle m = c.getMutuelle();
        return new ContratResponse(
                c.getId(),
                m != null ? m.getId() : null,
                m != null ? m.getNomAffiche() : null,
                m != null ? m.getCode() : null,
                c.getNumeroContrat(),
                c.getNumeroConventionnement(),
                c.getDateDebut(), c.getDateFin(),
                c.getTauxCouverture(), c.getTauxEffectif(),
                c.getPlafondMensuel(), c.getPlafondAnnuel(),
                c.getTiersPayantAutorise(), c.getMontantAvanceMax(),
                c.getEstActif(), c.estValide()
        );
    }

    public AdherentResponse toAdherentResponse(Adherent a) {
        if (a == null) return null;
        Mutuelle m = a.getMutuelle();
        return new AdherentResponse(
                a.getId(),
                m != null ? m.getId() : null,
                m != null ? m.getNomAffiche() : null,
                a.getNumeroAdherent(), a.getNumeroCarte(), a.getTypeBeneficiaire(),
                a.getNom(), a.getPrenom(), a.getNomComplet(),
                a.getDateNaissance(), a.getSexe(),
                a.getTelephone(), a.getEmail(),
                a.getEmployeur(), a.getMatriculeEmploye(),
                a.getTauxCouverture(), a.getTauxEffectif(),
                a.getPlafondAnnuel(), a.getConsommationAnnuelle(), a.getResteAConsommer(),
                a.getDateAdhesion(), a.getDateFinDroits(),
                a.getEstActif(), a.droitsOuverts(), a.plafondAtteint()
        );
    }

    public DemandeResponse toDemandeResponse(DemandeRemboursement d) {
        if (d == null) return null;
        Mutuelle m = d.getMutuelle();
        Adherent a = d.getAdherent();
        return new DemandeResponse(
                d.getId(), d.getNumero(),
                m != null ? m.getId() : null, m != null ? m.getNomAffiche() : null,
                a != null ? a.getId() : null, a != null ? a.getNomComplet() : null,
                a != null ? a.getNumeroAdherent() : null,
                d.getVente() != null ? d.getVente().getId() : null,
                d.getVente() != null ? d.getVente().getNumero() : null,
                d.getStatut(), d.getStatut() != null ? d.getStatut().getLibelle() : null,
                d.getDateDemande(), d.getDateSoumission(), d.getDateReponse(), d.getDatePaiement(),
                d.getMontantVente(), d.getMontantDemande(), d.getTauxApplique(),
                d.getMontantAccepte(), d.getMontantRejete(), d.getMontantPaye(), d.getResteAPayer(),
                d.getNumeroOrdonnance(), d.getMedecinPrescripteur(),
                d.getReferencePaiement(), d.getModePaiement(),
                d.getReferenceMutuelle(), d.getMotifRejet(),
                d.estPayee()
        );
    }
}
