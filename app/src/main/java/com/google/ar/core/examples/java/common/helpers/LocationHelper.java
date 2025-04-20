package com.google.ar.core.examples.java.common.helpers;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class LocationHelper {

    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final ArrayList<Location> locations = new ArrayList<>();

    public LocationHelper(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void AddCallBack(Consumer<Location> consumer) {

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 200).build();


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {


                    System.out.println("Got location: " + "ACC: " + location.getAccuracy() + "alt: " + location.getAltitude() + " lat:" + location.getLatitude() + " long:" + location.getLongitude());
                    if (location.getAccuracy() < 5f) {
                        locations.add(0, location);
                        if (locations.size() == 11) {
                            locations.remove(locations.stream().max(Comparator.comparingDouble(Location::getAccuracy)).get());
                        }

                    }


                }
                if (locations.size() > 5) {
                    System.out.print("Accuracies: ");
                    locations.forEach(x -> System.out.print(x.getAccuracy() + " "));
                    System.out.println();
                    consumer.accept(locationAvg());
                }
            }
        };


        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private Location locationAvg() {
        int l = locations.size();
        double alt = 0, lat = 0, lng = 0;
        float acc = 0;
        for (Location loc : locations) {
            alt += loc.getAltitude();
            lat += loc.getLatitude();
            lng += loc.getLongitude();
            acc += loc.getAccuracy();
        }
        Location res = new Location("AVG_MEASURE");
        res.setAltitude(alt / l);
        res.setLatitude(lat / l);
        res.setLongitude(lng / l);
        res.setAccuracy(acc / l);
        return res;
    }

    public void stop() {
        if (locationCallback != null)
            fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
