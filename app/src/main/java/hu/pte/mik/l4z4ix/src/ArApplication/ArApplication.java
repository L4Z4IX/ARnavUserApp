package hu.pte.mik.l4z4ix.src.ArApplication;

import android.annotation.SuppressLint;
import android.app.Application;

import hu.pte.mik.l4z4ix.src.Components.navigation.LocationProvider;

public class ArApplication extends Application {
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        LocationProvider.getInstance(this);
    }
}
