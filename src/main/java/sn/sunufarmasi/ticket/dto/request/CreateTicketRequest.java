package sn.sunufarmasi.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sn.sunufarmasi.ticket.enums.PrioriteTicket;

public record CreateTicketRequest(
        @NotBlank(message = "Le sujet est obligatoire")
        @Size(max = 200, message = "Sujet trop long (max 200 caractères)")
        String sujet,

        @NotBlank(message = "Le message est obligatoire")
        @Size(max = 5000, message = "Message trop long (max 5000 caractères)")
        String message,

        PrioriteTicket priorite,

        String categorie,

        String pieceJointeNom,   // Optionnel : nom du fichier joint

        String pieceJointeUrl    // Optionnel : URL du fichier joint
) {}
