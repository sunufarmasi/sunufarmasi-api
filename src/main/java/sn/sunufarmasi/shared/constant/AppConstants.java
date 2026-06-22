package sn.sunufarmasi.shared.constant;

/**
 * Constantes globales de l'application PharmaGo
 *
 * @author WeCan
 * @since 1.0.0
 */
public final class AppConstants {

    private AppConstants() {
        // Classe utilitaire - constructeur privé
    }

    // ═══════════════════════════════════════════════════════════
    // API
    // ═══════════════════════════════════════════════════════════

    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    public static final String API_PUBLIC_PATH = API_BASE_PATH + "/public";

    // ═══════════════════════════════════════════════════════════
    // JWT
    // ═══════════════════════════════════════════════════════════

    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final long JWT_ACCESS_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000L; // 7 jours en ms
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 30 * 24 * 60 * 60 * 1000L; // 30 jours en ms

    // ═══════════════════════════════════════════════════════════
    // OTP
    // ═══════════════════════════════════════════════════════════

    public static final int OTP_LENGTH = 6;
    public static final int OTP_VALIDITY_MINUTES = 10;
    public static final int OTP_MAX_ATTEMPTS = 3;
    public static final int OTP_RESEND_COOLDOWN_SECONDS = 60;

    // ═══════════════════════════════════════════════════════════
    // PAGINATION
    // ═══════════════════════════════════════════════════════════

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final String DEFAULT_SORT_FIELD = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    // ═══════════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════════

    // Téléphone
    public static final String SENEGAL_PHONE_REGEX = "^\\+221[0-9]{9}$";
    public static final String SENEGAL_PHONE_EXAMPLE = "+221771234567";

    // Email
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Mot de passe
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 100;
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$";

    // Noms
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 100;

    // ═══════════════════════════════════════════════════════════
    // RÔLES UTILISATEURS
    // ═══════════════════════════════════════════════════════════

    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_PHARMACIEN = "PHARMACIEN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SYNDICAT = "SYNDICAT";
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    // ═══════════════════════════════════════════════════════════
    // STATUTS
    // ═══════════════════════════════════════════════════════════

    // Pharmacie
    public static final String PHARMACIE_STATUS_ACTIVE = "ACTIVE";
    public static final String PHARMACIE_STATUS_INACTIVE = "INACTIVE";
    public static final String PHARMACIE_STATUS_SUSPENDED = "SUSPENDED";

    // Validation
    public static final String VALIDATION_STATUS_PENDING = "EN_ATTENTE";
    public static final String VALIDATION_STATUS_APPROVED = "APPROUVE";
    public static final String VALIDATION_STATUS_REJECTED = "REJETE";

    // Abonnement
    public static final String SUBSCRIPTION_STATUS_ACTIVE = "ACTIF";
    public static final String SUBSCRIPTION_STATUS_EXPIRED = "EXPIRE";
    public static final String SUBSCRIPTION_STATUS_CANCELLED = "ANNULE";
    public static final String SUBSCRIPTION_STATUS_PENDING = "EN_ATTENTE";

    // Paiement
    public static final String PAYMENT_STATUS_PENDING = "EN_ATTENTE";
    public static final String PAYMENT_STATUS_SUCCESS = "SUCCES";
    public static final String PAYMENT_STATUS_FAILED = "ECHEC";
    public static final String PAYMENT_STATUS_CANCELLED = "ANNULE";

    // ═══════════════════════════════════════════════════════════
    // GÉOLOCALISATION
    // ═══════════════════════════════════════════════════════════

    public static final double DEFAULT_SEARCH_RADIUS_KM = 5.0;
    public static final double MAX_SEARCH_RADIUS_KM = 50.0;
    public static final double EARTH_RADIUS_KM = 6371.0;

    // Coordonnées Dakar (par défaut)
    public static final double DAKAR_LATITUDE = 14.6937;
    public static final double DAKAR_LONGITUDE = -17.4441;

    // ═══════════════════════════════════════════════════════════
    // GARDE
    // ═══════════════════════════════════════════════════════════

    public static final String GARDE_TYPE_JOUR = "JOUR";
    public static final String GARDE_TYPE_NUIT = "NUIT";
    public static final String GARDE_TYPE_24H = "24H";

    // ═══════════════════════════════════════════════════════════
    // FICHIERS / UPLOAD
    // ═══════════════════════════════════════════════════════════

    public static final long MAX_FILE_SIZE_MB = 5;
    public static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/jpg", "image/webp"};
    public static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "image/jpeg", "image/png"};

    // ═══════════════════════════════════════════════════════════
    // CACHE
    // ═══════════════════════════════════════════════════════════

    public static final String CACHE_PHARMACIES = "pharmacies";
    public static final String CACHE_COMMUNES = "communes";
    public static final String CACHE_MEDICAMENTS = "medicaments";
    public static final String CACHE_PLANS_TARIFAIRES = "plans_tarifaires";

    public static final long CACHE_TTL_MINUTES = 30;

    // ═══════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    public static final String SMS_PROVIDER_DEFAULT = "orange";
    public static final String EMAIL_FROM = "noreply@pharmago.sn";
    public static final String EMAIL_FROM_NAME = "PharmaGo Sénégal";

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT MOBILE
    // ═══════════════════════════════════════════════════════════

    public static final String PAYMENT_PROVIDER_ORANGE_MONEY = "ORANGE_MONEY";
    public static final String PAYMENT_PROVIDER_WAVE = "WAVE";
    public static final String PAYMENT_PROVIDER_FREE_MONEY = "FREE_MONEY";

    public static final String PAYMENT_CURRENCY = "XOF"; // Franc CFA

    // ═══════════════════════════════════════════════════════════
    // TIMEZONE
    // ═══════════════════════════════════════════════════════════

    public static final String TIMEZONE_DAKAR = "Africa/Dakar";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm";

    // ═══════════════════════════════════════════════════════════
    // MESSAGES SYSTÈME
    // ═══════════════════════════════════════════════════════════

    public static final String APP_NAME = "PharmaGo";
    public static final String APP_DESCRIPTION = "Plateforme de localisation et gestion de pharmacies au Sénégal";
    public static final String APP_VERSION_NUMBER = "1.0.0";
    public static final String SUPPORT_EMAIL = "sunufarmasi@gmail.com";
    public static final String SUPPORT_PHONE = "+221 77 479 24 57";
}