package hu.pte.mik.l4z4ix.src.common.navigation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationProvider implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor rotationVectorSensor;
    private float azimuth;

    public RotationProvider(Context c) {
        sensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }


    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public float getAzimuth() {
        return azimuth;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        float[] remappedMatrix = new float[9];
        SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remappedMatrix
        );

        float[] orientation = new float[3];
        SensorManager.getOrientation(remappedMatrix, orientation);
        azimuth = (float) Math.toDegrees(orientation[0]);
        azimuth = (azimuth + 360) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
