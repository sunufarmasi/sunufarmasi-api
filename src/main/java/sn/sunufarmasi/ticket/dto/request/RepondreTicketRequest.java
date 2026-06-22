package sn.sunufarmasi.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sn.sunufarmasi.ticket.enums.TicketStatut;

public record RepondreTicketRequest(
        @NotBlank(message = "La réponse est obligatoire")
        @Size(max = 5000, message = "Réponse trop longue (max 5000 caractères)")
        String reponse,

        TicketStatut nouveauStatut
) {}
