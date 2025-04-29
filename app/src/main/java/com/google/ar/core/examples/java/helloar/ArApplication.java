package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.app.Application;

import com.google.ar.core.examples.java.common.navigation.LocationProvider;

public class ArApplication extends Application {
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        //LocationSensorManager.getInstance(this, SensorFusionLocationProcessor.getInstance()).start();
        LocationProvider.getInstance(this).start();
    }
}
