package sn.sunufarmasi.vente.enums;

/**
 * Modes de paiement acceptés
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum ModePaiement {

    ESPECES("Espèces", "ESP", true),
    CARTE_BANCAIRE("Carte bancaire", "CB", true),
    ORANGE_MONEY("Orange Money", "OM", true),
    WAVE("Wave", "WAVE", true),
    FREE_MONEY("Free Money", "FM", true),
    VIREMENT("Virement bancaire", "VIR", false),
    CHEQUE("Chèque", "CHQ", false),
    MUTUELLE("Mutuelle/Tiers payant", "MUT", false),
    CREDIT("Crédit client", "CRD", false),
    MIXTE("Paiement mixte", "MIX", true);

    private final String libelle;
    private final String code;
    private final boolean instantane;  // Paiement immédiat ou différé

    ModePaiement(String libelle, String code, boolean instantane) {
        this.libelle = libelle;
        this.code = code;
        this.instantane = instantane;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getCode() {
        return code;
    }

    public boolean isInstantane() {
        return instantane;
    }

    /**
     * Modes de paiement mobile money
     */
    public boolean isMobileMoney() {
        return this == ORANGE_MONEY || this == WAVE || this == FREE_MONEY;
    }
}
