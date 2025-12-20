package sn.sunufarmasi.auth.service;

/**
 * Interface pour l'envoi de SMS
 * Permet de changer facilement de provider (Twilio, Orange SMS API, etc.)
 *
 * @author WeCan
 * @since 1.0.0
 */
public interface SmsService {

    /**
     * Envoyer un SMS
     *
     * @param telephone Numéro de téléphone destinataire (format: +221XXXXXXXXX)
     * @param message Contenu du message
     */
    void sendSms(String telephone, String message);

    /**
     * Vérifier si le service SMS est disponible
     *
     * @return true si le service est opérationnel
     */
    boolean isAvailable();
}