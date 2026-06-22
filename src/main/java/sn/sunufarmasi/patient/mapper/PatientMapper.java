package sn.sunufarmasi.patient.mapper;

import org.springframework.stereotype.Component;
import sn.sunufarmasi.patient.dto.response.PatientResponse;
import sn.sunufarmasi.patient.entity.Patient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir Patient entity en DTO
 *
 * @author WeCan
 * @since 1.0.0
 */
@Component
public class PatientMapper {

    /**
     * Convertir Patient entity en PatientResponse
     */
    public PatientResponse toResponse(Patient patient) {
        if (patient == null) {
            return null;
        }

        // Commune
        PatientResponse.CommuneSimpleDto communeDto = null;
        if (patient.getCommune() != null) {
            String departementNom = patient.getCommune().getDepartement() != null
                    ? patient.getCommune().getDepartement().getNom()
                    : null;
            String regionNom = patient.getCommune().getDepartement() != null &&
                    patient.getCommune().getDepartement().getRegion() != null
                    ? patient.getCommune().getDepartement().getRegion().getNom()
                    : null;

            PatientResponse.CommuneSimpleDto.CoordinatesDto coordinates = null;
            if (patient.getCommune().hasCoordinates()) {
                coordinates = new PatientResponse.CommuneSimpleDto.CoordinatesDto(
                        patient.getCommune().getLatitude(),
                        patient.getCommune().getLongitude()
                );
            }

            communeDto = new PatientResponse.CommuneSimpleDto(
                    patient.getCommune().getId().toString(),
                    patient.getCommune().getNom(),
                    departementNom,
                    regionNom,
                    coordinates
            );
        }

        return new PatientResponse(
                patient.getId().toString(),
                patient.getEmail(),
                patient.getTelephone(),
                patient.getNomComplet(),
                patient.getDateNaissance(),
                patient.getSexe(),
                patient.getAdresse(),
                patient.getPhotoUrl(),
                communeDto,
                patient.isEmailVerified(),
                patient.isTelephoneVerified(),
                patient.isActif(),
                patient.isPremiumActif(),
                patient.getDateFinPremium(),
                patient.getMontantPremium(),
                patient.getReferencePaiement(),
                patient.getLastLoginAt(),
                patient.getCreatedAt()
        );
    }

    /**
     * Convertir une liste de Patient en liste de PatientResponse
     */
    public List<PatientResponse> toResponseList(List<Patient> patients) {
        if (patients == null) {
            return List.of();
        }

        return patients.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}