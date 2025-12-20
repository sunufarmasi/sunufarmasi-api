package sn.sunufarmasi.stock.enums;

/**
 * Catégories de produits pharmaceutiques
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum CategorieProduit {

    MEDICAMENT("Médicament", "Produits pharmaceutiques avec AMM"),
    GENERIQUE("Générique", "Médicaments génériques"),
    ORDONNANCE("Sur ordonnance", "Médicaments nécessitant une ordonnance"),
    OTC("OTC", "Médicaments en vente libre (Over The Counter)"),
    PARAMEDICAL("Paramédical", "Produits paramédicaux"),
    COSMETIQUE("Cosmétique", "Produits cosmétiques et de beauté"),
    HYGIENE("Hygiène", "Produits d'hygiène corporelle"),
    NUTRITION("Nutrition", "Compléments alimentaires et nutrition"),
    BEBE("Bébé & Maman", "Produits pour bébés et mamans"),
    MATERIEL_MEDICAL("Matériel médical", "Équipements et dispositifs médicaux"),
    OPTIQUE("Optique", "Lunettes, lentilles et accessoires"),
    VETERINAIRE("Vétérinaire", "Produits vétérinaires"),
    PHYTOTHERAPIE("Phytothérapie", "Produits à base de plantes"),
    HOMEOPATHIE("Homéopathie", "Produits homéopathiques"),
    AUTRE("Autre", "Autres produits");

    private final String libelle;
    private final String description;

    CategorieProduit(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Catégories nécessitant une ordonnance
     */
    public boolean necessiteOrdonnance() {
        return this == ORDONNANCE;
    }
}
