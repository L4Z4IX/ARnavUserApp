package com.google.ar.core.examples.java.common.navigation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationProvider implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationVectorSensor;
    private final Sensor magneticFieldSensor;

    private final RotationKalmanFilter kalmanFilter = new RotationKalmanFilter();

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private final float[] magneticValues = new float[3];

    private float lastAzimuth = 0f;
    private long lastTimestamp = 0;
    private float filteredAzimuthDeg = 0f;
    private static RotationProvider INSTANCE;

    private RotationProvider(Context c) {
        sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public static RotationProvider getInstance(Context context) {
        if (INSTANCE == null)
            INSTANCE = new RotationProvider(context);
        return INSTANCE;
    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public float getFilteredAzimuth() {
        return filteredAzimuthDeg;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, event.values.length);
        }

        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        float azimuthRad = orientationAngles[0];
        float azimuthDeg = (float) Math.toDegrees(azimuthRad);
        azimuthDeg = (azimuthDeg + 360f) % 360f;

        // Check for magnetic distortion
        float magFieldStrength = (float) Math.sqrt(
                magneticValues[0] * magneticValues[0] +
                        magneticValues[1] * magneticValues[1] +
                        magneticValues[2] * magneticValues[2]
        );

        boolean distorted = (magFieldStrength < 25f || magFieldStrength > 65f);
        if (distorted) return;

        long now = System.nanoTime();
        float dt = (lastTimestamp > 0) ? (now - lastTimestamp) / 1e9f : 0f;
        lastTimestamp = now;

        float rate = (dt > 0) ? (azimuthDeg - lastAzimuth) / dt : 0f;
        filteredAzimuthDeg = kalmanFilter.update(azimuthDeg, rate, dt);
        lastAzimuth = azimuthDeg;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Optional: handle sensor accuracy changes if you want to adapt measurement noise
    }
}
