package com.google.ar.core.examples.java.common.navigation;

public class RotationKalmanFilter {
    private float q = 0.001f;  // process noise
    private float r = 0.5f;    // measurement noise
    private float x = 0;       // estimated value
    private float p = 1;       // estimation error
    private float k;           // kalman gain

    public RotationKalmanFilter(float initVal) {
        this.x = initVal;
    }

    public float update(float measurement) {
        // Prediction update
        p += q;

        // Measurement update
        k = p / (p + r);
        x += k * (measurement - x);
        p *= (1 - k);

        return x;
    }

    public void setNoise(float processNoise, float measurementNoise) {
        this.q = processNoise;
        this.r = measurementNoise;
    }

    public float getEstimate() {
        return x;
    }
}
