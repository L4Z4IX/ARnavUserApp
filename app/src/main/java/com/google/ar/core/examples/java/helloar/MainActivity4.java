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
import com.google.ar.core.examples.java.common.navigation.LocationProvider;
import com.google.ar.core.examples.java.common.navigation.PointManager;
import com.google.ar.core.examples.java.common.navigation.RotationProvider;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity4 extends AppCompatActivity {

    ArFragment arFragment;
    AnchorNode currentAnchorNode;

    private volatile Location currentLocation;
    private final Location testLocation = new Location("manual");
    private ModelRenderable renderable;
    private int pointId;
    private final Timer placementTimer = new Timer();
    private RotationProvider rotationProvider;
    private final List<Double> fakeToRealHistory = Collections.synchronizedList(new ArrayList<>());

    private void addFakeToRealHistory(double reading) {
        reading = (360 + reading) % 360;
        synchronized (fakeToRealHistory) {
            fakeToRealHistory.add(reading);
            if (fakeToRealHistory.size() > 20)
                fakeToRealHistory.remove(0);
        }
    }

    private double getAVGDegreesFakeToReal() {
        synchronized (fakeToRealHistory) {
            return fakeToRealHistory.stream().mapToDouble(x -> x).average().getAsDouble();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        rotationProvider = new RotationProvider(this);
        testLocation.setAltitude(256.8999938964844);
        testLocation.setLatitude(46.07773314317652);
        testLocation.setLongitude(18.286210482154498);

        ModelRenderable.builder()
                .setSource(this, R.raw.pawn).build().thenAccept(r -> renderable = r);


        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arfragment);
        pointId = Integer.parseInt(getIntent().getStringExtra("pointId"));

    }

    private double getDegreesToFakeNorth() {
        float[] zAxis = arFragment.getArSceneView().getArFrame().getCamera().getPose().getZAxis();
        float forwardX = zAxis[0];
        float forwardZ = zAxis[2];
        return (360 - Math.toDegrees(Math.atan2(forwardX, forwardZ))) % 360;
    }

    private void placeModel() {
        double distance = currentLocation.distanceTo(testLocation);
        double bearing = (currentLocation.bearingTo(testLocation) + 360) % 360;
//        double distance = 5f;
//        double bearing = 300;

        float worldRotation = (float) Math.toRadians((getAVGDegreesFakeToReal() + bearing + 360) % 360);
        //double calculatedRotationRad = Math.toRadians((Math.toDegrees(angleCorrection - angleRadians) + 360) % 360);
        System.out.println("bearing: " + bearing
                + " Constant rotation: " + getAVGDegreesFakeToReal()
                + " FinalAngle: " + Math.toDegrees(worldRotation)
        );

        float dx = (float) (distance * Math.sin(worldRotation));
        float dy = (float) (currentLocation.getAltitude() - testLocation.getAltitude());
        float dz = (float) (distance * Math.cos(worldRotation));

        try {
            Pose targetPose = Pose.makeTranslation(dx, dy, dz);
            //System.out.println("CAMERA POS: " + arFragment.getArSceneView().getScene().getCamera().getWorldPosition());
            System.out.println("CALC DIST: " + distance + " D: " + dx + " " + dy + " " + dz);

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
            transformableNode.setParent(currentAnchorNode);
            transformableNode.setRenderable(renderable);

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

        arFragment.onResume();
        Config conf = arFragment.getArSceneView().getSession().getConfig();
        conf.setFocusMode(Config.FocusMode.AUTO);
        conf.setDepthMode(Config.DepthMode.AUTOMATIC);
        conf.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        arFragment.getArSceneView().getSession().configure(conf);
        PointManager pointManager = new PointManager(Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).filter(x -> x.getId() == pointId).findFirst().get());
        pointManager.pointManagerCallback(new Location("ASD"));
        super.onResume();
        //Timer Scheduler for object placement
        rotationProvider.start();
        LocationProvider.getInstance(null).start();
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentLocation = LocationProvider.getInstance(null).getCurrentLocation();
                System.out.println("ACC: " + currentLocation.getAccuracy() + "alt: " + currentLocation.getAltitude() + " lat:" + currentLocation.getLatitude() + " long:" + currentLocation.getLongitude());
                runOnUiThread(() -> placeModel());
            }
        }, 5000, 2000);
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Vector3 curLocation = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                curLocation.y = 0;
                addFakeToRealHistory(rotationProvider.getAzimuth() - getDegreesToFakeNorth());
                double dist = curLocation.length();
                double worldBearing = (360 + Math.toDegrees(Math.atan2(curLocation.x, -curLocation.z))) % 360;
                System.out.println("worldPos: " + curLocation);
                System.out.println("WORLDBEARING: " + worldBearing);
                double bearing = (540 + (worldBearing - getAVGDegreesFakeToReal())) % 360;
                System.out.println("Updateing with dist,bearing: " + dist + " " + bearing);
                LocationProvider.getInstance(null).updateLocations(dist, bearing);
            }
        }, 1000, 1000);

    }


    @Override
    protected void onDestroy() {
        arFragment.onDestroy();
        rotationProvider.stop();
        placementTimer.cancel();
        placementTimer.purge();
        LocationProvider.getInstance(null).stop();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        arFragment.onPause();
        rotationProvider.stop();
        placementTimer.cancel();
        placementTimer.purge();
        LocationProvider.getInstance(null).stop();
        super.onPause();
    }
}
