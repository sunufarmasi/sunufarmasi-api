package sn.sunufarmasi.syndicat.enums;

/**
 * Type de syndicat selon sa portée géographique
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeSyndicat {

    COMMUNE("Commune", "Gère les pharmacies d'une commune"),
    ZONE("Zone multi-communes", "Gère les pharmacies de plusieurs communes spécifiques"),
    DEPARTEMENT("Département", "Gère les pharmacies d'un département entier"),
    REGION("Région", "Gère les pharmacies d'une région entière");

    private final String libelle;
    private final String description;

    TypeSyndicat(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }
}
