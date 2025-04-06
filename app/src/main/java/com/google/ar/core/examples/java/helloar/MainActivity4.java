package com.google.ar.core.examples.java.helloar;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.examples.java.common.helpers.CameraPermissionHelper;
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.ar.core.examples.java.common.helpers.LocationHelper;
import com.google.ar.core.examples.java.common.helpers.LocationPermissionHelper;
import com.google.ar.core.examples.java.common.samplerender.Framebuffer;
import com.google.ar.core.examples.java.common.samplerender.SampleRender;
import com.google.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;

import java.io.IOException;

public class MainActivity4 extends AppCompatActivity implements SampleRender.Renderer {

    private Session session;
    private GLSurfaceView surfaceView;
    private SampleRender render;
    private BackgroundRenderer backgroundRenderer;
    private boolean hasSetTextureNames = false;
    private DisplayRotationHelper displayRotationHelper;
    private Framebuffer virtualSceneFramebuffer;
    private LocationHelper locationHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        locationHelper = new LocationHelper(this);


        surfaceView = findViewById(R.id.surfaceview);
        displayRotationHelper = new DisplayRotationHelper(MainActivity4.this);
        render = new SampleRender(surfaceView, this, getAssets());

    }

    @Override
    public void onSurfaceCreated(SampleRender render) {
        backgroundRenderer = new BackgroundRenderer(render);
        virtualSceneFramebuffer = new Framebuffer(render, 1, 1);
    }

    @Override
    public void onSurfaceChanged(SampleRender render, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        virtualSceneFramebuffer.resize(width, height);
    }

    @Override
    public void onDrawFrame(SampleRender render) {
        if (session == null) {
            return;
        }

        if (!hasSetTextureNames) {
            session.setCameraTextureNames(
                    new int[]{backgroundRenderer.getCameraColorTexture().getTextureId()});
            hasSetTextureNames = true;
        }
        displayRotationHelper.updateSessionIfNeeded(session);
        Frame frame;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            Log.e(this.getClass().getSimpleName(), "Camera not available during onDrawFrame", e);
            return;
        }
        Camera camera = frame.getCamera();
        try {
            backgroundRenderer.setUseDepthVisualization(
                    render, false);
            backgroundRenderer.setUseOcclusion(render, false);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "Failed to read a required asset file", e);
            return;
        }


        backgroundRenderer.updateDisplayGeometry(frame);

        if (frame.getTimestamp() != 0) {
            backgroundRenderer.drawBackground(render);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {

        super.onResume();
        surfaceView.setVisibility(GLSurfaceView.VISIBLE);
        surfaceView.onResume();
        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);

                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }
                if (!LocationPermissionHelper.hasFineLocationPermission(this)) {
                    LocationPermissionHelper.requestFineLocationPermission(this);
                    return;
                }


                session = new Session(this);
                configureSession();
                //TODO Set up gps tracking and what to do with data HERE
                locationHelper.AddCallBack(x -> {
                    System.out.println(x.toString());
                });
                session.resume();
            } catch (UnavailableArcoreNotInstalledException
                    e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }
        }


    }

    private void configureSession() {
        Config config = session.getConfig();
        config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        } else {
            config.setDepthMode(Config.DepthMode.DISABLED);
        }
        config.setInstantPlacementMode(Config.InstantPlacementMode.DISABLED);
        config.setFocusMode(Config.FocusMode.AUTO);
        session.configure(config);
    }

    @Override
    protected void onDestroy() {
        if (session != null) {
            session.close();
            session = null;
        }
        super.onDestroy();
    }

    public void onPause() {
        super.onPause();
        if (session != null) {
            surfaceView.onPause();
            session.pause();
        }
    }


}
