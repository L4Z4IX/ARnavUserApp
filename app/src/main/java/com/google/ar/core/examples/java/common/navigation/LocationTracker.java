package com.google.ar.core.examples.java.common.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTracker {
    private static LocationTracker instance;
    private final FusedLocationProviderClient fusedLocationClient;
    private final KalmanFilter kalman;

    private boolean isTracking = false;

    private LocationTracker(Context ctx) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx.getApplicationContext());
        kalman = new KalmanFilter();
    }

    public static LocationTracker getInstance(Context ctx) {
        if (instance == null) {
            instance = new LocationTracker(ctx);
        }
        return instance;
    }

    @SuppressLint("MissingPermission")
    public void startTracking() {
        if (isTracking) return;
        isTracking = true;

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location.hasAccuracy() && location.getAccuracy() <= 20) {
                        kalman.process(
                                location.getSpeed(),
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getAccuracy(),
                                location.getTime(),
                                location.getAltitude()
                        );
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    public Location getSmoothedLocation() {
        Location loc = new Location("kalman");
        loc.setLatitude(kalman.getLat());
        loc.setLongitude(kalman.getLng());
        loc.setAccuracy(kalman.getAccuracy());
        loc.setAltitude(kalman.getAlt());
        return loc;
    }
}

