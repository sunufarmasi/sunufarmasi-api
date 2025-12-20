package sn.sunufarmasi.syndicat.enums;

/**
 * Statut d'un syndicat
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutSyndicat {

    ACTIF("Actif", "Le syndicat est opérationnel"),
    SUSPENDU("Suspendu", "Le syndicat est temporairement suspendu"),
    INACTIF("Inactif", "Le syndicat n'est plus actif");

    private final String libelle;
    private final String description;

    StatutSyndicat(String libelle, String description) {
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
