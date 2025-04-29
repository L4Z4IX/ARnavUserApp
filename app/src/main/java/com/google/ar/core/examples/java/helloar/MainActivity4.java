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

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity4 extends AppCompatActivity {

    ArFragment arFragment;
    AnchorNode currentAnchorNode;

    private volatile Location currentLocation;
    private volatile double degreesToNorth;
    private final Location testLocation = new Location("manual");
    private ModelRenderable renderable;
    private int pointId;
    private final Timer placementTimer = new Timer();
    private RotationProvider rotationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        rotationProvider = new RotationProvider(this);
        testLocation.setAltitude(256.8999938964844);
        testLocation.setLatitude(46.07776651999999);
        testLocation.setLongitude(18.286225499999993);

        ModelRenderable.builder()
                .setSource(this, R.raw.pawn).build().thenAccept(r -> renderable = r);


        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arfragment);
        pointId = Integer.parseInt(getIntent().getStringExtra("pointId"));

    }

    private void placeModel() {
        double distance = currentLocation.distanceTo(testLocation);
        double bearing = (currentLocation.bearingTo(testLocation) + 360) % 360;
//        double distance = 5f;
//        double bearing = 300;
        double radToNorth = Math.toRadians(degreesToNorth);

        float[] zAxis = arFragment.getArSceneView().getArFrame().getCamera().getPose().getZAxis();
        float forwardX = zAxis[0];
        float forwardZ = zAxis[2];
        double radToFakeNorth = Math.toRadians((360 - Math.toDegrees(Math.atan2(forwardX, forwardZ))) % 360);

        float worldRotation = (float) Math.toRadians((Math.toDegrees(radToNorth - radToFakeNorth + Math.toRadians(bearing)) + 360) % 360);
        //double calculatedRotationRad = Math.toRadians((Math.toDegrees(angleCorrection - angleRadians) + 360) % 360);
        System.out.println("bearing: " + bearing
                + " azimuth: " + degreesToNorth
                + " SceneRotation: " + Math.toDegrees(radToFakeNorth)
                + " FinalAngle: " + Math.toDegrees(worldRotation)
        );
        float dx = (float) (distance * Math.sin(worldRotation));
        float dy = (float) (currentLocation.getAltitude() - testLocation.getAltitude());
        float dz = (float) (distance * Math.cos(worldRotation));

        try {
            Vector3 worldpose = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Pose targetPose = Pose.makeTranslation(dx - worldpose.x, dy - worldpose.y, dz - worldpose.z);
            System.out.println("CAMERA POS: " + arFragment.getArSceneView().getScene().getCamera().getWorldPosition());
            System.out.println("CALC DIST: " + distance + " D: " + dx + " " + dy + " " + dz);
            System.out.println("ANDROID SENSOR POSE: " + arFragment.getArSceneView().getArFrame().getAndroidSensorPose());
            System.out.println("T:" + targetPose);

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
        PointManager pointManager = new PointManager(Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPointSet().stream()).filter(x -> x.getId() == pointId).findFirst().get());
        pointManager.pointManagerCallback(new Location("ASD"));
        super.onResume();
        //Timer Scheduler for object placement
        rotationProvider.start();
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //currentLocation = SensorFusionLocationProcessor.getInstance().getCurrentEstimatedLocation();
                currentLocation = LocationProvider.getInstance(null).getCurrentLocation();
                degreesToNorth = rotationProvider.getAzimuth();
                System.out.println("ACC: " + currentLocation.getAccuracy() + "alt: " + currentLocation.getAltitude() + " lat:" + currentLocation.getLatitude() + " long:" + currentLocation.getLongitude());
                runOnUiThread(() -> placeModel());
            }
        }, 5000, 2000);

    }


    @Override
    protected void onDestroy() {
        arFragment.onDestroy();
        rotationProvider.stop();
        placementTimer.cancel();
        placementTimer.purge();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        arFragment.onPause();
        rotationProvider.stop();
        placementTimer.cancel();
        placementTimer.purge();
        super.onPause();
    }
}
