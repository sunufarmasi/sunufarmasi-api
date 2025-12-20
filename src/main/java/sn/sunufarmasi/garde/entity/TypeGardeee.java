package sn.sunufarmasi.garde.entity;

/**
 * Types de garde
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeGardeee {

    /**
     * Garde de jour (8h - 20h)
     */
    JOUR("Garde de Jour", "08:00 - 20:00", false),

    /**
     * Garde de nuit (20h - 8h)
     */
    NUIT("Garde de Nuit", "20:00 - 08:00", true),

    /**
     * Garde 24h (toute la journée)
     */
    JOUR_24H("Garde 24h", "00:00 - 23:59", true),

    /**
     * Garde jour férié
     */
    JOUR_FERIE("Jour Férié", "00:00 - 23:59", true),

    /**
     * Garde weekend (samedi/dimanche)
     */
    WEEKEND("Weekend", "00:00 - 23:59", true);

    private final String libelle;
    private final String horairesDefaut;
    private final boolean estMajoree;

    TypeGardeee(String libelle, String horairesDefaut, boolean estMajoree) {
        this.libelle = libelle;
        this.horairesDefaut = horairesDefaut;
        this.estMajoree = estMajoree;
    }

    public String getLibelle() { return libelle; }
//    public String getHorairesDefaut() { return horairesDefaut; }
    public boolean isEstMajoree() { return estMajoree; }

    /**
     * Taux de majoration pour ce type de garde (en %)
     */
    public int getTauxMajoration() {
        return switch (this) {
            case JOUR -> 0;
            case NUIT -> 25;
            case JOUR_24H -> 15;
            case JOUR_FERIE -> 50;
            case WEEKEND -> 25;
        };
    }

//    public String getHorairesDefaut() {
////        if (heureDebut == 0 && heureFin == 24) {
////            return "00:00 - 23:59";
////        }
////        return String.format("%02d:00 - %02d:00", heureDebut, heureFin);
//    }
}


//
//package sn.sunufarmasi.garde.enums;
//
///**
// * Types de garde pharmaceutique
// *
// * @author WeCan
// * @since 1.0.0
// */
//public enum TypeGarde {
//
//    JOUR("Garde de jour", "De 08h à 20h", 8, 20),
//    NUIT("Garde de nuit", "De 20h à 08h", 20, 8),
//    JOUR_24H("Garde 24h", "24 heures complètes", 0, 24),
//    WEEKEND("Garde weekend", "Samedi et dimanche", 0, 24),
//    JOUR_FERIE("Garde jour férié", "Jour férié", 0, 24);
//
//    private final String libelle;
//    private final String description;
//    private final int heureDebut;
//    private final int heureFin;
//
//    TypeGarde(String libelle, String description, int heureDebut, int heureFin) {
//        this.libelle = libelle;
//        this.description = description;
//        this.heureDebut = heureDebut;
//        this.heureFin = heureFin;
//    }
//
//    public String getLibelle() {
//        return libelle;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public int getHeureDebut() {
//        return heureDebut;
//    }
//
//    public int getHeureFin() {
//        return heureFin;
//    }
//
//    /**
//     * Obtenir les horaires par défaut formatés
//     */
//    public String getHorairesDefaut() {
//        if (heureDebut == 0 && heureFin == 24) {
//            return "00:00 - 23:59";
//        }
//        return String.format("%02d:00 - %02d:00", heureDebut, heureFin);
//    }
//}