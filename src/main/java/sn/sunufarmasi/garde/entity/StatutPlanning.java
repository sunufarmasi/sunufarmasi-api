package sn.sunufarmasi.garde.entity;

/**
 * Statuts d'un planning de garde
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum StatutPlanning {

    /**
     * Brouillon - En cours d'élaboration
     */
    BROUILLON("Brouillon", "Le planning est en cours d'élaboration", true),

    /**
     * En validation - Soumis pour validation
     */
    EN_VALIDATION("En validation", "Le planning est en attente de validation", false),

    /**
     * Validé - Prêt à être publié
     */
    VALIDE("Validé", "Le planning est validé et prêt à être publié", false),

    /**
     * Publié - Visible par les pharmacies
     */
    PUBLIE("Publié", "Le planning est publié et visible par les pharmacies", false),

    /**
     * Archivé - Période terminée
     */
    ARCHIVE("Archivé", "Le planning est archivé (période terminée)", false);

    private final String libelle;
    private final String description;
    private final boolean modifiable;

    StatutPlanning(String libelle, String description, boolean modifiable) {
        this.libelle = libelle;
        this.description = description;
        this.modifiable = modifiable;
    }

    public String getLibelle() { return libelle; }
    public String getDescription() { return description; }
    public boolean isModifiable() { return modifiable; }

    /**
     * Vérifier si on peut publier
     */
    public boolean peutPublier() {
        return this == VALIDE;
    }

    /**
     * Vérifier si on peut soumettre pour validation
     */
    public boolean peutSoumettre() {
        return this == BROUILLON;
    }
}
