package com.google.ar.core.examples.java.common.navigation;

public class RotationKalmanFilter {
    private float angle = 0f;
    private float bias = 0f;
    private final float[][] P = {{1, 0}, {0, 1}};

    private float Q_angle = 0.001f;
    private float Q_bias = 0.003f;
    private float R_measure = 0.5f;

    public float update(float newAngle, float newRate, float dt) {
        // Predict
        angle += dt * (newRate - bias);
        P[0][0] += dt * (dt * P[1][1] - P[0][1] - P[1][0] + Q_angle);
        P[0][1] -= dt * P[1][1];
        P[1][0] -= dt * P[1][1];
        P[1][1] += Q_bias * dt;

        // Update
        float y = newAngle - angle;
        float S = P[0][0] + R_measure;
        float K0 = P[0][0] / S;
        float K1 = P[1][0] / S;

        angle += K0 * y;
        bias += K1 * y;

        float P00_temp = P[0][0];
        float P01_temp = P[0][1];

        P[0][0] -= K0 * P00_temp;
        P[0][1] -= K0 * P01_temp;
        P[1][0] -= K1 * P00_temp;
        P[1][1] -= K1 * P01_temp;

        return angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setNoise(float qAngle, float qBias, float rMeasure) {
        this.Q_angle = qAngle;
        this.Q_bias = qBias;
        this.R_measure = rMeasure;
    }
}
