package sn.sunufarmasi.shared.constant;

/**
 * Messages d'erreur standardisés pour l'API SunuFarmasi
 * Tous les messages d'erreur doivent être définis ici pour faciliter la maintenance
 * et l'internationalisation future
 *
 * @author WeCan
 * @since 1.0.0
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Classe utilitaire - constructeur privé
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    public static final String AUTH_INVALID_CREDENTIALS = "Téléphone ou mot de passe incorrect";
    public static final String AUTH_ACCOUNT_NOT_VERIFIED = "Votre compte n'est pas vérifié. Veuillez vérifier votre téléphone avec le code OTP";
    public static final String AUTH_ACCOUNT_DISABLED = "Votre compte a été désactivé. Contactez l'administrateur";
    public static final String AUTH_ACCOUNT_LOCKED = "Votre compte est temporairement bloqué suite à plusieurs tentatives échouées";
    public static final String AUTH_SESSION_EXPIRED = "Votre session a expiré. Veuillez vous reconnecter";
    public static final String AUTH_TOKEN_INVALID = "Token d'authentification invalide ou expiré";
    public static final String AUTH_TOKEN_MISSING = "Token d'authentification manquant";
    public static final String AUTH_UNAUTHORIZED = "Vous devez être connecté pour accéder à cette ressource";

    // ═══════════════════════════════════════════════════════════
    // OTP
    // ═══════════════════════════════════════════════════════════

    public static final String OTP_INVALID = "Code OTP invalide";
    public static final String OTP_EXPIRED = "Code OTP expiré. Demandez un nouveau code";
    public static final String OTP_MAX_ATTEMPTS = "Nombre maximum de tentatives atteint. Demandez un nouveau code";
    public static final String OTP_RESEND_COOLDOWN = "Veuillez attendre avant de demander un nouveau code";
    public static final String OTP_SEND_FAILED = "Échec de l'envoi du code OTP. Veuillez réessayer";
    public static final String OTP_NOT_FOUND = "Aucun code OTP trouvé pour cet utilisateur";

    // ═══════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════

    public static final String VALIDATION_EMAIL_INVALID = "Format d'email invalide";
    public static final String VALIDATION_EMAIL_REQUIRED = "L'email est obligatoire";

    public static final String EMAIL_EXISTS = "Cet email est déjà utilisé";
    public static final String PHONE_EXISTS = "Ce numéro de téléphone est déjà utilisé";

    public static final String VALIDATION_PHONE_INVALID = "Format de téléphone invalide. Format attendu: +221771234567";
    public static final String VALIDATION_PHONE_REQUIRED = "Le numéro de téléphone est obligatoire";

    public static final String VALIDATION_PASSWORD_REQUIRED = "Le mot de passe est obligatoire";
    public static final String VALIDATION_PASSWORD_TOO_SHORT = "Le mot de passe doit contenir au moins 8 caractères";
    public static final String VALIDATION_PASSWORD_WEAK = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre";
    public static final String VALIDATION_PASSWORD_MISMATCH = "Les mots de passe ne correspondent pas";

    public static final String VALIDATION_FIELD_REQUIRED = "Le champ '%s' est obligatoire";
    public static final String VALIDATION_FIELD_INVALID = "Le champ '%s' est invalide";
    public static final String VALIDATION_FIELD_TOO_SHORT = "Le champ '%s' est trop court (minimum %d caractères)";
    public static final String VALIDATION_FIELD_TOO_LONG = "Le champ '%s' est trop long (maximum %d caractères)";

    // ═══════════════════════════════════════════════════════════
    // RESSOURCES
    // ═══════════════════════════════════════════════════════════

    public static final String RESOURCE_NOT_FOUND = "%s non trouvé(e)";
    public static final String RESOURCE_ALREADY_EXISTS = "%s existe déjà";
    public static final String RESOURCE_CANNOT_BE_DELETED = "%s ne peut pas être supprimé(e)";
    public static final String RESOURCE_CANNOT_BE_UPDATED = "%s ne peut pas être modifié(e)";

    public static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    public static final String PATIENT_NOT_FOUND = "Patient non trouvé";
    public static final String PHARMACIEN_NOT_FOUND = "Pharmacien non trouvé";
    public static final String PHARMACIE_NOT_FOUND = "Pharmacie non trouvée";
    public static final String MEDICAMENT_NOT_FOUND = "Médicament non trouvé";
    public static final String COMMUNE_NOT_FOUND = "Commune non trouvée";
    public static final String ABONNEMENT_NOT_FOUND = "Abonnement non trouvé";
    public static final String PLAN_TARIFAIRE_NOT_FOUND = "Plan tarifaire non trouvé";

    // ═══════════════════════════════════════════════════════════
    // PERMISSIONS
    // ═══════════════════════════════════════════════════════════

    public static final String PERMISSION_DENIED = "Vous n'avez pas la permission d'effectuer cette action";
    public static final String PERMISSION_NOT_OWNER = "Vous n'êtes pas propriétaire de cette ressource";
    public static final String PERMISSION_INSUFFICIENT_ROLE = "Votre rôle est insuffisant pour cette action";
    public static final String PERMISSION_SUBSCRIPTION_REQUIRED = "Cette fonctionnalité nécessite un abonnement actif";
    public static final String PERMISSION_ACCOUNT_NOT_VALIDATED = "Votre compte est en attente de validation par un administrateur";

    // ═══════════════════════════════════════════════════════════
    // PHARMACIE
    // ═══════════════════════════════════════════════════════════

    public static final String PHARMACIE_AGREMENT_REQUIRED = "Le numéro d'agrément est obligatoire";
    public static final String PHARMACIE_AGREMENT_INVALID = "Numéro d'agrément invalide";
    public static final String PHARMACIE_AGREMENT_ALREADY_EXISTS = "Ce numéro d'agrément est déjà utilisé";
    public static final String PHARMACIE_ALREADY_REGISTERED = "Vous avez déjà une pharmacie enregistrée";
    public static final String PHARMACIE_INACTIVE = "Cette pharmacie est inactive";
    public static final String PHARMACIE_SUSPENDED = "Cette pharmacie est suspendue";
    public static final String PHARMACIE_LOCATION_REQUIRED = "Les coordonnées GPS sont obligatoires";
    public static final String PHARMACIE_LOCATION_INVALID = "Coordonnées GPS invalides";

    public static final String INVALID_GPS_COORDINATES = "Coordonnées GPS invalides";

    // ═══════════════════════════════════════════════════════════
    // ABONNEMENT
    // ═══════════════════════════════════════════════════════════

    public static final String SUBSCRIPTION_EXPIRED = "Votre abonnement a expiré";
    public static final String SUBSCRIPTION_ALREADY_ACTIVE = "Vous avez déjà un abonnement actif";
    public static final String SUBSCRIPTION_NOT_ACTIVE = "Vous n'avez pas d'abonnement actif";
    public static final String SUBSCRIPTION_CANNOT_DOWNGRADE = "Vous ne pouvez pas rétrograder vers un plan inférieur";
    public static final String SUBSCRIPTION_TRIAL_ALREADY_USED = "Vous avez déjà utilisé votre période d'essai";
    public static final String SUBSCRIPTION_PAYMENT_REQUIRED = "Un paiement est requis pour activer votre abonnement";

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    public static final String PAYMENT_FAILED = "Le paiement a échoué";
    public static final String PAYMENT_CANCELLED = "Le paiement a été annulé";
    public static final String PAYMENT_AMOUNT_INVALID = "Montant de paiement invalide";
    public static final String PAYMENT_METHOD_INVALID = "Méthode de paiement invalide";
    public static final String PAYMENT_ALREADY_PROCESSED = "Ce paiement a déjà été traité";
    public static final String PAYMENT_PHONE_INVALID = "Numéro de téléphone de paiement invalide";
    public static final String PAYMENT_PROVIDER_ERROR = "Erreur du fournisseur de paiement: %s";

    // ═══════════════════════════════════════════════════════════
    // SYSTÈME
    // ═══════════════════════════════════════════════════════════

    public static final String INTERNAL_SERVER_ERROR = "Une erreur interne s'est produite. Veuillez réessayer plus tard";
    public static final String SERVICE_UNAVAILABLE = "Service temporairement indisponible";
    public static final String DATABASE_ERROR = "Erreur de base de données";
    public static final String NETWORK_ERROR = "Erreur réseau. Vérifiez votre connexion";

    // ═══════════════════════════════════════════════════════════
    // MÉTHODES UTILITAIRES
    // ═══════════════════════════════════════════════════════════

    /**
     * Formater un message avec des paramètres
     */
    public static String format(String message, Object... params) {
        return String.format(message, params);
    }
}