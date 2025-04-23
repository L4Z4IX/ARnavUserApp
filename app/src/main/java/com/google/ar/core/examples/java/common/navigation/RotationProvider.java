package com.google.ar.core.examples.java.common.navigation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationProvider implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationSensor;
    private final RotationKalmanFilter kalmanAzimuth = new RotationKalmanFilter(0f);

    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private float filteredAzimuthDeg = 0;
    private static RotationProvider INSTANCE;

    private RotationProvider(Context c) {
        sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public static RotationProvider getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new RotationProvider(context);
        }
        return INSTANCE;
    }

    public void start() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);

        float azimuthRad = orientation[0];
        float azimuthDeg = (float) Math.toDegrees(azimuthRad);
        if (azimuthDeg < 0) azimuthDeg += 360;

        filteredAzimuthDeg = kalmanAzimuth.update(azimuthDeg);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public float getFilteredAzimuth() {
        return filteredAzimuthDeg;
    }
}
