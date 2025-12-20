package sn.sunufarmasi.shared.util;

import sn.sunufarmasi.shared.constant.AppConstants;

/**
 * Utilitaires pour les calculs géographiques (distance, proximité, etc.)
 * 
 * @author WeCan
 * @since 1.0.0
 */
public final class GeoUtil {
    
    private GeoUtil() {
        // Classe utilitaire - constructeur privé
    }
    
    // Rayon de la Terre en kilomètres
    private static final double EARTH_RADIUS_KM = AppConstants.EARTH_RADIUS_KM;
    
    // ═══════════════════════════════════════════════════════════
    // CALCUL DE DISTANCE
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Calculer la distance entre deux points GPS (formule de Haversine)
     * 
     * @param lat1 Latitude du point 1 (en degrés)
     * @param lon1 Longitude du point 1 (en degrés)
     * @param lat2 Latitude du point 2 (en degrés)
     * @param lon2 Longitude du point 2 (en degrés)
     * @return Distance en kilomètres
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Conversion en radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Différences
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        // Formule de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Distance en km
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Calculer la distance en mètres
     * 
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return Distance en mètres
     */
    public static double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000;
    }
    
    /**
     * Arrondir une distance pour affichage
     * - < 1 km : affiche en mètres (ex: "500 m")
     * - >= 1 km : affiche en km avec 1 décimale (ex: "2.5 km")
     * 
     * @param distanceKm Distance en kilomètres
     * @return Distance formatée
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 0.001) {
            return "< 1 m";
        } else if (distanceKm < 1.0) {
            int meters = (int) Math.round(distanceKm * 1000);
            return meters + " m";
        } else if (distanceKm < 10.0) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%.0f km", distanceKm);
        }
    }
    
    // ═══════════════════════════════════════════════════════════
    // VÉRIFICATIONS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Vérifier si un point est dans un rayon donné
     * 
     * @param centerLat Latitude du centre
     * @param centerLon Longitude du centre
     * @param pointLat Latitude du point à tester
     * @param pointLon Longitude du point à tester
     * @param radiusKm Rayon en kilomètres
     * @return true si le point est dans le rayon
     */
    public static boolean isWithinRadius(
            double centerLat, double centerLon,
            double pointLat, double pointLon,
            double radiusKm
    ) {
        double distance = calculateDistance(centerLat, centerLon, pointLat, pointLon);
        return distance <= radiusKm;
    }
    
    /**
     * Vérifier si des coordonnées sont valides
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true si valides
     */
    public static boolean areValidCoordinates(Double latitude, Double longitude) {
        return ValidationUtil.isValidCoordinates(latitude, longitude);
    }
    
    // ═══════════════════════════════════════════════════════════
    // CALCULS DE BOÎTE ENGLOBANTE (BOUNDING BOX)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Calculer une boîte englobante (bounding box) autour d'un point
     * Utile pour les requêtes de recherche dans une zone géographique
     * 
     * @param centerLat Latitude du centre
     * @param centerLon Longitude du centre
     * @param radiusKm Rayon en kilomètres
     * @return Tableau [minLat, maxLat, minLon, maxLon]
     */
    public static double[] calculateBoundingBox(double centerLat, double centerLon, double radiusKm) {
        // Approximation simple (peut être imprécise près des pôles)
        double latDelta = radiusKm / 111.0; // 1 degré de latitude ≈ 111 km
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));
        
        double minLat = centerLat - latDelta;
        double maxLat = centerLat + latDelta;
        double minLon = centerLon - lonDelta;
        double maxLon = centerLon + lonDelta;
        
        // S'assurer que les coordonnées sont valides
        minLat = Math.max(minLat, -90.0);
        maxLat = Math.min(maxLat, 90.0);
        minLon = Math.max(minLon, -180.0);
        maxLon = Math.min(maxLon, 180.0);
        
        return new double[]{minLat, maxLat, minLon, maxLon};
    }
    
    /**
     * Classe pour représenter une boîte englobante
     */
    public static class BoundingBox {
        public final double minLatitude;
        public final double maxLatitude;
        public final double minLongitude;
        public final double maxLongitude;
        
        public BoundingBox(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLatitude = minLat;
            this.maxLatitude = maxLat;
            this.minLongitude = minLon;
            this.maxLongitude = maxLon;
        }
        
        /**
         * Vérifier si un point est dans la bounding box
         */
        public boolean contains(double latitude, double longitude) {
            return latitude >= minLatitude && latitude <= maxLatitude &&
                   longitude >= minLongitude && longitude <= maxLongitude;
        }
        
        /**
         * Obtenir le centre de la bounding box
         */
        public double[] getCenter() {
            return new double[]{
                (minLatitude + maxLatitude) / 2,
                (minLongitude + maxLongitude) / 2
            };
        }
    }
    
    /**
     * Créer une BoundingBox autour d'un point
     * 
     * @param centerLat Latitude du centre
     * @param centerLon Longitude du centre
     * @param radiusKm Rayon en kilomètres
     * @return BoundingBox
     */
    public static BoundingBox createBoundingBox(double centerLat, double centerLon, double radiusKm) {
        double[] bounds = calculateBoundingBox(centerLat, centerLon, radiusKm);
        return new BoundingBox(bounds[0], bounds[1], bounds[2], bounds[3]);
    }
    
    // ═══════════════════════════════════════════════════════════
    // URLS MAPS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Générer une URL Google Maps pour un point
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @return URL Google Maps
     */
    public static String generateGoogleMapsUrl(double latitude, double longitude) {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }
    
    /**
     * Générer une URL Google Maps avec label
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @param label Nom du lieu
     * @return URL Google Maps
     */
    public static String generateGoogleMapsUrl(double latitude, double longitude, String label) {
        String encodedLabel = java.net.URLEncoder.encode(label, java.nio.charset.StandardCharsets.UTF_8);
        return String.format("https://www.google.com/maps/search/?api=1&query=%.6f,%.6f&query_place_id=%s",
                latitude, longitude, encodedLabel);
    }
    
    /**
     * Générer une URL Google Maps pour itinéraire
     * 
     * @param fromLat Latitude de départ
     * @param fromLon Longitude de départ
     * @param toLat Latitude d'arrivée
     * @param toLon Longitude d'arrivée
     * @return URL Google Maps directions
     */
    public static String generateGoogleMapsDirectionsUrl(
            double fromLat, double fromLon,
            double toLat, double toLon
    ) {
        return String.format("https://www.google.com/maps/dir/?api=1&origin=%.6f,%.6f&destination=%.6f,%.6f",
                fromLat, fromLon, toLat, toLon);
    }
    
    /**
     * Générer une URL Waze pour navigation
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @return URL Waze
     */
    public static String generateWazeUrl(double latitude, double longitude) {
        return String.format("https://waze.com/ul?ll=%.6f,%.6f&navigate=yes", latitude, longitude);
    }
    
    /**
     * Générer une URL pour appel téléphonique
     * 
     * @param phoneNumber Numéro de téléphone
     * @return URL tel:
     */
    public static String generatePhoneCallUrl(String phoneNumber) {
        if (StringUtil.isBlank(phoneNumber)) {
            return null;
        }
        String cleaned = phoneNumber.replaceAll("[\\s\\-()]", "");
        return "tel:" + cleaned;
    }
    
    /**
     * Générer une URL WhatsApp
     * 
     * @param phoneNumber Numéro de téléphone (format international)
     * @return URL WhatsApp
     */
    public static String generateWhatsAppUrl(String phoneNumber) {
        if (StringUtil.isBlank(phoneNumber)) {
            return null;
        }
        // Supprimer le + du début si présent
        String cleaned = phoneNumber.replaceAll("[\\s\\-()\\+]", "");
        return "https://wa.me/" + cleaned;
    }
    
    /**
     * Générer une URL WhatsApp avec message pré-rempli
     * 
     * @param phoneNumber Numéro de téléphone
     * @param message Message pré-rempli
     * @return URL WhatsApp
     */
    public static String generateWhatsAppUrl(String phoneNumber, String message) {
        String baseUrl = generateWhatsAppUrl(phoneNumber);
        if (baseUrl == null) {
            return null;
        }
        String encodedMessage = java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8);
        return baseUrl + "?text=" + encodedMessage;
    }
    
    // ═══════════════════════════════════════════════════════════
    // COORDONNÉES SÉNÉGAL
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Vérifier si des coordonnées sont approximativement au Sénégal
     * Boîte englobante approximative du Sénégal:
     * - Latitude: 12.3° N à 16.7° N
     * - Longitude: -17.5° W à -11.4° W
     * 
     * @param latitude Latitude
     * @param longitude Longitude
     * @return true si dans les limites du Sénégal
     */
    public static boolean isInSenegal(double latitude, double longitude) {
        return latitude >= 12.3 && latitude <= 16.7 &&
               longitude >= -17.5 && longitude <= -11.4;
    }
    
    /**
     * Calculer la distance depuis Dakar
     * 
     * @param latitude Latitude du point
     * @param longitude Longitude du point
     * @return Distance en kilomètres depuis Dakar
     */
    public static double distanceFromDakar(double latitude, double longitude) {
        return calculateDistance(
            AppConstants.DAKAR_LATITUDE,
            AppConstants.DAKAR_LONGITUDE,
            latitude,
            longitude
        );
    }
    
    // ═══════════════════════════════════════════════════════════
    // BEARING (DIRECTION)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Calculer le bearing (direction) entre deux points
     * 
     * @param lat1 Latitude du point 1
     * @param lon1 Longitude du point 1
     * @param lat2 Latitude du point 2
     * @param lon2 Longitude du point 2
     * @return Bearing en degrés (0-360, 0 = Nord)
     */
    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double lonDiff = Math.toRadians(lon2 - lon1);
        
        double y = Math.sin(lonDiff) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(lonDiff);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360; // Normaliser entre 0 et 360
    }
    
    /**
     * Convertir un bearing en direction cardinale (N, NE, E, SE, S, SW, W, NW)
     * 
     * @param bearing Bearing en degrés
     * @return Direction cardinale
     */
    public static String bearingToCardinal(double bearing) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(bearing / 45) % 8;
        return directions[index];
    }
}
