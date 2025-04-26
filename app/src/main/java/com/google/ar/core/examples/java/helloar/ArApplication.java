package com.google.ar.core.examples.java.helloar;

import android.app.Application;

import com.google.ar.core.examples.java.common.navigation.LocationSensorManager;
import com.google.ar.core.examples.java.common.navigation.SensorFusionLocationProcessor;

public class ArApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocationSensorManager.getInstance(this, SensorFusionLocationProcessor.getInstance()).start();
    }
}
