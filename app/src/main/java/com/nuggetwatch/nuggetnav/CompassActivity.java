package com.nuggetwatch.nuggetnav;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CompassActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    public static final String NA = "N/A";
    public static final String FIXED = "FIXED";
    // location min time
    private static final int LOCATION_MIN_TIME = 30 * 1000;
    // location min distance
    private static final int LOCATION_MIN_DISTANCE = 10;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private SensorManager sensorManager;
    // sensor gravity
    private Sensor sensorGravity;
    private Sensor sensorMagnetic;
    private LocationManager locationManager;
    private Location currentLocation;
    private Location nugget = new Location("Nugget");
    private GeomagneticField geomagneticField;
    private double bearing = 0;
    private TextView textDirection, textLat, textLong;
    private CompassView compassView;
    private boolean found = false;

    private boolean fixed = false;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLocationPermission();
        setContentView(R.layout.activity_compass);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        textDirection = (TextView) findViewById(R.id.text);
        compassView = (CompassView) findViewById(R.id.compass);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (getIntent().hasExtra("latitude")) {
            nugget.setLatitude(Double.valueOf(getIntent().getStringExtra("latitude")));
            nugget.setLongitude(Double.valueOf(getIntent().getStringExtra("longitude")));
            fixed = true;
            found = true;
        }

        onPermissionGranted();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_compass:
                    return true;
                case R.id.navigation_home:
                    Intent intentHome = new Intent(CompassActivity.this, MainActivity.class);
                    intentHome.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentHome);
                    return true;
                case R.id.navigation_map:
                    Intent intentMap = new Intent(CompassActivity.this, MapActivity.class);
                    intentMap.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intentMap);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove listeners
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
        locationManager.removeUpdates(this);
    }

    protected void onPermissionGranted() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this, sensorGravity, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetic, sensorManager.SENSOR_DELAY_NORMAL);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);

        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (gpsLocation != null) {
            currentLocation = gpsLocation;
        } else {
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (networkLocation != null) {
                currentLocation = networkLocation;
            } else {
                currentLocation = new Location(FIXED);
                currentLocation.setAltitude(1);
                currentLocation.setLatitude(-43.296482);
                currentLocation.setLongitude(170.36978);
            }
        }


        onLocationChanged(currentLocation);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        geomagneticField = new GeomagneticField(
                (float) currentLocation.getLatitude(),
                (float) currentLocation.getLongitude(),
                (float) currentLocation.getAltitude(),
                System.currentTimeMillis());

        if (!fixed) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofitInstance = new Retrofit
                    .Builder()
                    .baseUrl("https://nuggetwatch.co.nz/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            API apiService = retrofitInstance.create(API.class);

            Call<NearestModel> apiCall = apiService.nearest(
                    Double.toString(currentLocation.getLatitude()),
                    Double.toString(currentLocation.getLongitude()));

            apiCall.enqueue(new Callback<NearestModel>() {
                @Override
                public void onResponse(Call<NearestModel> call, Response<NearestModel> response) {
                    nugget.setLatitude(response.body().getLat());
                    nugget.setLongitude(response.body().getLng());
                    found = true;
                }

                @Override
                public void onFailure(Call<NearestModel> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Could not fetch nugget locations", Toast.LENGTH_LONG);
                }
            });
        }

        updateLocation(location);

    }

    public void updateLocation(Location location) {
        if (found) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            dfs.setDecimalSeparator('.');
            NumberFormat formatter = new DecimalFormat("#0.00", dfs);

            TextView distance = (TextView) findViewById(R.id.textDistance);
            float distanceTo = currentLocation.distanceTo(nugget) / 1000;
            String unit = "km";
            if (distanceTo < 1) {
                unit = "m";
                distanceTo *= 1000;
            }
            distance.setText(formatter.format(distanceTo) + " " + unit);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // Handle this some how
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (found) {
            boolean accelOrMagnetic = false;

            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Need to use a low pass filter to smooth data
                smoothed = LowPassFilter.filter(sensorEvent.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];
                accelOrMagnetic = true;
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                smoothed = LowPassFilter.filter(sensorEvent.values, geomagnetic);
                geomagnetic[0] = smoothed[0];
                geomagnetic[1] = smoothed[1];
                geomagnetic[2] = smoothed[2];
                accelOrMagnetic = true;
            }

            // Not sure what this does
            SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
            // Get bearing to target
            SensorManager.getOrientation(rotation, orientation);
            // East degrees of true north
            bearing = orientation[0];
            // Convert from radians to degrees
            bearing = Math.toDegrees(bearing) - currentLocation.bearingTo(nugget);

            // Fix difference between true and magnetic north
            if (geomagneticField != null) {
                bearing += geomagneticField.getDeclination();
            }

            if (bearing < 0) {
                bearing += 360;
            } else if (bearing > 360) {
                bearing -= 360;
            }

            compassView.setBearing((float) bearing);

            if (accelOrMagnetic) {
                compassView.postInvalidate();
            }

            updateTextDirection(bearing);
        }
    }

    private void updateTextDirection(double bearing) {
        int range = (int) (bearing / (360f / 16f));
        String dirText = "";
        if (range == 15 ||range == 0) dirText = "All roads lead to nuggets.";
        if (range == 14 ||range == 13) dirText = "A little to the right";
        if (range == 12 ||range == 11) dirText = "";
        if (range == 10 ||range == 9) dirText = "";
        if (range == 8 ||range == 7) dirText = "Nope. Turn around.";
        if (range == 6 ||range == 5) dirText = "I'm not going to tell you how to live your life.";
        if (range == 4 ||range == 3) dirText = "";
        if (range == 2 ||range == 1) dirText = "A little to the left";

        // char 176 is the degrees symbol
        // textDirection.setText("" + ((int) bearing) + ((char) 176) + " " + dirText);
        textDirection.setText(dirText);
    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    // https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation without blocking this thread
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.title_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(CompassActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled the result arrays are empty
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted();
                } else {
                    // Boo!
                }
                return;
            }
        }
    }
}
