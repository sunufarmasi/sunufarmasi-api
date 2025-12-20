package sn.sunufarmasi.shared.util;

import sn.sunufarmasi.shared.constant.AppConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilitaires de manipulation de dates et heures
 * 
 * @author WeCan
 * @since 1.0.0
 */
public final class DateUtil {
    
    private DateUtil() {
        // Classe utilitaire - constructeur privé
    }
    
    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern(AppConstants.DATETIME_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT);
    
    // Timezone
    private static final ZoneId DAKAR_ZONE = ZoneId.of(AppConstants.TIMEZONE_DAKAR);
    
    // ═══════════════════════════════════════════════════════════
    // DATE/HEURE ACTUELLE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Obtenir la date/heure actuelle (timezone Dakar)
     * 
     * @return LocalDateTime actuel
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(DAKAR_ZONE);
    }
    
    /**
     * Obtenir la date actuelle (timezone Dakar)
     * 
     * @return LocalDate actuel
     */
    public static LocalDate today() {
        return LocalDate.now(DAKAR_ZONE);
    }
    
    /**
     * Obtenir l'heure actuelle (timezone Dakar)
     * 
     * @return LocalTime actuel
     */
    public static LocalTime currentTime() {
        return LocalTime.now(DAKAR_ZONE);
    }
    
    /**
     * Obtenir le timestamp actuel (epoch milliseconds)
     * 
     * @return Timestamp en millisecondes
     */
    public static long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
    
    // ═══════════════════════════════════════════════════════════
    // FORMATAGE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Formater une date en chaîne
     * Format: yyyy-MM-dd
     * 
     * @param date Date à formater
     * @return Date formatée
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Formater une date/heure en chaîne
     * Format: yyyy-MM-dd'T'HH:mm:ss
     * 
     * @param dateTime Date/heure à formater
     * @return Date/heure formatée
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    /**
     * Formater une heure en chaîne
     * Format: HH:mm
     * 
     * @param time Heure à formater
     * @return Heure formatée
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }
    
    /**
     * Formater une date en français
     * Exemple: "15 janvier 2025"
     * 
     * @param date Date à formater
     * @return Date formatée en français
     */
    public static String formatDateFrench(LocalDate date) {
        if (date == null) {
            return null;
        }
        
        DateTimeFormatter frenchFormatter = DateTimeFormatter
            .ofPattern("dd MMMM yyyy")
            .withZone(DAKAR_ZONE);
        
        return date.format(frenchFormatter);
    }
    
    // ═══════════════════════════════════════════════════════════
    // PARSING
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Parser une chaîne en LocalDate
     * Format attendu: yyyy-MM-dd
     * 
     * @param dateStr Chaîne à parser
     * @return LocalDate ou null si invalide
     */
    public static LocalDate parseDate(String dateStr) {
        if (StringUtil.isBlank(dateStr)) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parser une chaîne en LocalDateTime
     * Format attendu: yyyy-MM-dd'T'HH:mm:ss
     * 
     * @param dateTimeStr Chaîne à parser
     * @return LocalDateTime ou null si invalide
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (StringUtil.isBlank(dateTimeStr)) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parser une chaîne en LocalTime
     * Format attendu: HH:mm
     * 
     * @param timeStr Chaîne à parser
     * @return LocalTime ou null si invalide
     */
    public static LocalTime parseTime(String timeStr) {
        if (StringUtil.isBlank(timeStr)) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // CALCULS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Calculer la différence en jours entre deux dates
     * 
     * @param date1 Première date
     * @param date2 Deuxième date
     * @return Nombre de jours
     */
    public static long daysBetween(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(date1, date2);
    }
    
    /**
     * Calculer la différence en heures entre deux dates/heures
     * 
     * @param dateTime1 Première date/heure
     * @param dateTime2 Deuxième date/heure
     * @return Nombre d'heures
     */
    public static long hoursBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(dateTime1, dateTime2);
    }
    
    /**
     * Calculer la différence en minutes entre deux dates/heures
     * 
     * @param dateTime1 Première date/heure
     * @param dateTime2 Deuxième date/heure
     * @return Nombre de minutes
     */
    public static long minutesBetween(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(dateTime1, dateTime2);
    }
    
    /**
     * Calculer l'âge à partir d'une date de naissance
     * 
     * @param birthDate Date de naissance
     * @return Âge en années
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, today()).getYears();
    }
    
    /**
     * Ajouter des jours à une date
     * 
     * @param date Date de base
     * @param days Nombre de jours à ajouter
     * @return Nouvelle date
     */
    public static LocalDate addDays(LocalDate date, int days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }
    
    /**
     * Ajouter des mois à une date
     * 
     * @param date Date de base
     * @param months Nombre de mois à ajouter
     * @return Nouvelle date
     */
    public static LocalDate addMonths(LocalDate date, int months) {
        if (date == null) {
            return null;
        }
        return date.plusMonths(months);
    }
    
    /**
     * Ajouter des années à une date
     * 
     * @param date Date de base
     * @param years Nombre d'années à ajouter
     * @return Nouvelle date
     */
    public static LocalDate addYears(LocalDate date, int years) {
        if (date == null) {
            return null;
        }
        return date.plusYears(years);
    }
    
    /**
     * Ajouter des heures à une date/heure
     * 
     * @param dateTime Date/heure de base
     * @param hours Nombre d'heures à ajouter
     * @return Nouvelle date/heure
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }
    
    /**
     * Ajouter des minutes à une date/heure
     * 
     * @param dateTime Date/heure de base
     * @param minutes Nombre de minutes à ajouter
     * @return Nouvelle date/heure
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }
    
    // ═══════════════════════════════════════════════════════════
    // COMPARAISONS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Vérifier si une date est dans le passé
     * 
     * @param date Date à vérifier
     * @return true si dans le passé
     */
    public static boolean isInPast(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(today());
    }
    
    /**
     * Vérifier si une date est dans le futur
     * 
     * @param date Date à vérifier
     * @return true si dans le futur
     */
    public static boolean isInFuture(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(today());
    }
    
    /**
     * Vérifier si une date/heure est expirée
     * 
     * @param dateTime Date/heure à vérifier
     * @return true si expirée
     */
    public static boolean isExpired(LocalDateTime dateTime) {
        if (dateTime == null) {
            return true;
        }
        return dateTime.isBefore(now());
    }
    
    /**
     * Vérifier si une date est aujourd'hui
     * 
     * @param date Date à vérifier
     * @return true si aujourd'hui
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(today());
    }
    
    /**
     * Vérifier si deux dates sont le même jour
     * 
     * @param date1 Première date
     * @param date2 Deuxième date
     * @return true si même jour
     */
    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }
    
    // ═══════════════════════════════════════════════════════════
    // DÉBUT/FIN DE PÉRIODE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Obtenir le début de la journée (00:00:00)
     * 
     * @param date Date
     * @return LocalDateTime au début de la journée
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }
    
    /**
     * Obtenir la fin de la journée (23:59:59)
     * 
     * @param date Date
     * @return LocalDateTime à la fin de la journée
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }
    
    /**
     * Obtenir le début du mois
     * 
     * @param date Date de référence
     * @return Premier jour du mois
     */
    public static LocalDate startOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }
    
    /**
     * Obtenir la fin du mois
     * 
     * @param date Date de référence
     * @return Dernier jour du mois
     */
    public static LocalDate endOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONVERSION
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Convertir LocalDateTime en timestamp (epoch millis)
     * 
     * @param dateTime Date/heure à convertir
     * @return Timestamp en millisecondes
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0;
        }
        return dateTime.atZone(DAKAR_ZONE).toInstant().toEpochMilli();
    }
    
    /**
     * Convertir timestamp en LocalDateTime
     * 
     * @param timestamp Timestamp en millisecondes
     * @return LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            DAKAR_ZONE
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // AFFICHAGE RELATIF
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Obtenir une représentation relative d'une date/heure
     * Exemples: "il y a 2 minutes", "il y a 3 heures", "il y a 2 jours"
     * 
     * @param dateTime Date/heure à comparer
     * @return Chaîne relative
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        long minutes = minutesBetween(dateTime, now());
        
        if (minutes < 1) {
            return "à l'instant";
        } else if (minutes < 60) {
            return "il y a " + minutes + " minute" + (minutes > 1 ? "s" : "");
        }
        
        long hours = minutes / 60;
        if (hours < 24) {
            return "il y a " + hours + " heure" + (hours > 1 ? "s" : "");
        }
        
        long days = hours / 24;
        if (days < 7) {
            return "il y a " + days + " jour" + (days > 1 ? "s" : "");
        }
        
        if (days < 30) {
            long weeks = days / 7;
            return "il y a " + weeks + " semaine" + (weeks > 1 ? "s" : "");
        }
        
        if (days < 365) {
            long months = days / 30;
            return "il y a " + months + " mois";
        }
        
        long years = days / 365;
        return "il y a " + years + " an" + (years > 1 ? "s" : "");
    }
}
