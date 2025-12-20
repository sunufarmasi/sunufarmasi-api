package sn.sunufarmasi.employe.enums;

/**
 * Statut d'un employé
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutEmploye {

    ACTIF("Actif", "L'employé est actif et peut se connecter"),
    SUSPENDU("Suspendu", "L'employé est temporairement suspendu"),
    INACTIF("Inactif", "L'employé n'est plus actif (licencié, démission)");

    private final String libelle;
    private final String description;

    StatutEmploye(String libelle, String description) {
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
