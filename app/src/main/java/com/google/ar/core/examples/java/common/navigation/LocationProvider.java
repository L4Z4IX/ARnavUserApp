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
    private double distanceToZero = 0;
    private double bearingDegreesToZero = 0;
    private boolean isTracking = false;
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

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void start() {
        locationHistory.clear();
        if (!isTracking) {
            LocationRequest request = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setFastestInterval(500);

            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            isTracking = true;
        }
    }

    public void stop() {
        if (isTracking)
            fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
    }

    private void addToHistory(Location location) {
        locationHistory.add(new Location(location));
        if (locationHistory.size() > 30) {
            int maxIndex = 0;
            for (int i = 1; i < locationHistory.size(); i++)
                if (locationHistory.get(maxIndex).getAccuracy() < locationHistory.get(i).getAccuracy())
                    maxIndex = i;

            locationHistory.remove(maxIndex);
        }
    }

    private Location moveLocation(Location start, double distanceMeters, double bearingDegrees) {
        double R = 6371000; // Earth radius in meters
        double angleRad = bearingDegrees * Math.PI / 180;
        double latRad = start.getLatitude() * Math.PI / 180;
        double lngRad = start.getLongitude() * Math.PI / 180;

        double destLat = Math.asin(Math.sin(latRad) * Math.cos(distanceMeters / R) +
                Math.cos(latRad) * Math.sin(distanceMeters / R) * Math.cos(angleRad));
        double destLng = lngRad + Math.atan2(
                Math.sin(angleRad) * Math.sin(distanceMeters / R) * Math.cos(latRad),
                Math.cos(distanceMeters / R) - Math.sin(latRad) * Math.sin(destLat)
        );


        Location result = new Location(start);
        result.setLatitude(destLat * 180 / Math.PI);
        result.setLongitude(destLng * 180 / Math.PI);
        result.setAltitude(start.getAltitude());

        return result;
    }

    public void updateLocations(double distanceToZeroMeters, double bearingDegreesToZero) {
        this.distanceToZero = distanceToZeroMeters;
        this.bearingDegreesToZero = bearingDegreesToZero;
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
        if (distanceToZero == 0 && bearingDegreesToZero == 0) {
            addToHistory(newLocation);
        } else {
            Location movedLocation = moveLocation(newLocation, distanceToZero, bearingDegreesToZero);
            addToHistory(movedLocation);
            //System.out.println("LOCPROC:def: d: " + distanceToZero + " b: " + bearingDegreesToZero + " new d: " + newLocation.distanceTo(movedLocation) + " b: " + movedLocation.bearingTo(newLocation));
            System.out.println("AAA" + newLocation.getLatitude() + ";" + newLocation.getLongitude() + ";" + newLocation.getAccuracy());
            System.out.println("AAA" + bearingDegreesToZero + ";" + distanceToZero);
            System.out.println("AAA" + movedLocation.getLatitude() + ";" + movedLocation.getLongitude() + ";" + movedLocation.getAccuracy());
        }
        currentLocation = fuseLocationHistory();
    }


    public Location getCurrentLocation() {
        return currentLocation;
    }
}
