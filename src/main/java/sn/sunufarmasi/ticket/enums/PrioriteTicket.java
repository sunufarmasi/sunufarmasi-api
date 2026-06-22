package sn.sunufarmasi.ticket.enums;

public enum PrioriteTicket {
    BASSE("Basse"),
    NORMALE("Normale"),
    HAUTE("Haute"),
    URGENTE("Urgente");

    private final String libelle;

    PrioriteTicket(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
