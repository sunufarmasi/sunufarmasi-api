////package sn.sunufarmasi.security;
////
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.security.core.GrantedAuthority;
////import org.springframework.security.core.authority.SimpleGrantedAuthority;
////import org.springframework.security.core.userdetails.User;
////import org.springframework.security.core.userdetails.UserDetails;
////import org.springframework.security.core.userdetails.UserDetailsService;
////import org.springframework.security.core.userdetails.UsernameNotFoundException;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.util.ArrayList;
////import java.util.List;
////
/////**
//// * Service personnalisé pour charger les détails des utilisateurs
//// * Implémente UserDetailsService de Spring Security
//// *
//// * TODO: À implémenter après la création des repositories Patient, Pharmacien, Admin
//// *
//// * @author WeCan
//// * @since 1.0.0
//// */
////@Service
////@RequiredArgsConstructor
////@Slf4j
////public class CustomUserDetailsService implements UserDetailsService {
////
////    // TODO: Injecter les repositories ici
////    // private final PatientRepository patientRepository;
////    // private final PharmacienRepository pharmacienRepository;
////    // private final AdminRepository adminRepository;
////
////    /**
////     * Charger un utilisateur par son email
////     * Recherche dans Patient, Pharmacien, Admin
////     *
////     * @param email Email de l'utilisateur
////     * @return UserDetails
////     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
////     */
////    @Override
////    @Transactional(readOnly = true)
////    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
////        log.debug("Chargement de l'utilisateur: {}", email);
////
////        // TODO: Implémenter la recherche réelle après création des repositories
////
////        /*
////        // 1. Chercher dans Patient
////        Optional<Patient> patient = patientRepository.findByEmail(email);
////        if (patient.isPresent()) {
////            return buildUserDetails(
////                patient.get().getEmail(),
////                patient.get().getMotDePasseHash(),
////                AppConstants.ROLE_PATIENT,
////                patient.get().isActif(),
////                patient.get().isCompteVerifie()
////            );
////        }
////
////        // 2. Chercher dans Pharmacien
////        Optional<Pharmacien> pharmacien = pharmacienRepository.findByEmail(email);
////        if (pharmacien.isPresent()) {
////            return buildUserDetails(
////                pharmacien.get().getEmail(),
////                pharmacien.get().getMotDePasseHash(),
////                AppConstants.ROLE_PHARMACIEN,
////                pharmacien.get().isActif(),
////                true  // Les pharmaciens n'ont pas de vérification OTP
////            );
////        }
////
////        // 3. Chercher dans Admin
////        Optional<Admin> admin = adminRepository.findByEmail(email);
////        if (admin.isPresent()) {
////            return buildUserDetails(
////                admin.get().getEmail(),
////                admin.get().getMotDePasseHash(),
////                admin.get().getRole(),  // ADMIN ou SUPER_ADMIN
////                admin.get().isActif(),
////                true
////            );
////        }
////        */
////
////        // Pour le moment, retourner une exception
////        log.error("Utilisateur non trouvé: {}", email);
////        throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
////    }
////
////    /**
////     * Construire un UserDetails à partir des informations utilisateur
////     *
////     * @param email Email
////     * @param passwordHash Mot de passe hashé
////     * @param role Rôle (PATIENT, PHARMACIEN, ADMIN, etc.)
////     * @param isActive Compte actif
////     * @param isVerified Compte vérifié
////     * @return UserDetails
////     */
////    private UserDetails buildUserDetails(
////            String email,
////            String passwordHash,
////            String role,
////            boolean isActive,
////            boolean isVerified
////    ) {
////        // Vérifications
////        if (!isActive) {
////            throw new UsernameNotFoundException("Compte désactivé");
////        }
////
////        if (!isVerified) {
////            throw new UsernameNotFoundException("Compte non vérifié");
////        }
////
////        // Créer les autorités (rôles)
////        List<GrantedAuthority> authorities = new ArrayList<>();
////        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
////
////        // Créer UserDetails
////        return User.builder()
////            .username(email)
////            .password(passwordHash)
////            .authorities(authorities)
////            .accountExpired(false)
////            .accountLocked(false)
////            .credentialsExpired(false)
////            .disabled(!isActive)
////            .build();
////    }
////
////    /**
////     * Construire un UserDetails simple (pour tests)
////     *
////     * @param email Email
////     * @param passwordHash Mot de passe hashé
////     * @param role Rôle
////     * @return UserDetails
////     */
////    public UserDetails buildSimpleUserDetails(String email, String passwordHash, String role) {
////        return buildUserDetails(email, passwordHash, role, true, true);
////    }
////}
//
//
//
//package sn.sunufarmasi.security;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import sn.sunufarmasi.patient.entity.Patient;
//import sn.sunufarmasi.patient.repository.PatientRepository;
//import sn.sunufarmasi.shared.constant.AppConstants;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Service personnalisé pour charger les détails des utilisateurs
// * Implémente UserDetailsService de Spring Security
// *
// * @author WeCan
// * @since 1.0.0
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final PatientRepository patientRepository;
//
//    // TODO: Ajouter quand disponibles
//    // private final PharmacienRepository pharmacienRepository;
//    // private final AdminRepository adminRepository;
//    // private final SyndicatRepository syndicatRepository;
//
//    /**
//     * Charger un utilisateur par son email
//     * Recherche dans Patient, Pharmacien, Admin, Syndicat
//     *
//     * @param email Email de l'utilisateur
//     * @return UserDetails
//     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        log.debug("Chargement de l'utilisateur: {}", email);
//
//        // 1. Chercher dans Patient
//        Optional<Patient> patient = patientRepository.findByEmail(email);
//        if (patient.isPresent()) {
//            return buildUserDetailsFromPatient(patient.get());
//        }
//
//        // TODO: 2. Chercher dans Pharmacien
//        /*
//        Optional<Pharmacien> pharmacien = pharmacienRepository.findByEmail(email);
//        if (pharmacien.isPresent()) {
//            return buildUserDetails(
//                pharmacien.get().getEmail(),
//                pharmacien.get().getMotDePasseHash(),
//                AppConstants.ROLE_PHARMACIEN,
//                pharmacien.get().isActif(),
//                true  // Les pharmaciens n'ont pas de vérification OTP
//            );
//        }
//        */
//
//        // TODO: 3. Chercher dans Admin
//        /*
//        Optional<Admin> admin = adminRepository.findByEmail(email);
//        if (admin.isPresent()) {
//            return buildUserDetails(
//                admin.get().getEmail(),
//                admin.get().getMotDePasseHash(),
//                admin.get().getRole(),  // ADMIN ou SUPER_ADMIN
//                admin.get().isActif(),
//                true
//            );
//        }
//        */
//
//        // TODO: 4. Chercher dans Syndicat
//        /*
//        Optional<Syndicat> syndicat = syndicatRepository.findByEmail(email);
//        if (syndicat.isPresent()) {
//            return buildUserDetails(
//                syndicat.get().getEmail(),
//                syndicat.get().getMotDePasseHash(),
//                AppConstants.ROLE_SYNDICAT,
//                syndicat.get().isActif(),
//                true
//            );
//        }
//        */
//
//        // Utilisateur non trouvé
//        log.error("Utilisateur non trouvé: {}", email);
//        throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
//    }
//
//    /**
//     * Construire UserDetails à partir d'un Patient
//     *
//     * @param patient Entity Patient
//     * @return UserDetails
//     */
//    private UserDetails buildUserDetailsFromPatient(Patient patient) {
//        // Vérifications
//        if (!patient.isActif()) {
//            throw new UsernameNotFoundException("Compte désactivé");
//        }
//
//        if (!patient.isCompteVerifie()) {
//            throw new UsernameNotFoundException("Compte non vérifié");
//        }
//
//        return buildUserDetails(
//                patient.getEmail(),
//                patient.getMotDePasseHash(),
//                AppConstants.ROLE_PATIENT,
//                patient.isActif(),
//                patient.isCompteVerifie()
//        );
//    }
//
//    /**
//     * Construire un UserDetails à partir des informations utilisateur
//     *
//     * @param email Email
//     * @param passwordHash Mot de passe hashé
//     * @param role Rôle (PATIENT, PHARMACIEN, ADMIN, etc.)
//     * @param isActive Compte actif
//     * @param isVerified Compte vérifié
//     * @return UserDetails
//     */
//    private UserDetails buildUserDetails(
//            String email,
//            String passwordHash,
//            String role,
//            boolean isActive,
//            boolean isVerified
//    ) {
//        // Créer les autorités (rôles)
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//
//        // Créer UserDetails
//        return User.builder()
//                .username(email)
//                .password(passwordHash)
//                .authorities(authorities)
//                .accountExpired(false)
//                .accountLocked(false)
//                .credentialsExpired(false)
//                .disabled(!isActive)
//                .build();
//    }
//}