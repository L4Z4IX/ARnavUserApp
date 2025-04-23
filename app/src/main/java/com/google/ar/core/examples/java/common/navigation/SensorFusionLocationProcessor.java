package com.google.ar.core.examples.java.common.navigation;

import android.location.Location;

public class SensorFusionLocationProcessor {
    private final GPSKalmanFilter kalman;
    private double lastLat, lastLng;
    private long lastUpdateTime = 0;
    private float currentHeading = 0; // Degrees
    private final float stepLength = 0.7f; // Adjust as needed (avg step ~70cm)
    private boolean gpsAvailable = false;
    private static SensorFusionLocationProcessor instance;

    public static synchronized SensorFusionLocationProcessor getInstance() {
        if (instance == null) {
            instance = new SensorFusionLocationProcessor();
        }
        return instance;
    }

    private SensorFusionLocationProcessor() {
        kalman = new GPSKalmanFilter();
    }

    public void updateWithGPS(Location location) {
        gpsAvailable = true;
        kalman.process(location.getSpeed(), location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.getTime(), location.getAltitude());
        lastLat = kalman.getLat();
        lastLng = kalman.getLng();
        lastUpdateTime = location.getTime();
    }

    public void updateHeading(float headingDegrees) {
        currentHeading = headingDegrees;
    }

    public void stepDetected() {
        if (!gpsAvailable) {
            double deltaLat = stepLength * Math.cos(Math.toRadians(currentHeading)) / 6371000.0;
            double deltaLng = stepLength * Math.sin(Math.toRadians(currentHeading)) / (6371000.0 * Math.cos(Math.toRadians(lastLat)));

            lastLat += Math.toDegrees(deltaLat);
            lastLng += Math.toDegrees(deltaLng);

            kalman.process(
                    stepLength, // using step length as a proxy for low-speed estimate
                    lastLat,
                    lastLng,
                    5.0f, // assume 5m accuracy for dead reckoning
                    System.currentTimeMillis(),
                    kalman.getAlt() // retain last known altitude
            );
        }
    }

    public Location getCurrentEstimatedLocation() {
        Location loc = new Location("fused");
        loc.setLatitude(kalman.getLat());
        loc.setLongitude(kalman.getLng());
        loc.setAccuracy(kalman.getAccuracy());
        loc.setAltitude(kalman.getAlt());
        return loc;
    }
}
