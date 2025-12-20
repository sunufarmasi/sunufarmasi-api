package sn.sunufarmasi.shared.constant;

/**
 * Messages de succès standardisés pour l'API SunuFarmasi
 * Tous les messages de succès doivent être définis ici pour faciliter la maintenance
 * et l'internationalisation future
 *
 * @author WeCan
 * @since 1.0.0
 */
public final class SuccessMessages {

    private SuccessMessages() {
        // Classe utilitaire - constructeur privé
    }

    // ═══════════════════════════════════════════════════════════
    // AUTHENTIFICATION
    // ═══════════════════════════════════════════════════════════

    public static final String AUTH_LOGIN_SUCCESS = "Connexion réussie";
    public static final String AUTH_LOGOUT_SUCCESS = "Déconnexion réussie";
    public static final String AUTH_REGISTRATION_SUCCESS = "Inscription réussie. Vérifiez votre téléphone pour le code OTP";
    public static final String AUTH_ACCOUNT_VERIFIED = "Compte vérifié avec succès";
    public static final String AUTH_TOKEN_REFRESHED = "Token rafraîchi avec succès";

    // ═══════════════════════════════════════════════════════════
    // OTP
    // ═══════════════════════════════════════════════════════════

    public static final String OTP_SENT = "Code OTP envoyé avec succès";
    public static final String OTP_VERIFIED = "Code OTP vérifié avec succès";
    public static final String OTP_RESENT = "Nouveau code OTP envoyé";

    // ═══════════════════════════════════════════════════════════
    // PATIENT
    // ═══════════════════════════════════════════════════════════

    public static final String PATIENT_CREATED = "Compte patient créé avec succès";
    public static final String PATIENT_UPDATED = "Profil patient mis à jour avec succès";
    public static final String PATIENT_DELETED = "Compte patient supprimé avec succès";
    public static final String PATIENT_PROFILE_RETRIEVED = "Profil patient récupéré avec succès";

    public static final String PROFILE_UPDATED = "Profil mis à jour avec succès";
    public static final String PASSWORD_CHANGED = "Mot de passe modifié avec succès";
    public static final String ACCOUNT_DEACTIVATED = "Compte désactivé avec succès";

    // ═══════════════════════════════════════════════════════════
    // ABONNEMENT
    // ═══════════════════════════════════════════════════════════

    public static final String SUBSCRIPTION_CREATED = "Abonnement créé avec succès";
    public static final String SUBSCRIPTION_UPDATED = "Abonnement mis à jour avec succès";
    public static final String SUBSCRIPTION_CANCELLED = "Abonnement annulé avec succès";
    public static final String SUBSCRIPTION_RENEWED = "Abonnement renouvelé avec succès";
    public static final String SUBSCRIPTION_TRIAL_ACTIVATED = "Période d'essai de 15 jours activée";

    // ═══════════════════════════════════════════════════════════
    // PAIEMENT
    // ═══════════════════════════════════════════════════════════

    public static final String PAYMENT_INITIATED = "Paiement initié avec succès";
    public static final String PAYMENT_SUCCESS = "Paiement effectué avec succès";
    public static final String PAYMENT_VERIFIED = "Paiement vérifié avec succès";

    // ═══════════════════════════════════════════════════════════
    // PHARMACIE
    // ═══════════════════════════════════════════════════════════

    public static final String PHARMACIE_CREATED = "Pharmacie créée avec succès";
    public static final String PHARMACIE_UPDATED = "Pharmacie mise à jour avec succès";
    public static final String PHARMACIE_DELETED = "Pharmacie supprimée avec succès";

    // ═══════════════════════════════════════════════════════════
    // GÉNÉRIQUE
    // ═══════════════════════════════════════════════════════════

    public static final String OPERATION_SUCCESS = "Opération effectuée avec succès";
    public static final String DATA_RETRIEVED = "Données récupérées avec succès";
    public static final String DATA_SAVED = "Données enregistrées avec succès";
    public static final String DATA_UPDATED = "Données mises à jour avec succès";
    public static final String DATA_DELETED = "Données supprimées avec succès";

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