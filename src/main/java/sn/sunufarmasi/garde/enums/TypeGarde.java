package sn.sunufarmasi.garde.enums;

/**
 * Types de garde pharmaceutique au Sénégal
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeGarde {

    /**
     * Garde de jour uniquement (8h - 20h)
     */
    JOUR("Garde de jour", "De 08h à 20h", 8, 20),

    /**
     * Garde de nuit uniquement (20h - 8h)
     */
    NUIT("Garde de nuit", "De 20h à 08h", 20, 8),

    /**
     * Garde jour et nuit (24h/24) - Le plus courant au Sénégal
     */
    JOUR_ET_NUIT("Garde 24h", "24 heures sur 24", 0, 24);

    private final String libelle;
    private final String description;
    private final int heureDebut;
    private final int heureFin;

    TypeGarde(String libelle, String description, int heureDebut, int heureFin) {
        this.libelle = libelle;
        this.description = description;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public int getHeureDebut() {
        return heureDebut;
    }

    public int getHeureFin() {
        return heureFin;
    }

    /**
     * Obtenir les horaires par défaut formatés
     */
    public String getHorairesDefaut() {
        if (this == JOUR_ET_NUIT) {
            return "24h/24";
        }
        return String.format("%02d:00 - %02d:00", heureDebut, heureFin);
    }
}