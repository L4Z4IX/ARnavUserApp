package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.examples.java.common.entityModel.Storage;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.LocationPermissionHelper;
import com.google.ar.core.examples.java.common.navigation.LocationTracker;
import com.google.ar.core.examples.java.common.navigation.PointManager;
import com.google.ar.core.examples.java.common.navigation.RotationProvider;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity4 extends AppCompatActivity {

    ArFragment arFragment;
    AnchorNode currentAnchorNode;

    private volatile Location currentLocation;
    private volatile float angleCorrection;
    private final Location testLocation = new Location("manual");
    private ModelRenderable renderable;
    private int pointId;
    private Timer placementTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        testLocation.setAltitude(256.8999938964844);
        testLocation.setLatitude(46.07772804106784);
        testLocation.setLongitude(18.286214778052006);

        ModelRenderable.builder()
                .setSource(this, R.raw.pawn).build().thenAccept(r -> renderable = r);


        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arfragment);
        pointId = Integer.parseInt(getIntent().getStringExtra("pointId"));

        //System.out.println(currentLocation.getAltitude() + " " + currentLocation.getLatitude() + " " + currentLocation.getLongitude());

        //Pose targetPose = cameraPose.compose(Pose.makeTranslation(0, 0, -2.0f));


    }

    private void placeModel() {
        double distance = currentLocation.distanceTo(testLocation);
        double bearing = currentLocation.bearingTo(testLocation);

        float dx = (float) (distance * Math.sin(Math.toRadians(bearing)));
        float dy = (float) (currentLocation.getAltitude() - testLocation.getAltitude());
        float dz = (float) (-distance * Math.cos(Math.toRadians(bearing)));
        try {
            Pose diffPose = Pose.makeTranslation(dx, dy, dz);
            float[] poseQ = diffPose.getRotationQuaternion();
            Quaternion rotationQ = Quaternion.multiply(new Quaternion(0f, 1f, 0f, angleCorrection), new Quaternion(poseQ[0], poseQ[1], poseQ[2], poseQ[3]));
            Pose targetPose = Pose.makeRotation(rotationQ.x, rotationQ.y, rotationQ.z, rotationQ.w).compose(Pose.makeTranslation(diffPose.getTranslation()));
            System.out.println("CAMERA POS: " + arFragment.getArSceneView().getScene().getCamera().getWorldPosition());
            System.out.println(dx + " " + dy + " " + dz);

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(targetPose);
            if (currentAnchorNode != null) {
                currentAnchorNode.getAnchor().detach();
            }
            for (int i = 0; i < renderable.getSubmeshCount(); i++) {
                Material originalMaterial = renderable.getMaterial(i);
                // Create a mutable copy to avoid modifying the original shared material
                Material debugMaterial = originalMaterial.makeCopy();

                // Disable depth testing (reading)
                debugMaterial.setBoolean("depthRead", false); // Filament name
                // Or potentially: debugMaterial.setBoolean("depthTest", false); // Older/alternative name

                // Disable depth writing
                debugMaterial.setBoolean("depthWrite", false);

                // Optional: Set a high rendering priority (e.g., 100+)
                // The exact parameter name might vary based on Sceneform/Filament version.
                // Check Filament documentation for material parameters like 'priority' or 'renderingOrder'.
                // Example (syntax might differ):
                // debugMaterial.setInt("priority", 100);

                // Apply the modified material back to the renderable's submesh
                renderable.setMaterial(i, debugMaterial);
            }
            currentAnchorNode = new AnchorNode(anchor);
            currentAnchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
            transformableNode.setRenderable(renderable);
            transformableNode.setParent(currentAnchorNode);
            transformableNode.setLocalScale(new Vector3(5.0f, 5.0f, 5.0f));
            System.out.println("Placed object");

        } catch (Exception ignored) {
            System.out.println("ERROR: " + ignored.getMessage());
            ignored.printStackTrace();
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


        arFragment.onResume();
        Config conf = arFragment.getArSceneView().getSession().getConfig();
        conf.setFocusMode(Config.FocusMode.AUTO);
        conf.setDepthMode(Config.DepthMode.DISABLED);
        conf.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        arFragment.getArSceneView().getSession().configure(conf);
        PointManager pointManager = new PointManager(Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPointSet().stream()).filter(x -> x.getId() == pointId).findFirst().get());
        pointManager.pointManagerCallback(new Location("ASD"));
        super.onResume();
        //Timer Scheduler for object placement
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                currentLocation = LocationTracker.getInstance(null).getSmoothedLocation();
                angleCorrection = (float) Math.toRadians(-RotationProvider.getInstance(null).getFilteredAzimuth());
                System.out.println("ACC: " + currentLocation.getAccuracy() + "alt: " + currentLocation.getAltitude() + " lat:" + currentLocation.getLatitude() + " long:" + currentLocation.getLongitude());
                runOnUiThread(() -> placeModel());
            }
        }, 5000, 2000);


    }


    @Override
    protected void onDestroy() {
        arFragment.onDestroy();
        placementTimer.cancel();
        placementTimer.purge();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        arFragment.onPause();
        placementTimer.cancel();
        placementTimer.purge();
        super.onPause();
    }
}
