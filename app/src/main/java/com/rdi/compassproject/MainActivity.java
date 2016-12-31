package com.rdi.compassproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements CompassHelper.CompassListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST_LOCATION = 0;

    @BindView(R.id.compassView) CompassView mCompassView;
    @BindView(R.id.btnCoordinates) Button mBtnCoordinates;

    private CompassHelper mCompassHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Request permissions.
        if (!checkPermissions()){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }

        // Bind views.
        ButterKnife.bind(this);

        // Create CompassHelper and set up new Listener;
        mCompassHelper = new CompassHelper(this);
        mCompassHelper.addListener(this);

        // TODO Check permission for location
        mBtnCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TODO: Draw from a new thread.
                // Workaround to stop UI lags.
                mCompassHelper.stopCompass();

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog, null);

                final EditText editLatitude = (EditText) dialogView.findViewById(R.id.inputLatitude);
                final EditText editLongitude = (EditText) dialogView.findViewById(R.id.inputLongitude);

                alert.setView(dialogView);
                alert.setTitle(getResources().getString(R.string.dialog_title));
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Draw from a new thread.
                        mCompassHelper.startCompass();
                        try {
                            // Update mark coordinates
                            Coordinate tmpCoordinate = new Coordinate(Double.valueOf(editLatitude.getText().toString()), Double.valueOf(editLongitude.getText().toString()));
                            mCompassHelper.setMarkCoordinate(tmpCoordinate);
                            mCompassView.setMark( mCompassHelper.getMarkBearing() );
                        }catch (NumberFormatException e){
                            Log.i(TAG, "Invalid number");
                        }

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Start measurements.
                        mCompassHelper.startCompass();
                    }
                });

                alert.show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissions granted.
                    Log.i(TAG, "Permissions granted. Restart CompassHelper");
//                    mCompassHelper.stopCompass();
//                    mCompassHelper.removeListener(this);
//                    mCompassHelper = new CompassHelper(this);
//                    mCompassHelper.addListener(this);
//                    mCompassHelper.startCompass();
                } else {
                    // Permissions denied.
                }
            }

        }
    }

    protected void onPause() {
        super.onPause();
        mCompassHelper.stopCompass();
    }

    protected void onResume() {
        super.onResume();
        mCompassHelper.startCompass();
    }


    @Override
    public void onNewCompassMeasurement(float degrees) {
        mCompassView.setDegree(degrees);

    }

    @Override
    public void onNewGPSMeasurement(Coordinate here) {
        mCompassView.updateMark( mCompassHelper.getMarkBearing() );
    }

    @Override
    public void onCompassStopped() {
        Log.i(TAG, "Compass stopped.");
    }

    @Override
    public void onCompassStarted() {
        Log.i(TAG, "Compass started.");
    }

    private boolean checkPermissions(){
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ;
    }
}
