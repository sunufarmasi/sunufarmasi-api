package sn.sunufarmasi.mutuelle.enums;

/**
 * Types de mutuelles et organismes payeurs
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum TypeMutuelle {

    MUTUELLE_SANTE("Mutuelle de santé", "Mutuelle privée de santé"),
    IPM("IPM", "Institution de Prévoyance Maladie"),
    ASSURANCE("Assurance maladie", "Compagnie d'assurance santé"),
    CMU("CMU", "Couverture Maladie Universelle"),
    ENTREPRISE("Entreprise", "Prise en charge directe entreprise"),
    ONG("ONG", "Organisation non gouvernementale"),
    ETAT("État", "Prise en charge étatique"),
    AUTRE("Autre", "Autre type d'organisme");

    private final String libelle;
    private final String description;

    TypeMutuelle(String libelle, String description) {
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
     * Est-ce un organisme public ?
     */
    public boolean estPublic() {
        return this == CMU || this == ETAT;
    }
}
