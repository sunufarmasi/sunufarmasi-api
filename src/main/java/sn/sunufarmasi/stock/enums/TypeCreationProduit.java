package sn.sunufarmasi.stock.enums;

public enum TypeCreationProduit {

    CATALOGUE_NATIONAL("Catalogue national", "Produit du catalogue officiel"),
    PREPARATION_OFFICINALE("Préparation officinale", "Préparation faite par la pharmacie"),
    PRODUIT_LOCAL("Produit local", "Produit local non référencé"),
    IMPORT_PRIVE("Import privé", "Produit importé directement"),
    AUTRE("Autre", "Autre type de produit");

    private final String libelle;
    private final String description;

    TypeCreationProduit(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
}