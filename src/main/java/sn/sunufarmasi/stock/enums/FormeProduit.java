package sn.sunufarmasi.stock.enums;

/**
 * Formes galéniques des produits pharmaceutiques
 *
 * @author WeCan
 * @since 1.0.0
 */
public enum FormeProduit {

    // Formes orales solides
    COMPRIME("Comprimé", "cp"),
    GELULE("Gélule", "gél"),
    CAPSULE("Capsule", "caps"),
    DRAGEE("Dragée", "drag"),
    GRANULE("Granulé", "gran"),
    POUDRE_ORALE("Poudre orale", "pdr"),
    SACHET("Sachet", "sach"),
    PASTILLE("Pastille", "past"),

    // Formes orales liquides
    SIROP("Sirop", "sir"),
    SOLUTION_BUVABLE("Solution buvable", "sol buv"),
    SUSPENSION_BUVABLE("Suspension buvable", "susp buv"),
    GOUTTES_BUVABLES("Gouttes buvables", "gttes buv"),
    AMPOULE_BUVABLE("Ampoule buvable", "amp buv"),

    // Formes injectables
    INJECTABLE("Injectable", "inj"),
    AMPOULE_INJECTABLE("Ampoule injectable", "amp inj"),
    FLACON_INJECTABLE("Flacon injectable", "fl inj"),
    SERINGUE_PREREMPLIE("Seringue pré-remplie", "ser"),
    PERFUSION("Perfusion", "perf"),

    // Formes cutanées
    CREME("Crème", "crm"),
    POMMADE("Pommade", "pom"),
    GEL("Gel", "gel"),
    LOTION("Lotion", "lot"),
    SPRAY_CUTANE("Spray cutané", "spr cut"),
    POUDRE_CUTANEE("Poudre cutanée", "pdr cut"),
    PATCH("Patch", "patch"),

    // Formes ophtalmiques
    COLLYRE("Collyre", "coll"),
    POMMADE_OPHTALMIQUE("Pommade ophtalmique", "pom opht"),
    GEL_OPHTALMIQUE("Gel ophtalmique", "gel opht"),

    // Formes ORL
    GOUTTES_AURICULAIRES("Gouttes auriculaires", "gttes aur"),
    SPRAY_NASAL("Spray nasal", "spr nas"),
    GOUTTES_NASALES("Gouttes nasales", "gttes nas"),

    // Formes respiratoires
    AEROSOL("Aérosol", "aér"),
    INHALATEUR("Inhalateur", "inh"),
    NEBULISATEUR("Nébulisateur", "néb"),

    // Formes rectales/vaginales
    SUPPOSITOIRE("Suppositoire", "suppo"),
    OVULE("Ovule", "ov"),
    CREME_VAGINALE("Crème vaginale", "crm vag"),

    // Autres
    DISPOSITIF_MEDICAL("Dispositif médical", "DM"),
    AUTRE("Autre", "autre");

    private final String libelle;
    private final String abreviation;

    FormeProduit(String libelle, String abreviation) {
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
