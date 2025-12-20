package sn.sunufarmasi.shared.util;

import sn.sunufarmasi.shared.constant.AppConstants;

import java.util.regex.Pattern;

/**
 * Utilitaires de validation pour l'application PharmaGo
 * 
 * @author WeCan
 * @since 1.0.0
 */
public final class ValidationUtil {
    
    private ValidationUtil() {
        // Classe utilitaire - constructeur privé
    }
    
    // Patterns compilés pour performance
    private static final Pattern EMAIL_PATTERN = Pattern.compile(AppConstants.EMAIL_REGEX);
    private static final Pattern SENEGAL_PHONE_PATTERN = Pattern.compile(AppConstants.SENEGAL_PHONE_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(AppConstants.PASSWORD_REGEX);
    
    // ═══════════════════════════════════════════════════════════
    // EMAIL
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un email
     * 
     * @param email Email à valider
     * @return true si valide
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Normaliser un email (lowercase, trim)
     * 
     * @param email Email à normaliser
     * @return Email normalisé
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
    
    // ═══════════════════════════════════════════════════════════
    // TÉLÉPHONE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un numéro de téléphone sénégalais
     * Format: +221XXXXXXXXX (9 chiffres après +221)
     * 
     * @param phone Numéro à valider
     * @return true si valide
     */
    public static boolean isValidSenegalPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return SENEGAL_PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Normaliser un numéro de téléphone sénégalais
     * Convertit différents formats vers +221XXXXXXXXX
     * 
     * @param phone Numéro à normaliser
     * @return Numéro normalisé ou null si invalide
     */
    public static String normalizeSenegalPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        
        // Nettoyer (supprimer espaces, tirets, parenthèses)
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        
        // Si commence par 00221, remplacer par +221
        if (cleaned.startsWith("00221")) {
            cleaned = "+221" + cleaned.substring(5);
        }
        // Si commence par 221 (sans +), ajouter +
        else if (cleaned.startsWith("221") && !cleaned.startsWith("+221")) {
            cleaned = "+" + cleaned;
        }
        // Si commence par 7 ou 3 (numéro local), ajouter +221
        else if (cleaned.matches("^[73]\\d{8}$")) {
            cleaned = "+221" + cleaned;
        }
        
        // Valider le format final
        return isValidSenegalPhone(cleaned) ? cleaned : null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MOT DE PASSE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un mot de passe
     * Doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre
     * 
     * @param password Mot de passe à valider
     * @return true si valide
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        
        // Vérifier longueur
        if (password.length() < AppConstants.PASSWORD_MIN_LENGTH || 
            password.length() > AppConstants.PASSWORD_MAX_LENGTH) {
            return false;
        }
        
        // Vérifier pattern (majuscule, minuscule, chiffre)
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Évaluer la force d'un mot de passe (0-4)
     * 0 = Très faible, 1 = Faible, 2 = Moyen, 3 = Fort, 4 = Très fort
     * 
     * @param password Mot de passe à évaluer
     * @return Score de force (0-4)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isBlank()) {
            return 0;
        }
        
        int score = 0;
        
        // Longueur
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Complexité
        if (password.matches(".*[a-z].*")) score++; // Minuscule
        if (password.matches(".*[A-Z].*")) score++; // Majuscule
        if (password.matches(".*\\d.*")) score++;   // Chiffre
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++; // Caractère spécial
        
        // Normaliser sur 4
        return Math.min(4, score / 2);
    }
    
    // ═══════════════════════════════════════════════════════════
    // NOM / PRÉNOM
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un nom ou prénom
     * 
     * @param name Nom à valider
     * @return true si valide
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        
        String trimmed = name.trim();
        return trimmed.length() >= AppConstants.NAME_MIN_LENGTH && 
               trimmed.length() <= AppConstants.NAME_MAX_LENGTH &&
               trimmed.matches("^[a-zA-ZÀ-ÿ\\s\\-']+$"); // Lettres, espaces, tirets, apostrophes
    }
    
    /**
     * Capitaliser un nom (première lettre en majuscule)
     * 
     * @param name Nom à capitaliser
     * @return Nom capitalisé
     */
    public static String capitalizeName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        
        String[] words = name.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    // ═══════════════════════════════════════════════════════════
    // UUID
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un UUID
     * 
     * @param uuid UUID à valider
     * @return true si valide
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return false;
        }
        
        try {
            java.util.UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // COORDONNÉES GPS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider une latitude
     * 
     * @param latitude Latitude à valider
     * @return true si valide (-90 à 90)
     */
    public static boolean isValidLatitude(Double latitude) {
        return latitude != null && latitude >= -90.0 && latitude <= 90.0;
    }
    
    /**
     * Valider une longitude
     * 
     * @param longitude Longitude à valider
     * @return true si valide (-180 à 180)
     */
    public static boolean isValidLongitude(Double longitude) {
        return longitude != null && longitude >= -180.0 && longitude <= 180.0;
    }
    
    /**
     * Valider des coordonnées GPS (latitude + longitude)
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true si les deux sont valides
     */
    public static boolean isValidCoordinates(Double latitude, Double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }
    
    // ═══════════════════════════════════════════════════════════
    // URL
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider une URL
     * 
     * @param url URL à valider
     * @return true si valide
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // NUMÉRO D'AGRÉMENT PHARMACIE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un numéro d'agrément de pharmacie
     * Format attendu: XX-XXX-XXXX (région-département-numéro)
     * 
     * @param agrement Numéro d'agrément
     * @return true si valide
     */
    public static boolean isValidPharmacieAgrement(String agrement) {
        if (agrement == null || agrement.isBlank()) {
            return false;
        }
        
        // Format: DK-001-2024 ou similaire
        return agrement.matches("^[A-Z]{2}-\\d{3}-\\d{4}$");
    }
    
    /**
     * Normaliser un numéro d'agrément
     * 
     * @param agrement Numéro d'agrément
     * @return Agrément normalisé (uppercase, trimmed)
     */
    public static String normalizePharmacieAgrement(String agrement) {
        if (agrement == null) {
            return null;
        }
        return agrement.trim().toUpperCase();
    }
    
    // ═══════════════════════════════════════════════════════════
    // MONTANT
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un montant (doit être positif)
     * 
     * @param amount Montant à valider
     * @return true si valide
     */
    public static boolean isValidAmount(Double amount) {
        return amount != null && amount > 0;
    }
    
    /**
     * Valider un montant dans une plage
     * 
     * @param amount Montant à valider
     * @param min Montant minimum
     * @param max Montant maximum
     * @return true si valide
     */
    public static boolean isValidAmountRange(Double amount, Double min, Double max) {
        return amount != null && amount >= min && amount <= max;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CODE OTP
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Valider un code OTP
     * 
     * @param otp Code OTP
     * @return true si valide (6 chiffres)
     */
    public static boolean isValidOtp(String otp) {
        if (otp == null || otp.isBlank()) {
            return false;
        }
        return otp.matches("^\\d{" + AppConstants.OTP_LENGTH + "}$");
    }
}
