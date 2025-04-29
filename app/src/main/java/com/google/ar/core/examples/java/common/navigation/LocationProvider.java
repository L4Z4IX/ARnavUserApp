package com.google.ar.core.examples.java.common.navigation;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class LocationProvider {
    private Location currentLocation;
    private static LocationProvider INSTANCE;
    private final FusedLocationProviderClient fusedLocationClient;
    private final ArrayList<Location> locationHistory = new ArrayList<>();
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            for (Location l : locationResult.getLocations())
                process(l);
        }
    };

    private LocationProvider(Context c) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(c);
    }

    public static LocationProvider getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LocationProvider(context);
        }
        return INSTANCE;
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION})
    public void start() {
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    public void stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void addToHistory(Location location) {
        locationHistory.add(new Location(location));
        if (locationHistory.size() > 15) {
            locationHistory.remove(0);
        }
    }

    private void inflateAccuracies() {
        for (int i = 0; i < locationHistory.size(); i++) {
            float acc = locationHistory.get(i).getAccuracy();
            float newAcc = (float) (acc * Math.pow(1.1, locationHistory.size() - i));
            locationHistory.get(i).setAccuracy(newAcc);
        }
    }

    private Location fuseLocationHistory() {
        double sumLat = 0, sumLng = 0, sumAlt = 0, sumWeight = 0;
        for (Location l : locationHistory) {
            double weight = 1.0 / (l.getAccuracy() * l.getAccuracy());
            sumLat += l.getLatitude() * weight;
            sumLng += l.getLongitude() * weight;
            sumAlt += l.getAltitude() * weight;
            sumWeight += weight;
        }
        Location res = new Location("fused");
        res.setLatitude(sumLat / sumWeight);
        res.setLongitude(sumLng / sumWeight);
        res.setAltitude(sumAlt / sumWeight);
        res.setAccuracy((float) Math.sqrt(1.0 / sumWeight));
        return res;
    }

    private void process(Location newLocation) {
        addToHistory(newLocation);
        if (currentLocation == null) {
            currentLocation = newLocation;
            return;
        }
        //inflateAccuracies();
        currentLocation = fuseLocationHistory();

    }


    public Location getCurrentLocation() {
        return currentLocation;
    }
}
