package com.google.ar.core.examples.java.common.navigation;

public class RotationKalmanFilter {
    private float q = 0.001f;  // process noise
    private float r = 0.5f;    // default measurement noise
    private float x = 0;       // estimated value
    private float p = 1;       // estimation error
    private float k;           // kalman gain

    public RotationKalmanFilter(float initVal) {
        this.x = initVal;
    }

    // Overload: allow setting measurement noise per reading
    public float update(float measurement, float measurementNoise) {
        // Prediction update
        p += q;

        // Measurement update
        float rLocal = (measurementNoise > 0) ? measurementNoise : r;
        k = p / (p + rLocal);
        x += k * (measurement - x);
        p *= (1 - k);

        return x;
    }

    public float update(float measurement) {
        return update(measurement, -1); // fallback to default noise
    }

    public void setNoise(float processNoise, float measurementNoise) {
        this.q = processNoise;
        this.r = measurementNoise;
    }

    public float getEstimate() {
        return x;
    }
}
