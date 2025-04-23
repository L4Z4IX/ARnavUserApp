package com.google.ar.core.examples.java.common.navigation;

public class GPSKalmanFilter {

    private final double minAccuracy = 1;

    private double lat, lng, alt;
    private float accuracy;
    private long timestamp;
    private boolean isInitialized = false;

    public GPSKalmanFilter() {

    }

    public void process(double speed, double lat_measure, double lng_measure, float accuracy_measure, long time_millis, double alt_measure) {

        if (accuracy_measure < minAccuracy)
            accuracy_measure = (float) minAccuracy;

        if (!isInitialized) {
            this.lat = lat_measure;
            this.lng = lng_measure;
            this.alt = alt_measure;
            this.accuracy = accuracy_measure * accuracy_measure;
            this.timestamp = time_millis;
            this.isInitialized = true;
        }

        long timeDelta = time_millis - this.timestamp;
        if (timeDelta > 0) {
            float variance = accuracy * accuracy;
            float predictedVariance = variance + (float) (timeDelta * speed * speed / 1000);
            float k = predictedVariance / (predictedVariance + accuracy_measure * accuracy_measure);

            this.lat += k * (lat_measure - this.lat);
            this.lng += k * (lng_measure - this.lng);
            this.accuracy = (float) Math.sqrt((1 - k) * predictedVariance);
            this.timestamp = time_millis;
            this.alt = alt_measure;
        }
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public double getAlt() {
        return alt;
    }
}
