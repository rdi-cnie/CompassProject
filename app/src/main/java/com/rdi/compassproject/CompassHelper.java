package com.rdi.compassproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rudi on 29-12-2016.
 */

public class CompassHelper implements SensorEventListener, LocationListener {

    private final static String TAG = CompassHelper.class.getSimpleName();

    private final float SMOOTH_FACTOR = 0.97f;

    private Context context;

    private LocationManager mlocationManager;

    private Coordinate markCoordinate;
    private Coordinate phoneCoordinate;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Sensor mGravitySensor;

    private boolean isGravityAvailable = false;
    private boolean isAccelerometerAvailable = false;
    private boolean isMagnetometerAvailable = false;

    private float[] accelerometerData = new float[3];
    private float[] magnetometerData = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];

    private float currentDegree = 0f;
    private float currentSin;
    private float currentCos;

    private List<CompassListener> listeners = new ArrayList<>();

    /**
     * Interface for compass listeners.
     */
    public interface CompassListener {
        void onNewCompassMeasurement(float degrees);
        void onNewGPSMeasurement(Coordinate here);
        void onCompassStopped();
        void onCompassStarted();
    }

    public CompassHelper(final Context context) {
        this.context = context;

        // Compass
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        // Location
        mlocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Initialize
        markCoordinate = new Coordinate();
        phoneCoordinate = new Coordinate();
    }

    /**
     * Add a compass listener.
     * @param listener
     */
    public void addListener(CompassListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove a compass listener.
     * @param listener
     */
    public void removeListener(CompassListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Start measurements.
     */
    public void startCompass() {
        isAccelerometerAvailable = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        isMagnetometerAvailable = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        isGravityAvailable = mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_GAME);

        // If Gravity sensor is available there is no need for Accelerometer
        if( isGravityAvailable ) {
            mSensorManager.unregisterListener(this, this.mAccelerometer);
        }

        // Location: request updates
        try {
            mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
        }catch (SecurityException e){
            //e.printStackTrace();
            Log.e(TAG, "Location not available");
        }

        for (CompassListener listener : listeners) {
            listener.onCompassStarted();
        }

        if ( ( isGravityAvailable || isAccelerometerAvailable ) && isMagnetometerAvailable ) {
            Log.i(TAG, "Sensors are available.");
        } else {
            Log.e(TAG, "Sensors unavailable.");
            stopCompass();
        }

    }

    /**
     * Stop measurements and unregister sensor listeners.
     */
    public void stopCompass() {
        this.mSensorManager.unregisterListener(this, this.mAccelerometer);
        this.mSensorManager.unregisterListener(this, this.mMagnetometer);
        this.accelerometerData = new float[3];
        this.magnetometerData = new float[3];
        this.currentDegree = 0f;

        try {
            this.mlocationManager.removeUpdates(this);
        }catch (SecurityException e){
            // Failure
        }

        for (CompassListener listener : listeners) {
            listener.onCompassStopped();
        }
    }

    /**
     * Set mark coordinates
     * @param markCoordinate Coordinate
     */
    public void setMarkCoordinate(Coordinate markCoordinate) {
        this.markCoordinate = markCoordinate;
    }

    /**
     * Calculate bearing between phone location and mark location
     * @return Bearing angle
     */
    public float getMarkBearing(){
        return (float) Utils.calculateBearing(phoneCoordinate, markCoordinate);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Get sensor data types
        switch ( sensorEvent.sensor.getType() ) {
            case Sensor.TYPE_GRAVITY:
                accelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerData = sensorEvent.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magnetometerData = sensorEvent.values.clone();
                break;
            default: return;
        }

        // Calculate degree
        if ( SensorManager.getRotationMatrix( rotationMatrix, null, accelerometerData, magnetometerData ) ) {
            // Get degrees from sensors
            currentDegree = (int) ( Math.toDegrees( SensorManager.getOrientation( rotationMatrix, orientation )[0] ) + 360 ) % 360;

            // Smooth degress using sine / cosine
            currentSin = SMOOTH_FACTOR * currentSin + (1-SMOOTH_FACTOR) * (float) Math.sin( Math.toRadians(currentDegree) );
            currentCos = SMOOTH_FACTOR * currentCos + (1-SMOOTH_FACTOR) * (float) Math.cos( Math.toRadians(currentDegree) );
            currentDegree = (float) Math.toDegrees(Math.atan2(currentSin, currentCos));

            // Inform listeners
            for (CompassListener listener : listeners) {
                listener.onNewCompassMeasurement( - this.currentDegree );
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not used.
    }

    @Override
    public void onLocationChanged(Location location) {
        phoneCoordinate = new Coordinate(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "Got GPS coordinates: " + phoneCoordinate.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}
