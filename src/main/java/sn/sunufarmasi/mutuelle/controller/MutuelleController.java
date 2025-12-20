package sn.sunufarmasi.mutuelle.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sn.sunufarmasi.mutuelle.dto.request.*;
import sn.sunufarmasi.mutuelle.dto.response.*;
import sn.sunufarmasi.mutuelle.enums.*;
import sn.sunufarmasi.mutuelle.service.MutuelleService;
import sn.sunufarmasi.shared.dto.ApiResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MutuelleController {

    private final MutuelleService mutuelleService;

    @PostMapping("/mutuelles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MutuelleResponse>> createMutuelle(
            @Valid @RequestBody CreateMutuelleRequest request) {
        MutuelleResponse response = mutuelleService.createMutuelle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mutuelle creee", response));
    }

    @GetMapping("/mutuelles")
    public ResponseEntity<List<MutuelleResponse>> getAllMutuelles() {
        return ResponseEntity.ok(mutuelleService.getAllMutuelles());
    }

    @GetMapping("/pharmacies/{pharmacieId}/mutuelles/conventionnees")
    public ResponseEntity<List<MutuelleResponse>> getMutuellesConventionnees(@PathVariable UUID pharmacieId) {
        return ResponseEntity.ok(mutuelleService.getMutuellesConventionnees(pharmacieId));
    }

    @PostMapping("/pharmacies/{pharmacieId}/contrats-mutuelle")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<ContratResponse>> createContrat(
            @PathVariable UUID pharmacieId, @Valid @RequestBody CreateContratRequest request) {
        ContratResponse response = mutuelleService.createContrat(pharmacieId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Contrat cree", response));
    }

    @GetMapping("/pharmacies/{pharmacieId}/contrats-mutuelle")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<List<ContratResponse>> getContratsActifs(@PathVariable UUID pharmacieId) {
        return ResponseEntity.ok(mutuelleService.getContratsActifs(pharmacieId));
    }

    @PostMapping("/adherents")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIEN')")
    public ResponseEntity<ApiResponse<AdherentResponse>> createAdherent(@Valid @RequestBody CreateAdherentRequest request) {
        AdherentResponse response = mutuelleService.createAdherent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Adherent cree", response));
    }

    @GetMapping("/adherents/numero/{numero}")
    public ResponseEntity<AdherentResponse> getAdherentByNumero(@PathVariable String numero) {
        return ResponseEntity.ok(mutuelleService.getAdherentByNumero(numero));
    }

    @GetMapping("/mutuelles/{mutuelleId}/adherents/search")
    public ResponseEntity<List<AdherentResponse>> searchAdherents(@PathVariable UUID mutuelleId, @RequestParam String q) {
        return ResponseEntity.ok(mutuelleService.searchAdherents(mutuelleId, q));
    }

    @PostMapping("/pharmacies/{pharmacieId}/demandes-remboursement")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<DemandeResponse>> createDemande(
            @PathVariable UUID pharmacieId, @Valid @RequestBody CreateDemandeRequest request,
            @RequestHeader("X-User-Id") UUID userId, @RequestHeader("X-User-Name") String userName) {
        DemandeResponse response = mutuelleService.createDemande(pharmacieId, request, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Demande creee", response));
    }

    @PostMapping("/demandes-remboursement/{demandeId}/soumettre")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<DemandeResponse>> soumettreDemande(@PathVariable UUID demandeId) {
        DemandeResponse response = mutuelleService.soumettreDemande(demandeId);
        return ResponseEntity.ok(ApiResponse.success("Demande soumise", response));
    }

    @PostMapping("/demandes-remboursement/{demandeId}/traiter")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<ApiResponse<DemandeResponse>> traiterDemande(
            @PathVariable UUID demandeId, @Valid @RequestBody TraiterDemandeRequest request) {
        DemandeResponse response = mutuelleService.traiterDemande(demandeId, request);
        return ResponseEntity.ok(ApiResponse.success("Demande traitee", response));
    }

    @GetMapping("/pharmacies/{pharmacieId}/demandes-remboursement")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<Page<DemandeResponse>> getDemandes(@PathVariable UUID pharmacieId, Pageable pageable) {
        return ResponseEntity.ok(mutuelleService.getDemandesByPharmacie(pharmacieId, pageable));
    }

    @GetMapping("/pharmacies/{pharmacieId}/demandes-remboursement/en-attente")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<List<DemandeResponse>> getDemandesEnAttente(@PathVariable UUID pharmacieId) {
        return ResponseEntity.ok(mutuelleService.getDemandesEnAttente(pharmacieId));
    }

    @GetMapping("/pharmacies/{pharmacieId}/demandes-remboursement/creances")
    @PreAuthorize("hasRole('PHARMACIEN')")
    public ResponseEntity<BigDecimal> getCreances(@PathVariable UUID pharmacieId) {
        return ResponseEntity.ok(mutuelleService.getCreances(pharmacieId));
    }

    @GetMapping("/mutuelles/enums/types")
    public ResponseEntity<TypeMutuelle[]> getTypesMutuelle() {
        return ResponseEntity.ok(TypeMutuelle.values());
    }

    @GetMapping("/mutuelles/enums/couvertures")
    public ResponseEntity<TypeCouverture[]> getTypesCouverture() {
        return ResponseEntity.ok(TypeCouverture.values());
    }

    @GetMapping("/demandes-remboursement/enums/statuts")
    public ResponseEntity<StatutDemande[]> getStatutsDemande() {
        return ResponseEntity.ok(StatutDemande.values());
    }
}
