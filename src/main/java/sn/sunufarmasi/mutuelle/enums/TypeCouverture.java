package sn.sunufarmasi.mutuelle.enums;

/**
 * Types de couverture mutuelle
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeCouverture {

    TOTALE("Couverture totale", "100% des frais pris en charge", 100),
    PARTIELLE_80("Couverture 80%", "80% des frais pris en charge", 80),
    PARTIELLE_70("Couverture 70%", "70% des frais pris en charge", 70),
    PARTIELLE_60("Couverture 60%", "60% des frais pris en charge", 60),
    PARTIELLE_50("Couverture 50%", "50% des frais pris en charge", 50),
    FORFAITAIRE("Forfaitaire", "Montant forfaitaire par acte", 0),
    PLAFONNEE("Plafonnée", "Couverture avec plafond annuel", 0),
    PERSONNALISEE("Personnalisée", "Taux personnalisé", 0);

    private final String libelle;
    private final String description;
    private final int tauxDefaut;

    TypeCouverture(String libelle, String description, int tauxDefaut) {
        this.libelle = libelle;
        this.description = description;
        this.tauxDefaut = tauxDefaut;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public int getTauxDefaut() {
        return tauxDefaut;
    }
}
