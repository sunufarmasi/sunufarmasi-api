package sn.sunufarmasi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration Firebase Admin SDK
 *
 * Pour configurer :
 * 1. Aller dans Firebase Console → Project Settings → Service accounts
 * 2. Générer une nouvelle clé privée (JSON)
 * 3. Placer le fichier dans src/main/resources/firebase-service-account.json
 *    OU définir FIREBASE_SERVICE_ACCOUNT_PATH dans les variables d'env
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @Bean
    public FirebaseApp firebaseApp() {
        // Si Firebase est déjà initialisé, retourner l'instance existante
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        try {
            InputStream serviceAccount = loadServiceAccount();
            if (serviceAccount == null) {
                log.warn("⚠️ Firebase non configuré - notifications push désactivées");
                log.warn("   Pour activer: définir firebase.service-account-path dans application.yml");
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("✅ Firebase initialisé avec succès");
            return app;

        } catch (IOException e) {
            log.error("❌ Erreur initialisation Firebase: {}", e.getMessage());
            log.warn("   Notifications push désactivées");
            return null;
        }
    }

    private InputStream loadServiceAccount() {
        // 1. Essayer le chemin configuré
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            try {
                return new FileInputStream(serviceAccountPath);
            } catch (IOException e) {
                log.warn("Fichier service account non trouvé: {}", serviceAccountPath);
            }
        }

        // 2. Essayer dans les ressources (classpath)
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");
        if (stream != null) {
            log.info("Firebase service account chargé depuis le classpath");
            return stream;
        }

        return null;
    }
}
