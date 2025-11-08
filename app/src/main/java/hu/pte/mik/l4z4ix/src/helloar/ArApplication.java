package hu.pte.mik.l4z4ix.src.helloar;

import android.annotation.SuppressLint;
import android.app.Application;

import hu.pte.mik.l4z4ix.src.common.navigation.LocationProvider;

public class ArApplication extends Application {
    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        LocationProvider.getInstance(this);
    }
}
