package sn.sunufarmasi.auth.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO Request pour l'inscription d'un patient
 * 
 * @author WeCan
 * @since 1.0.0
 */
public record RegisterPatientRequest(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    String email,
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre"
    )
    String password,
    
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    String confirmPassword,
    
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
        regexp = "^\\+221[0-9]{9}$",
        message = "Format de téléphone invalide. Format attendu: +221XXXXXXXXX"
    )
    String telephone,
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    String prenom,
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    String nom,
    
    @Past(message = "La date de naissance doit être dans le passé")
    LocalDate dateNaissance,
    
    @Pattern(regexp = "M|F", message = "Sexe invalide. Valeurs acceptées: M ou F")
    String sexe,
    
    @NotNull(message = "La commune est obligatoire")
    String communeId,
    
    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    String adresse,
    
    String photoUrl
) {
    /**
     * Constructeur compact pour validations personnalisées
     */
    public RegisterPatientRequest {
        // Normaliser l'email
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        
        // Normaliser le téléphone
        if (telephone != null) {
            telephone = telephone.trim();
        }
        
        // Vérifier que les mots de passe correspondent
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }
        
        // Vérifier l'âge minimum (13 ans)
        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now().minusYears(13))) {
            throw new IllegalArgumentException("Vous devez avoir au moins 13 ans pour vous inscrire");
        }
    }
    
    /**
     * Calculer l'âge
     */
    public int getAge() {
        if (dateNaissance == null) return 0;
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }
}
