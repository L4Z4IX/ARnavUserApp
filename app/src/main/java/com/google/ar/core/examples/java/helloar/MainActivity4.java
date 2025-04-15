package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.LocationHelper;
import com.google.ar.core.examples.java.common.helpers.LocationPermissionHelper;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity4 extends AppCompatActivity {

    ArFragment arFragment;
    AnchorNode currentAnchorNode;

    private LocationHelper locationHelper;
    private volatile Location currentLocation;
    private final Location testLocation = new Location("manual");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        locationHelper = new LocationHelper(this);
        testLocation.setAltitude(256.8999938964844);
        testLocation.setLatitude(46.0777558);
        testLocation.setLongitude(18.2862263);


        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arfragment);

        //System.out.println(currentLocation.getAltitude() + " " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());

        //Pose targetPose = cameraPose.compose(Pose.makeTranslation(0, 0, -2.0f));


    }

    private void placeModel() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        Camera camera = frame.getCamera();
        double distance = currentLocation.distanceTo(testLocation);
        double bearing = currentLocation.bearingTo(testLocation);

        float dx = (float) (distance * Math.sin(Math.toRadians(bearing)));
        float dy = (float) (currentLocation.getAltitude() - testLocation.getAltitude());
        float dz = (float) (-distance * Math.cos(Math.toRadians(bearing)));
        try {
            Pose cameraPose = camera.getPose();
            Pose targetPose = cameraPose.compose(Pose.makeTranslation(dx, dy, dz));
            System.out.println(dx + " " + dy + " " + dz);

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(targetPose);
            if (currentAnchorNode != null) {
                currentAnchorNode.getAnchor().detach();
            }
            ModelRenderable.builder()
                    .setSource(this, R.raw.pawn).build()
                    .thenAccept(renderable -> {
                        currentAnchorNode = new AnchorNode(anchor);
                        currentAnchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                        transformableNode.setRenderable(renderable);
                        transformableNode.setParent(currentAnchorNode);
                    })
                    .exceptionally(throwable -> {
                        System.err.print("AR Model load error: " + throwable.getMessage());
                        return null;
                    });
            locationHelper.stop();
        } catch (Exception ignored) {
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {


        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }
        if (!LocationPermissionHelper.hasFineLocationPermission(this)) {
            LocationPermissionHelper.requestFineLocationPermission(this);
            return;
        }


        //TODO Set up gps tracking and what to do with data HERE
        locationHelper.AddCallBack(x -> {
            currentLocation = x;
            System.out.println("alt: " + currentLocation.getAltitude() + " lat:" + currentLocation.getLatitude() + " long:" + currentLocation.getLongitude());
            placeModel();
        });
        arFragment.onResume();
        Config conf = arFragment.getArSceneView().getSession().getConfig();
        //conf.setFocusMode(Config.FocusMode.AUTO);
        conf.setDepthMode(Config.DepthMode.AUTOMATIC);
        conf.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        arFragment.getArSceneView().getSession().configure(conf);
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        arFragment.onDestroy();
        locationHelper.stop();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        arFragment.onPause();
        super.onPause();
    }
}
