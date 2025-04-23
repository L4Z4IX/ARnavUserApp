package com.google.ar.core.examples.java.common.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationSensorManager implements SensorEventListener {
    private final SensorManager sensorManager;
    private final FusedLocationProviderClient fusedLocationClient;
    private final SensorFusionLocationProcessor processor;
    private final Sensor accelSensor, magnetSensor, stepSensor;

    private float[] accel = new float[3];
    private float[] magnet = new float[3];

    private static LocationSensorManager instance;

    public static LocationSensorManager getInstance(Context context, SensorFusionLocationProcessor processor) {
        if (instance == null) {
            instance = new LocationSensorManager(context, processor);
        }
        return instance;
    }

    private LocationSensorManager(Context context, SensorFusionLocationProcessor processor) {
        this.processor = processor;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }


    @SuppressLint("MissingPermission")
    public void start() {
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_GAME);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        fusedLocationClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                for (Location loc : result.getLocations()) {
                    if (loc.getAccuracy() <= 20) {
                        processor.updateWithGPS(loc);
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    public void stop() {
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accel = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnet = event.values.clone();
                break;
            case Sensor.TYPE_STEP_DETECTOR:
                processor.stepDetected();
                break;
        }

        updateHeading();
    }

    private void updateHeading() {
        float[] rotationMatrix = new float[9];
        float[] orientationAngles = new float[3];

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            float azimuth = (float) Math.toDegrees(orientationAngles[0]);
            processor.updateHeading((azimuth + 360) % 360);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}

