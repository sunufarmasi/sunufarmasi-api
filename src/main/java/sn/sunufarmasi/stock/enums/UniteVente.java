package sn.sunufarmasi.stock.enums;

/**
 * Unités de vente des produits
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum UniteVente {

    BOITE("Boîte", "bte"),
    UNITE("Unité", "u"),
    PLAQUETTE("Plaquette", "plaq"),
    FLACON("Flacon", "fl"),
    TUBE("Tube", "tb"),
    SACHET("Sachet", "sach"),
    AMPOULE("Ampoule", "amp"),
    SERINGUE("Seringue", "ser"),
    POT("Pot", "pot"),
    SPRAY("Spray", "spr"),
    DISPOSITIF("Dispositif", "disp"),
    KIT("Kit", "kit"),
    PIECE("Pièce", "pc"),
    LITRE("Litre", "L"),
    MILLILITRE("Millilitre", "mL"),
    GRAMME("Gramme", "g"),
    KILOGRAMME("Kilogramme", "kg");

    private final String libelle;
    private final String abreviation;

    UniteVente(String libelle, String abreviation) {
        this.libelle = libelle;
        this.abreviation = abreviation;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getAbreviation() {
        return abreviation;
    }
}
