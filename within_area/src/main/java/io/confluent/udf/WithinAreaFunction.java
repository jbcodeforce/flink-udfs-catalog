package io.confluent.udf;

import org.apache.flink.table.functions.ScalarFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

/**
 * Flink UDF that checks whether a point (lat, lon) is inside an area.
 * Areas are defined as rectangle or circle
 * <p>Table schema (example):
 *   area_id    STRING
 *   area_type  STRING  // "RECTANGLE" or "CIRCLE"
 *   geo_locations  ARRAY<DOUBLE>   // 4 values for rectangle, 2 for circle
 *   radius     DOUBLE (nullable, meters, only used for circles)
 *
 * <p>Usage in SQL:

 */
public class WithinAreaFunction extends ScalarFunction {
    private static final Logger logger = LogManager.getLogger(WithinAreaFunction.class);
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers
    private static final double EARTH_RADIUS_METERS = 6_371_000.0; // mean radius

    /**
     * Calculates the distance between two points on Earth using the Haversine formula.
     *
     * @param pointLat      latitude of the point you want to test
     * @param pointLon      longitude of the point you want to test
     * @param areaType      "RECTANGLE" or "CIRCLE" (case‑insensitive)
     * @param geoLocations  list of doubles that encode the area geometry
     * @param radius        radius in meters (only needed for circles)
     * @return true if the point is inside the area, false otherwise
     */
    public boolean eval(Double pointLat, Double pointLon, String areaType,  List<Double> geoLocations, Double radius) {
        if (pointLat == null || pointLon == null || areaType == null || geoLocations == null) {
            return false;
        }

        switch (areaType.toUpperCase()) {
            case "RECTANGLE":
                return insideRectangle(pointLat, pointLon, geoLocations);

            case "CIRCLE":
                return insideCircle(pointLat, pointLon, geoLocations, radius);

            default:
                // Unknown area type → treat as outside
                return false;
        }
    }

    private boolean insideRectangle(Double pLat, Double pLon, List<Double> loc) {
        if (loc.size() != 4) {
            return false; // malformed data
        }

        double lat1 = loc.get(0);
        double lon1 = loc.get(1);
        double lat2 = loc.get(2);
        double lon2 = loc.get(3);

        double minLat = Math.min(lat1, lat2);
        double maxLat = Math.max(lat1, lat2);
        double minLon = Math.min(lon1, lon2);
        double maxLon = Math.max(lon1, lon2);

        return pLat >= minLat && pLat <= maxLat &&
               pLon >= minLon && pLon <= maxLon;
    }

    /* ------------------------------------------------------------ */
    /*   Circle handling – haversine distance vs radius            */
    /* ------------------------------------------------------------ */
    private boolean insideCircle(Double pLat, Double pLon, List<Double> loc, Double rad) {
        if (loc.size() != 2 || rad == null) {
            return false; // malformed data
        }

        double centerLat = loc.get(0);
        double centerLon = loc.get(1);

        double distanceMeters = haversineMeters(pLat, pLon, centerLat, centerLon);
        return distanceMeters <= rad;
    }

    /**
     * Haversine formula that returns the great‑circle distance in meters.
     */
    public double haversineMeters(
            double lat1Deg, double lon1Deg,
            double lat2Deg, double lon2Deg) {

        double lat1Rad = Math.toRadians(lat1Deg);
        double lon1Rad = Math.toRadians(lon1Deg);
        double lat2Rad = Math.toRadians(lat2Deg);
        double lon2Rad = Math.toRadians(lon2Deg);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Returns a string describing the function.
     */
    @Override
    public String toString() {
        return "IS_WITHIN_DISTANCE";
    }
}
