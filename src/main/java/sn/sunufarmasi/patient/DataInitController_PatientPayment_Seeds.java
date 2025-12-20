//// ═══════════════════════════════════════════════════════════════════════════════
//// SEEDS POUR PATIENT, SUBSCRIPTION, PAYMENT
//// À ajouter dans DataInitController.java
//// ═══════════════════════════════════════════════════════════════════════════════
//
//// IMPORTS À AJOUTER :
///*
//import sn.sunufarmasi.patient.entity.Patient;
//import sn.sunufarmasi.patient.repository.PatientRepository;
//import sn.sunufarmasi.payment.entity.Payment;
//import sn.sunufarmasi.payment.entity.PaymentMethod;
//import sn.sunufarmasi.payment.entity.PaymentStatus;
//import sn.sunufarmasi.payment.repository.PaymentRepository;
//import sn.sunufarmasi.subscription.entity.Subscription;
//import sn.sunufarmasi.subscription.entity.SubscriptionPlan;
//import sn.sunufarmasi.subscription.entity.SubscriptionStatus;
//import sn.sunufarmasi.subscription.repository.SubscriptionPlanRepository;
//import sn.sunufarmasi.subscription.repository.SubscriptionRepository;
//*/
//
//// INJECTIONS À AJOUTER dans le constructeur :
///*
//private final PatientRepository patientRepository;
//private final SubscriptionPlanRepository subscriptionPlanRepository;
//private final SubscriptionRepository subscriptionRepository;
//private final PaymentRepository paymentRepository;
//*/
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : SEED PLANS D'ABONNEMENT
//// ═══════════════════════════════════════════════════════════════════════════════
//
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PostMapping;
//
///**
// * POST /api/v1/init/seed-plans
// * Initialiser les plans d'abonnement
// */
//@PostMapping("/seed-plans")
//@Transactional
//@Operation(summary = "Initialiser les plans d'abonnement")
//public ResponseEntity<ApiResponse<Map<String, Object>>> seedSubscriptionPlans() {
//    log.info("POST /api/v1/init/seed-plans");
//
//    List<Map<String, Object>> plansCreated = new ArrayList<>();
//
//    // Plan 1 : Essai gratuit 15 jours
//    SubscriptionPlan freeTrial = subscriptionPlanRepository.findByCode("FREE_TRIAL")
//            .orElseGet(() -> {
//                SubscriptionPlan plan = SubscriptionPlan.builder()
//                        .code("FREE_TRIAL")
//                        .nom("Essai Gratuit")
//                        .description("Période d'essai de 15 jours pour découvrir SunuFarmasi. Accès complet à toutes les fonctionnalités.")
//                        .prix(0)
//                        .dureeJours(15)
//                        .avecPublicite(true)
//                        .actif(true)
//                        .ordre(1)
//                        .build();
//                return subscriptionPlanRepository.save(plan);
//            });
//    plansCreated.add(Map.of(
//            "code", freeTrial.getCode(),
//            "nom", freeTrial.getNom(),
//            "prix", freeTrial.getPrix(),
//            "dureeJours", freeTrial.getDureeJours()
//    ));
//
//    // Plan 2 : Mensuel 750 FCFA
//    SubscriptionPlan monthly = subscriptionPlanRepository.findByCode("MONTHLY")
//            .orElseGet(() -> {
//                SubscriptionPlan plan = SubscriptionPlan.builder()
//                        .code("MONTHLY")
//                        .nom("Abonnement Mensuel")
//                        .description("Abonnement mensuel avec publicités. Accès complet aux pharmacies de garde, recherche de médicaments, et favoris.")
//                        .prix(750)
//                        .dureeJours(30)
//                        .avecPublicite(true)
//                        .actif(true)
//                        .ordre(2)
//                        .build();
//                return subscriptionPlanRepository.save(plan);
//            });
//    plansCreated.add(Map.of(
//            "code", monthly.getCode(),
//            "nom", monthly.getNom(),
//            "prix", monthly.getPrix(),
//            "dureeJours", monthly.getDureeJours()
//    ));
//
//    // Plan 3 : Annuel 8000 FCFA
//    SubscriptionPlan annual = subscriptionPlanRepository.findByCode("ANNUAL")
//            .orElseGet(() -> {
//                SubscriptionPlan plan = SubscriptionPlan.builder()
//                        .code("ANNUAL")
//                        .nom("Abonnement Annuel")
//                        .description("Abonnement annuel SANS publicités. Économisez 1000 FCFA par rapport au mensuel. Accès VIP à toutes les fonctionnalités.")
//                        .prix(8000)
//                        .dureeJours(365)
//                        .avecPublicite(false)
//                        .actif(true)
//                        .ordre(3)
//                        .build();
//                return subscriptionPlanRepository.save(plan);
//            });
//    plansCreated.add(Map.of(
//            "code", annual.getCode(),
//            "nom", annual.getNom(),
//            "prix", annual.getPrix(),
//            "dureeJours", annual.getDureeJours()
//    ));
//
//    Map<String, Object> result = Map.of(
//            "message", "Plans d'abonnement initialisés",
//            "total", plansCreated.size(),
//            "plans", plansCreated
//    );
//
//    return ResponseEntity.ok(ApiResponse.success("Plans créés avec succès", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : SEED PATIENTS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * POST /api/v1/init/seed-patients
// * Initialiser des patients de test
// */
//@PostMapping("/seed-patients")
//@Transactional
//@Operation(summary = "Initialiser les patients de test")
//public ResponseEntity<ApiResponse<Map<String, Object>>> seedPatients() {
//    log.info("POST /api/v1/init/seed-patients");
//
//    // Récupérer quelques communes
//    List<Commune> communes = communeRepository.findAll();
//    if (communes.isEmpty()) {
//        return ResponseEntity.badRequest().body(
//                ApiResponse.error("Veuillez d'abord initialiser les localisations (seed-localisation)")
//        );
//    }
//
//    List<Map<String, Object>> patientsCreated = new ArrayList<>();
//
//    // Données des patients
//    List<Map<String, String>> patientsData = List.of(
//            Map.of("nom", "Amadou Diallo", "telephone", "+221771234501", "email", "amadou.diallo@gmail.com", "sexe", "M"),
//            Map.of("nom", "Fatou Ndiaye", "telephone", "+221771234502", "email", "fatou.ndiaye@gmail.com", "sexe", "F"),
//            Map.of("nom", "Moussa Sow", "telephone", "+221771234503", "email", "moussa.sow@gmail.com", "sexe", "M"),
//            Map.of("nom", "Aïssatou Ba", "telephone", "+221771234504", "email", "aissatou.ba@gmail.com", "sexe", "F"),
//            Map.of("nom", "Ibrahima Fall", "telephone", "+221771234505", "email", "ibrahima.fall@gmail.com", "sexe", "M"),
//            Map.of("nom", "Mariama Diop", "telephone", "+221771234506", "email", "mariama.diop@gmail.com", "sexe", "F"),
//            Map.of("nom", "Ousmane Mbaye", "telephone", "+221771234507", "email", "ousmane.mbaye@gmail.com", "sexe", "M"),
//            Map.of("nom", "Khady Sarr", "telephone", "+221771234508", "email", "khady.sarr@gmail.com", "sexe", "F"),
//            Map.of("nom", "Cheikh Gueye", "telephone", "+221771234509", "email", "cheikh.gueye@gmail.com", "sexe", "M"),
//            Map.of("nom", "Aminata Faye", "telephone", "+221771234510", "email", "aminata.faye@gmail.com", "sexe", "F")
//    );
//
//    int communeIndex = 0;
//    for (Map<String, String> data : patientsData) {
//        String telephone = data.get("telephone");
//
//        // Vérifier si le patient existe déjà
//        if (patientRepository.findByTelephone(telephone).isPresent()) {
//            log.info("Patient {} déjà existant", telephone);
//            continue;
//        }
//
//        Commune commune = communes.get(communeIndex % communes.size());
//        communeIndex++;
//
//        Patient patient = Patient.builder()
//                .nomComplet(data.get("nom"))
//                .telephone(telephone)
//                .email(data.get("email"))
//                .sexe(data.get("sexe"))
//                .dateNaissance(LocalDate.of(1985 + (communeIndex % 20), (communeIndex % 12) + 1, (communeIndex % 28) + 1))
//                .adresse("Quartier " + commune.getNom())
//                .commune(commune)
//                .emailVerified(true)
//                .telephoneVerified(true)
//                .actif(true)
//                .build();
//
//        patient = patientRepository.save(patient);
//
//        patientsCreated.add(Map.of(
//                "id", patient.getId(),
//                "nom", patient.getNomComplet(),
//                "telephone", patient.getTelephone(),
//                "email", patient.getEmail() != null ? patient.getEmail() : "",
//                "commune", commune.getNom()
//        ));
//
//        log.info("Patient créé: {} - {}", patient.getNomComplet(), patient.getTelephone());
//    }
//
//    Map<String, Object> result = Map.of(
//            "message", "Patients initialisés",
//            "total", patientsCreated.size(),
//            "patients", patientsCreated
//    );
//
//    return ResponseEntity.ok(ApiResponse.success("Patients créés avec succès", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : SEED SUBSCRIPTIONS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * POST /api/v1/init/seed-subscriptions
// * Initialiser des abonnements pour les patients
// */
//@PostMapping("/seed-subscriptions")
//@Transactional
//@Operation(summary = "Initialiser les abonnements de test")
//public ResponseEntity<ApiResponse<Map<String, Object>>> seedSubscriptions() {
//    log.info("POST /api/v1/init/seed-subscriptions");
//
//    // Vérifier les prérequis
//    List<Patient> patients = patientRepository.findAll();
//    if (patients.isEmpty()) {
//        return ResponseEntity.badRequest().body(
//                ApiResponse.error("Veuillez d'abord initialiser les patients (seed-patients)")
//        );
//    }
//
//    List<SubscriptionPlan> plans = subscriptionPlanRepository.findByActifTrueOrderByOrdreAsc();
//    if (plans.isEmpty()) {
//        return ResponseEntity.badRequest().body(
//                ApiResponse.error("Veuillez d'abord initialiser les plans (seed-plans)")
//        );
//    }
//
//    List<Map<String, Object>> subscriptionsCreated = new ArrayList<>();
//    LocalDateTime now = LocalDateTime.now();
//
//    int planIndex = 0;
//    for (Patient patient : patients) {
//        // Vérifier si le patient a déjà un abonnement actif
//        if (subscriptionRepository.findByPatientIdAndStatus(patient.getId(), SubscriptionStatus.ACTIVE).isPresent()) {
//            log.info("Patient {} a déjà un abonnement actif", patient.getNomComplet());
//            continue;
//        }
//
//        // Attribuer un plan différent à chaque patient
//        SubscriptionPlan plan = plans.get(planIndex % plans.size());
//        planIndex++;
//
//        // Déterminer les dates
//        LocalDateTime startsAt = now.minusDays(planIndex * 2L); // Décalage pour variété
//        LocalDateTime expiresAt = startsAt.plusDays(plan.getDureeJours());
//
//        // Déterminer le statut en fonction des dates
//        SubscriptionStatus status;
//        if (expiresAt.isBefore(now)) {
//            status = SubscriptionStatus.EXPIRED;
//        } else {
//            status = SubscriptionStatus.ACTIVE;
//        }
//
//        Subscription subscription = Subscription.builder()
//                .patient(patient)
//                .plan(plan)
//                .status(status)
//                .startsAt(startsAt)
//                .expiresAt(expiresAt)
//                .autoRenew(plan.isAnnual()) // Auto-renew pour annuel
//                .isTrial(plan.isFreeTrial())
//                .build();
//
//        subscription = subscriptionRepository.save(subscription);
//
//        subscriptionsCreated.add(Map.of(
//                "id", subscription.getId(),
//                "patient", patient.getNomComplet(),
//                "plan", plan.getNom(),
//                "status", subscription.getStatus().name(),
//                "startsAt", subscription.getStartsAt().toString(),
//                "expiresAt", subscription.getExpiresAt().toString(),
//                "daysRemaining", subscription.getDaysRemaining()
//        ));
//
//        log.info("Abonnement créé: {} - {} ({})",
//                patient.getNomComplet(), plan.getNom(), status);
//    }
//
//    Map<String, Object> result = Map.of(
//            "message", "Abonnements initialisés",
//            "total", subscriptionsCreated.size(),
//            "subscriptions", subscriptionsCreated
//    );
//
//    return ResponseEntity.ok(ApiResponse.success("Abonnements créés avec succès", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : SEED PAYMENTS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * POST /api/v1/init/seed-payments
// * Initialiser des paiements de test
// */
//@PostMapping("/seed-payments")
//@Transactional
//@Operation(summary = "Initialiser les paiements de test")
//public ResponseEntity<ApiResponse<Map<String, Object>>> seedPayments() {
//    log.info("POST /api/v1/init/seed-payments");
//
//    // Récupérer les abonnements payants (pas les essais gratuits)
//    List<Subscription> paidSubscriptions = subscriptionRepository.findAll().stream()
//            .filter(s -> !s.isTrial() && s.getPlan().getPrix() > 0)
//            .toList();
//
//    if (paidSubscriptions.isEmpty()) {
//        return ResponseEntity.badRequest().body(
//                ApiResponse.error("Veuillez d'abord initialiser les abonnements (seed-subscriptions)")
//        );
//    }
//
//    List<Map<String, Object>> paymentsCreated = new ArrayList<>();
//    PaymentMethod[] methods = {PaymentMethod.ORANGE_MONEY, PaymentMethod.WAVE, PaymentMethod.FREE_MONEY};
//
//    int methodIndex = 0;
//    for (Subscription subscription : paidSubscriptions) {
//        // Vérifier si un paiement existe déjà pour cet abonnement
//        if (!paymentRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscription.getId()).isEmpty()) {
//            log.info("Paiement déjà existant pour l'abonnement {}", subscription.getId());
//            continue;
//        }
//
//        PaymentMethod method = methods[methodIndex % methods.length];
//        methodIndex++;
//
//        // Créer le paiement réussi
//        Payment payment = Payment.builder()
//                .patient(subscription.getPatient())
//                .subscription(subscription)
//                .montant(subscription.getPlan().getPrix())
//                .methode(method)
//                .status(PaymentStatus.SUCCESS)
//                .telephonePaiement(subscription.getPatient().getTelephone())
//                .referenceInterne(Payment.generateReferenceInterne())
//                .referenceExterne(generateExternalReference(method))
//                .paidAt(subscription.getStartsAt())
//                .build();
//
//        payment = paymentRepository.save(payment);
//
//        paymentsCreated.add(Map.of(
//                "id", payment.getId(),
//                "patient", subscription.getPatient().getNomComplet(),
//                "montant", payment.getMontant() + " FCFA",
//                "methode", payment.getMethode().name(),
//                "status", payment.getStatus().name(),
//                "referenceInterne", payment.getReferenceInterne(),
//                "referenceExterne", payment.getReferenceExterne()
//        ));
//
//        log.info("Paiement créé: {} - {} FCFA via {}",
//                subscription.getPatient().getNomComplet(),
//                payment.getMontant(),
//                method);
//    }
//
//    // Ajouter quelques paiements échoués pour le réalisme
//    List<Patient> patients = patientRepository.findAll();
//    if (!patients.isEmpty()) {
//        Patient patientWithFailedPayment = patients.get(0);
//
//        Payment failedPayment = Payment.builder()
//                .patient(patientWithFailedPayment)
//                .subscription(null)
//                .montant(750)
//                .methode(PaymentMethod.ORANGE_MONEY)
//                .status(PaymentStatus.FAILED)
//                .telephonePaiement(patientWithFailedPayment.getTelephone())
//                .referenceInterne(Payment.generateReferenceInterne())
//                .errorMessage("Solde insuffisant")
//                .build();
//
//        failedPayment = paymentRepository.save(failedPayment);
//
//        paymentsCreated.add(Map.of(
//                "id", failedPayment.getId(),
//                "patient", patientWithFailedPayment.getNomComplet(),
//                "montant", failedPayment.getMontant() + " FCFA",
//                "methode", failedPayment.getMethode().name(),
//                "status", failedPayment.getStatus().name(),
//                "referenceInterne", failedPayment.getReferenceInterne(),
//                "error", failedPayment.getErrorMessage()
//        ));
//
//        log.info("Paiement échoué créé pour: {}", patientWithFailedPayment.getNomComplet());
//    }
//
//    Map<String, Object> result = Map.of(
//            "message", "Paiements initialisés",
//            "total", paymentsCreated.size(),
//            "payments", paymentsCreated
//    );
//
//    return ResponseEntity.ok(ApiResponse.success("Paiements créés avec succès", result));
//}
//
///**
// * Générer une référence externe simulée
// */
//private String generateExternalReference(PaymentMethod method) {
//    String prefix = switch (method) {
//        case ORANGE_MONEY -> "OM";
//        case WAVE -> "WV";
//        case FREE_MONEY -> "FM";
//        default -> "XX";
//    };
//    return prefix + "-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : GET PATIENTS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * GET /api/v1/init/patients-list
// * Liste des patients
// */
//@GetMapping("/patients-list")
//@Transactional(readOnly = true)
//@Operation(summary = "Lister les patients")
//public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPatients() {
//    log.info("GET /api/v1/init/patients-list");
//
//    List<Patient> patients = patientRepository.findAll();
//
//    List<Map<String, Object>> result = patients.stream()
//            .map(p -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("id", p.getId());
//                map.put("nomComplet", p.getNomComplet());
//                map.put("telephone", p.getTelephone());
//                map.put("email", p.getEmail());
//                map.put("sexe", p.getSexe());
//                map.put("dateNaissance", p.getDateNaissance());
//                map.put("commune", p.getCommune() != null ? p.getCommune().getNom() : null);
//                map.put("actif", p.isActif());
//                map.put("emailVerified", p.isEmailVerified());
//                map.put("telephoneVerified", p.isTelephoneVerified());
//                map.put("createdAt", p.getCreatedAt());
//                return map;
//            })
//            .toList();
//
//    return ResponseEntity.ok(ApiResponse.success("Liste des patients", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : GET SUBSCRIPTIONS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * GET /api/v1/init/subscriptions-list
// * Liste des abonnements
// */
//@GetMapping("/subscriptions-list")
//@Transactional(readOnly = true)
//@Operation(summary = "Lister les abonnements")
//public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listSubscriptions() {
//    log.info("GET /api/v1/init/subscriptions-list");
//
//    List<Subscription> subscriptions = subscriptionRepository.findAll();
//
//    List<Map<String, Object>> result = subscriptions.stream()
//            .map(s -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("id", s.getId());
//                map.put("patient", s.getPatient().getNomComplet());
//                map.put("patientId", s.getPatient().getId());
//                map.put("plan", s.getPlan().getNom());
//                map.put("planCode", s.getPlan().getCode());
//                map.put("prix", s.getPlan().getPrix());
//                map.put("status", s.getStatus().name());
//                map.put("startsAt", s.getStartsAt());
//                map.put("expiresAt", s.getExpiresAt());
//                map.put("daysRemaining", s.getDaysRemaining());
//                map.put("isTrial", s.isTrial());
//                map.put("autoRenew", s.isAutoRenew());
//                map.put("isActive", s.isActive());
//                map.put("isExpired", s.isExpired());
//                return map;
//            })
//            .toList();
//
//    return ResponseEntity.ok(ApiResponse.success("Liste des abonnements", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : GET PAYMENTS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * GET /api/v1/init/payments-list
// * Liste des paiements
// */
//@GetMapping("/payments-list")
//@Transactional(readOnly = true)
//@Operation(summary = "Lister les paiements")
//public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPayments() {
//    log.info("GET /api/v1/init/payments-list");
//
//    List<Payment> payments = paymentRepository.findAll();
//
//    List<Map<String, Object>> result = payments.stream()
//            .map(p -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("id", p.getId());
//                map.put("patient", p.getPatient().getNomComplet());
//                map.put("patientId", p.getPatient().getId());
//                map.put("montant", p.getMontant());
//                map.put("methode", p.getMethode().name());
//                map.put("status", p.getStatus().name());
//                map.put("referenceInterne", p.getReferenceInterne());
//                map.put("referenceExterne", p.getReferenceExterne());
//                map.put("telephonePaiement", p.getTelephonePaiement());
//                map.put("paidAt", p.getPaidAt());
//                map.put("errorMessage", p.getErrorMessage());
//                map.put("createdAt", p.getCreatedAt());
//                if (p.getSubscription() != null) {
//                    map.put("subscriptionId", p.getSubscription().getId());
//                    map.put("planNom", p.getSubscription().getPlan().getNom());
//                }
//                return map;
//            })
//            .toList();
//
//    return ResponseEntity.ok(ApiResponse.success("Liste des paiements", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// ENDPOINT : GET PLANS
//// ═══════════════════════════════════════════════════════════════════════════════
//
///**
// * GET /api/v1/init/plans-list
// * Liste des plans d'abonnement
// */
//@GetMapping("/plans-list")
//@Transactional(readOnly = true)
//@Operation(summary = "Lister les plans d'abonnement")
//public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPlans() {
//    log.info("GET /api/v1/init/plans-list");
//
//    List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
//
//    List<Map<String, Object>> result = plans.stream()
//            .map(p -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("id", p.getId());
//                map.put("code", p.getCode());
//                map.put("nom", p.getNom());
//                map.put("description", p.getDescription());
//                map.put("prix", p.getPrix());
//                map.put("dureeJours", p.getDureeJours());
//                map.put("avecPublicite", p.isAvecPublicite());
//                map.put("actif", p.isActif());
//                map.put("ordre", p.getOrdre());
//                map.put("prixParJour", Math.round(p.getPrixParJour() * 100.0) / 100.0);
//                return map;
//            })
//            .toList();
//
//    return ResponseEntity.ok(ApiResponse.success("Liste des plans", result));
//}
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// MISE À JOUR DE SEED-ALL
//// ═══════════════════════════════════════════════════════════════════════════════
//
//// Ajouter dans la méthode seedAll() après les autres seeds :
///*
//    // 9. Plans d'abonnement
//    seedSubscriptionPlans();
//
//    // 10. Patients
//    seedPatients();
//
//    // 11. Abonnements
//    seedSubscriptions();
//
//    // 12. Paiements
//    seedPayments();
//*/
//
//
//// ═══════════════════════════════════════════════════════════════════════════════
//// MISE À JOUR DE STATS
//// ═══════════════════════════════════════════════════════════════════════════════
//
//// Ajouter dans la méthode stats() :
///*
//    stats.put("subscription_plans", subscriptionPlanRepository.count());
//    stats.put("patients", patientRepository.count());
//    stats.put("subscriptions", subscriptionRepository.count());
//    stats.put("payments", paymentRepository.count());
//*/
