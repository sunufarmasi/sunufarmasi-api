package sn.sunufarmasi.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception levée lorsqu'une ressource demandée n'est pas trouvée
 * Retourne un code HTTP 404 (NOT_FOUND)
 *
 * @author AL Amine
 * @since 1.0.0
 */
public class ResourceNotFoundException extends ApiException {

    /**
     * Constructeur avec message simple
     *
     * @param message Message d'erreur
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    /**
     * Constructeur avec type de ressource et ID
     *
     * @param resourceName Nom de la ressource (ex: "Patient", "Pharmacie")
     * @param fieldName Nom du champ (ex: "id", "email")
     * @param fieldValue Valeur du champ
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s non trouvé(e) avec %s : '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND"
        );
    }

    /**
     * Constructeur pour ressource non trouvée par ID
     *
     * @param resourceName Nom de la ressource
     * @param id ID de la ressource
     */
    public static ResourceNotFoundException byId(String resourceName, Object id) {
        return new ResourceNotFoundException(resourceName, "id", id);
    }

    /**
     * Constructeur pour ressource non trouvée par email
     *
     * @param resourceName Nom de la ressource
     * @param email Email
     */
    public static ResourceNotFoundException byEmail(String resourceName, String email) {
        return new ResourceNotFoundException(resourceName, "email", email);
    }
}