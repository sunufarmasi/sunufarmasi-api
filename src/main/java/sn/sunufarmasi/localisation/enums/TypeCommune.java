package sn.sunufarmasi.localisation.enums;

/**
 * Types de communes
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeCommune {

    COMMUNE("Commune"),
    VILLE("Ville"),
    COMMUNE_ARRONDISSEMENT("Commune d'arrondissement"),
    VILLAGE("Village"),
    QUARTIER("Quartier"),
    CAPITALE("Capitale"),
    CHEF_LIEU_REGION("Chef-lieu de région"),
    CHEF_LIEU_DEPARTEMENT("Chef-lieu de département");

    private final String libelle;

    TypeCommune(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
