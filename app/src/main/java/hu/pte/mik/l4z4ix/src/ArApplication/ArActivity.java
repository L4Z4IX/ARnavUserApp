package hu.pte.mik.l4z4ix.src.ArApplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.navigation.LocationProvider;
import hu.pte.mik.l4z4ix.src.Components.navigation.Navigator;
import hu.pte.mik.l4z4ix.src.Components.navigation.RotationProvider;

public class ArActivity extends AppCompatActivity {

    private final Logger logger = Logger.getLogger("MainActivity4");
    private ArFragment arFragment;
    private AnchorNode currentAnchorNode;

    private volatile Location currentLocation;
    private final Location placementLocation = new Location("manual");
    private ModelRenderable renderable;
    private Timer placementTimer;
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

    TextView curLat;
    TextView curLng;
    TextView pointLat;
    TextView pointLng;
    TextView azimuth;
    TextView worldBear;
    TextView bearingOfPoints;
    TextView distOfPoints;
    TextView worldPos;
    TextView constRot;
    TextView camRot;
    Switch debugToggle;
    ConstraintLayout debugContainer;
    TextView targetPoint;
    ImageView arrowView;
    ConstraintLayout GPSState;
    Navigator navigator;
    Point target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        curLat = findViewById(R.id.curLat);
        curLng = findViewById(R.id.curLng);
        pointLat = findViewById(R.id.pointLat);
        pointLng = findViewById(R.id.pointLng);
        azimuth = findViewById(R.id.azimuth);
        worldBear = findViewById(R.id.worldBear);
        bearingOfPoints = findViewById(R.id.bearingOfPoints);
        distOfPoints = findViewById(R.id.pointDist);
        worldPos = findViewById(R.id.worldPos);
        constRot = findViewById(R.id.constRot);
        camRot = findViewById(R.id.camRot);
        debugToggle = findViewById(R.id.debugtoggle);
        debugContainer = findViewById(R.id.debugContainer);
        targetPoint = findViewById(R.id.targetPoint);
        arrowView = findViewById(R.id.arrowView);
        GPSState = findViewById(R.id.GPSState);

        target = (new Gson()).fromJson(getIntent().getStringExtra("point"), Point.class);
        navigator = new Navigator(Storage.INSTANCE.getConnections(), Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).toList(), target);

        rotationProvider = new RotationProvider(this);

        debugToggle.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            debugContainer.setVisibility(isChecked ? ConstraintLayout.VISIBLE : ConstraintLayout.GONE);
        }));

        ModelRenderable.builder()
                .setSource(this, R.raw.pointmarker).build().thenAccept(r -> renderable = r);


        arFragment = (ArFragment) getSupportFragmentManager()
                .findFragmentById(R.id.arfragment);

        String type = getIntent().getStringExtra("type");
        if (type.equals("admin")) {
            //admin sees only the selected point
            debugToggle.setVisibility(Switch.VISIBLE);
            placementLocation.setAltitude(target.getAltitude());
            placementLocation.setLatitude(target.getLatitude());
            placementLocation.setLongitude(target.getLongitude());
            pointLng.setText(placementLocation.getLongitude() + "");
            pointLat.setText(placementLocation.getLatitude() + "");
            targetPoint.setText(target.getName());
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (arFragment.getArSceneView() != null && arFragment.getArSceneView().getScene() != null) {
                    setupGPS();
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    private void setupGPS() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                var cam = arFragment.getArSceneView().getArFrame().getCamera();
                if (cam.getTrackingState() == TrackingState.TRACKING) {
                    GPSState.setVisibility(View.VISIBLE);
                    handleGPSAcc();
                } else
                    handler.postDelayed(this, 100);
            }
        }, 100);


    }

    private void handleGPSAcc() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null && currentLocation.getAccuracy() < 3) {
                    GPSState.setVisibility(View.GONE);
                    if (!getIntent().getStringExtra("type").equals("admin")) {
                        startNavigation();
                    }
                    arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> updateArrow());
                } else {
                    if (currentLocation != null) {
                        TextView accArea = GPSState.findViewById(R.id.gpsAcc);
                        accArea.setText(currentLocation.getAccuracy() + " meters");
                    }
                    handler.postDelayed(this, 500);
                }
            }
        }, 100);

    }

    private void startNavigation() {
        navigator.startNavigation(currentLocation);
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                Vector3 objectPose = currentAnchorNode.getWorldPosition();
                Point p = navigator.getCurrentPlacementLocation(objectPose, cameraPos);
                if (p != null) {
                    placementLocation.setAltitude(p.getAltitude());
                    placementLocation.setLongitude(p.getLongitude());
                    placementLocation.setLatitude(p.getLatitude());
                    runOnUiThread(() -> targetPoint.setText(p.getName()));
                } else {
                    runOnUiThread(() -> {
                        arFragment.onPause();
                        new AlertDialog.Builder(ArActivity.this)
                                .setTitle("Reached destination")
                                .setMessage("You have reached your selected destination.")
                                .setPositiveButton("Finish", (dialog, which) -> ArActivity.this.finish())
                                .show();
                    });
                    this.cancel();
                }

            }
        }, 1000, 1000);
    }

    private void updateArrow() {
        if (arFragment == null || arFragment.getArSceneView() == null || currentAnchorNode == null)
            return;
        if (arrowView.getVisibility() == View.GONE && currentAnchorNode != null)
            arrowView.setVisibility(View.VISIBLE);

        Camera camera = arFragment.getArSceneView().getScene().getCamera();
        Vector3 cameraPos = camera.getWorldPosition();
        Quaternion cameraRot = camera.getWorldRotation();
        Vector3 targetPos = currentAnchorNode.getWorldPosition();

        Vector3 dirToTarget = Vector3.subtract(targetPos, cameraPos);
        dirToTarget = new Vector3(dirToTarget.x, 0, dirToTarget.z).normalized();

        Vector3 camForward = Quaternion.rotateVector(cameraRot, Vector3.forward());
        camForward = new Vector3(camForward.x, 0, camForward.z).normalized();

        float angleRad = (float) Math.atan2(
                dirToTarget.x * camForward.z - dirToTarget.z * camForward.x,
                Vector3.dot(camForward, dirToTarget)
        );
        float angleDeg = (float) Math.toDegrees(angleRad);

        // Negative as android uses -Z for forward
        arrowView.setRotation(-angleDeg);
    }

    private double getDegreesToFakeNorth() {
        float[] zAxis = arFragment.getArSceneView().getArFrame().getCamera().getPose().getZAxis();
        float forwardX = zAxis[0];
        float forwardZ = zAxis[2];
        return (360 - Math.toDegrees(Math.atan2(forwardX, forwardZ))) % 360;
    }

    private void placeModel() {
        double distance = currentLocation.distanceTo(placementLocation);
        double bearing = (currentLocation.bearingTo(placementLocation) + 360) % 360;
        distOfPoints.setText(distance + "");
        bearingOfPoints.setText(bearing + "");
        float worldRotation = (float) Math.toRadians((bearing - getAVGDegreesFakeToReal() + 360) % 360);
        logger.log(Level.INFO, "bearing: " + bearing
                + " Constant rotation: " + getAVGDegreesFakeToReal()
                + " FinalAngle: " + Math.toDegrees(worldRotation));

        float dx = (float) (distance * Math.sin(worldRotation));
        float dy = 0;
        //for multi-level implementation, use:
        // (float) (currentLocation.getAltitude() - placementLocation.getAltitude());
        float dz = (float) (-distance * Math.cos(worldRotation));

        try {
            Pose targetPose = Pose.makeTranslation(dx, dy, dz);
            logger.log(Level.INFO, "calculated distance: " + distance + ",differentials : x: " + dx + ", y: " + dy + ", z: " + dz);

            Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(targetPose);
            if (currentAnchorNode != null) {
                currentAnchorNode.getAnchor().detach();
            }
            for (int i = 0; i < renderable.getSubmeshCount(); i++) {
                Material originalMaterial = renderable.getMaterial(i);
                // Create a mutable copy to avoid modifying the original shared material
                Material debugMaterial = originalMaterial.makeCopy();
                // Disable depth testing (reading)
                debugMaterial.setBoolean("depthRead", false);
                // Disable depth writing
                debugMaterial.setBoolean("depthWrite", false);
                renderable.setMaterial(i, debugMaterial);
            }
            currentAnchorNode = new AnchorNode(anchor);
            currentAnchorNode.setParent(arFragment.getArSceneView().getScene());

            TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
            transformableNode.setParent(currentAnchorNode);
            transformableNode.setRenderable(renderable);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error placing model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        placementTimer = new Timer();
        fakeToRealHistory.clear();
        arFragment.onResume();
        Config conf = arFragment.getArSceneView().getSession().getConfig();
        conf.setFocusMode(Config.FocusMode.AUTO);
        conf.setDepthMode(Config.DepthMode.AUTOMATIC);
        conf.setInstantPlacementMode(Config.InstantPlacementMode.LOCAL_Y_UP);
        arFragment.getArSceneView().getSession().configure(conf);
        super.onResume();
        setupGPS();
        //Timer Scheduler for object placement
        rotationProvider.start();
        LocationProvider.getInstance(null).start();
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentLocation = LocationProvider.getInstance(null).getCurrentLocation();

                logger.log(Level.INFO, "Current location data:\r\n" +
                        "\tAccuracy: " + currentLocation.getAccuracy() +
                        "\r\n\tAltitude: " + currentLocation.getAltitude() +
                        "\r\n\tLatitude: " + currentLocation.getLatitude() +
                        "\r\n\tLongitude:" + currentLocation.getLongitude());
                runOnUiThread(() -> {
                    placeModel();
                    curLat.setText(currentLocation.getLatitude() + "");
                    curLng.setText(currentLocation.getLongitude() + "");
                });
            }
        }, 5000, 2000);
        placementTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Vector3 curLocation = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                curLocation.y = 0;
                float az = rotationProvider.getAzimuth();
                double degToFakeN = getDegreesToFakeNorth();
                addFakeToRealHistory(az - degToFakeN);
                double dist = curLocation.length();
                double worldBearing = (360 + Math.toDegrees(Math.atan2(curLocation.x, -curLocation.z))) % 360;
                double FakeToReal = getAVGDegreesFakeToReal();
                double bearing = (540 + (worldBearing - FakeToReal)) % 360;
                logger.log(Level.INFO, "Current world readings: " +
                        "\r\n\tWorld position: " + curLocation +
                        "\r\n\tWorld bearing: " + worldBearing
                );
                logger.log(Level.INFO, "Set gps position transformer to distance: " + dist + ", bearing: " + bearing);
                LocationProvider.getInstance(null).updateLocations(dist, bearing);
                runOnUiThread(() -> {
                    azimuth.setText(az + "");
                    constRot.setText(FakeToReal + "");
                    worldBear.setText(worldBearing + "");
                    worldPos.setText(curLocation.toString());
                    camRot.setText(degToFakeN + "");
                });
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
