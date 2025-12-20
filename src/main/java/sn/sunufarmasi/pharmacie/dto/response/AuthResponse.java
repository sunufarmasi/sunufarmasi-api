package sn.sunufarmasi.pharmacie.dto.response;

/**
 * DTO Response pour l'authentification
 *
 * @author WeCan
 * @since 1.0.0
 */
public record AuthResponse(
        String token,
        String type,
        PharmacienResponse pharmacien
) {
    /**
     * Constructeur avec type par défaut
     */
    public AuthResponse(String token, PharmacienResponse pharmacien) {
        this(token, "Bearer", pharmacien);
    }
}