//package sn.sunufarmasi;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class SunuFarmasiApiApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(SunuFarmasiApiApplication.class, args);
//	}
//
//}


package sn.sunufarmasi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Application principale PharmaGo
 *
 * Plateforme de localisation de pharmacies au Sénégal
 * avec recherche GPS, commandes en ligne et gestion de mutuelles
 *
 * @author AL Amine
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
@EnableJpaAuditing
public class SunuFarmasiApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SunuFarmasiApiApplication.class, args);

        System.out.println("""
            
            ╔══════════════════════════════════════════════════════╗
            ║                                                      ║
            ║            🏥 SunuFarmasi BACKEND API 🏥            ║
            ║                                                      ║
            ║  Version: 1.0.0                                      ║
            ║  Port: 8080                                          ║
            ║  Swagger: http://localhost:8080/swagger-ui.html      ║
            ║  API Docs: http://localhost:8080/api-docs            ║
            ║                                                      ║
            ║  Status: ✅ RUNNING                                  ║
            ║                                                      ║
            ╚══════════════════════════════════════════════════════╝
            
            """);
    }
}