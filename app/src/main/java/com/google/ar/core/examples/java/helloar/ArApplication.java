package com.google.ar.core.examples.java.helloar;

import android.app.Application;

import com.google.ar.core.examples.java.common.navigation.LocationTracker;

public class ArApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LocationTracker.getInstance(this).startTracking();
    }
}
