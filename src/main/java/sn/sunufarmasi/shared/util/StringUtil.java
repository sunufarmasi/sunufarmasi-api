package sn.sunufarmasi.shared.util;

import java.text.Normalizer;
import java.util.Random;
import java.util.UUID;

/**
 * Utilitaires de manipulation de chaînes de caractères
 * 
 * @author WeCan
 * @since 1.0.0
 */
public final class StringUtil {
    
    private StringUtil() {
        // Classe utilitaire - constructeur privé
    }
    
    private static final Random RANDOM = new Random();
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String NUMERIC = "0123456789";
    
    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATIONS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Vérifier si une chaîne est null ou vide
     * 
     * @param str Chaîne à vérifier
     * @return true si null ou vide
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
    
    /**
     * Vérifier si une chaîne est null, vide ou ne contient que des espaces
     * 
     * @param str Chaîne à vérifier
     * @return true si null, vide ou blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
    
    /**
     * Vérifier si une chaîne n'est ni null ni vide
     * 
     * @param str Chaîne à vérifier
     * @return true si non vide
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    /**
     * Vérifier si une chaîne n'est ni null, ni vide, ni blank
     * 
     * @param str Chaîne à vérifier
     * @return true si non blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    // ═══════════════════════════════════════════════════════════
    // GÉNÉRATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Générer une chaîne aléatoire alphanumérique
     * 
     * @param length Longueur de la chaîne
     * @return Chaîne aléatoire
     */
    public static String generateRandomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
    
    /**
     * Générer une chaîne aléatoire numérique
     * 
     * @param length Longueur de la chaîne
     * @return Chaîne numérique aléatoire
     */
    public static String generateRandomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC.charAt(RANDOM.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }
    
    /**
     * Générer un UUID
     * 
     * @return UUID au format string
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Générer un code OTP
     * 
     * @param length Longueur du code OTP
     * @return Code OTP numérique
     */
    public static String generateOtp(int length) {
        return generateRandomNumeric(length);
    }
    
    // ═══════════════════════════════════════════════════════════
    // MANIPULATION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Tronquer une chaîne à une longueur maximale
     * 
     * @param str Chaîne à tronquer
     * @param maxLength Longueur maximale
     * @return Chaîne tronquée
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
    
    /**
     * Tronquer une chaîne avec ellipses (...)
     * 
     * @param str Chaîne à tronquer
     * @param maxLength Longueur maximale (incluant les ...)
     * @return Chaîne tronquée avec ...
     */
    public static String truncateWithEllipsis(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        if (maxLength <= 3) {
            return "...";
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Supprimer les accents d'une chaîne
     * 
     * @param str Chaîne avec accents
     * @return Chaîne sans accents
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) {
            return str;
        }
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
    
    /**
     * Convertir une chaîne en slug (URL-friendly)
     * Exemple: "Pharmacie Centrale Dakar" -> "pharmacie-centrale-dakar"
     * 
     * @param str Chaîne à convertir
     * @return Slug
     */
    public static String toSlug(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String slug = removeAccents(str);
        slug = slug.toLowerCase();
        slug = slug.replaceAll("[^a-z0-9\\s-]", "");
        slug = slug.trim().replaceAll("\\s+", "-");
        slug = slug.replaceAll("-+", "-");
        
        return slug;
    }
    
    /**
     * Capitaliser la première lettre
     * 
     * @param str Chaîne à capitaliser
     * @return Chaîne avec première lettre en majuscule
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Capitaliser chaque mot
     * 
     * @param str Chaîne à capitaliser
     * @return Chaîne avec chaque mot capitalisé
     */
    public static String capitalizeWords(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(capitalize(word)).append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    // ═══════════════════════════════════════════════════════════
    // MASQUAGE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Masquer un email
     * Exemple: "john.doe@example.com" -> "j***e@example.com"
     * 
     * @param email Email à masquer
     * @return Email masqué
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "*".repeat(localPart.length()) + "@" + domain;
        }
        
        return localPart.charAt(0) + 
               "*".repeat(localPart.length() - 2) + 
               localPart.charAt(localPart.length() - 1) + 
               "@" + domain;
    }
    
    /**
     * Masquer un numéro de téléphone
     * Exemple: "+221771234567" -> "+221****4567"
     * 
     * @param phone Téléphone à masquer
     * @return Téléphone masqué
     */
    public static String maskPhone(String phone) {
        if (isEmpty(phone) || phone.length() < 8) {
            return phone;
        }
        
        int visibleStart = 4; // Montrer les 4 premiers caractères (+221)
        int visibleEnd = 4;   // Montrer les 4 derniers chiffres
        int maskLength = phone.length() - visibleStart - visibleEnd;
        
        if (maskLength <= 0) {
            return phone;
        }
        
        return phone.substring(0, visibleStart) + 
               "*".repeat(maskLength) + 
               phone.substring(phone.length() - visibleEnd);
    }
    
    /**
     * Masquer une partie d'une chaîne
     * 
     * @param str Chaîne à masquer
     * @param visibleStart Nombre de caractères visibles au début
     * @param visibleEnd Nombre de caractères visibles à la fin
     * @return Chaîne masquée
     */
    public static String mask(String str, int visibleStart, int visibleEnd) {
        if (isEmpty(str) || str.length() <= (visibleStart + visibleEnd)) {
            return str;
        }
        
        int maskLength = str.length() - visibleStart - visibleEnd;
        
        return str.substring(0, visibleStart) + 
               "*".repeat(maskLength) + 
               str.substring(str.length() - visibleEnd);
    }
    
    // ═══════════════════════════════════════════════════════════
    // FORMATAGE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Formater un numéro de téléphone sénégalais pour affichage
     * Exemple: "+221771234567" -> "+221 77 123 45 67"
     * 
     * @param phone Téléphone à formater
     * @return Téléphone formaté
     */
    public static String formatSenegalPhone(String phone) {
        if (isEmpty(phone) || !phone.startsWith("+221")) {
            return phone;
        }
        
        String cleaned = phone.replace(" ", "");
        if (cleaned.length() != 13) {
            return phone;
        }
        
        // +221 77 123 45 67
        return cleaned.substring(0, 4) + " " +
               cleaned.substring(4, 6) + " " +
               cleaned.substring(6, 9) + " " +
               cleaned.substring(9, 11) + " " +
               cleaned.substring(11);
    }
    
    /**
     * Formater un montant en FCFA
     * Exemple: 25000 -> "25 000 FCFA"
     * 
     * @param amount Montant
     * @return Montant formaté
     */
    public static String formatAmount(double amount) {
        return String.format("%,.0f FCFA", amount).replace(",", " ");
    }
    
    /**
     * Joindre des chaînes avec un séparateur
     * 
     * @param separator Séparateur
     * @param parts Parties à joindre
     * @return Chaîne jointe
     */
    public static String join(String separator, String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }
        return String.join(separator, parts);
    }
    
    // ═══════════════════════════════════════════════════════════
    // COMPARAISON
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Comparer deux chaînes en ignorant la casse
     * 
     * @param str1 Première chaîne
     * @param str2 Deuxième chaîne
     * @return true si égales (ignore case)
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }
    
    /**
     * Vérifier si une chaîne contient une sous-chaîne (ignore case)
     * 
     * @param str Chaîne principale
     * @param substring Sous-chaîne à chercher
     * @return true si contient
     */
    public static boolean containsIgnoreCase(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) {
            return false;
        }
        return str.toLowerCase().contains(substring.toLowerCase());
    }
    
    // ═══════════════════════════════════════════════════════════
    // VALEURS PAR DÉFAUT
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Retourner une valeur par défaut si la chaîne est null ou vide
     * 
     * @param str Chaîne à vérifier
     * @param defaultValue Valeur par défaut
     * @return str si non vide, sinon defaultValue
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }
    
    /**
     * Retourner une valeur par défaut si la chaîne est null ou blank
     * 
     * @param str Chaîne à vérifier
     * @param defaultValue Valeur par défaut
     * @return str si non blank, sinon defaultValue
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }
}
