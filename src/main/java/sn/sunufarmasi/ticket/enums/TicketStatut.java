package sn.sunufarmasi.ticket.enums;

public enum TicketStatut {
    OUVERT("Ouvert"),
    EN_COURS("En cours de traitement"),
    RESOLU("Résolu"),
    FERME("Fermé");

    private final String libelle;

    TicketStatut(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
