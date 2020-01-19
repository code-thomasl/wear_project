package com.example.wearapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends WearableActivity implements View.OnClickListener {
    // Misc variables (@TO DESCRIBE)
    int PERMISSION_ID = 12;
    private static final String TAG = "MainActivity";

    // Manage location (latitude, longitude)
    FusedLocationProviderClient mFusedLocationClient;
    TextView latTextView, lonTextView;

    // Display results
    private ArrayList<String> mNames = new ArrayList<>();
    private TextView textViewResult;
    private TextView textViewResult2;


    // Refresh
    private SensorManager sm;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    // URL to make requests to
    private String url = "https://hmin309-embedded-systems.herokuapp.com/message-exchange/messages";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Refresh
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        // Display results
        //textViewResult = findViewById(R.id.text_view_result);
        //textViewResult2 = findViewById(R.id.text_view_result2);

        // Get request (@TO REFACTO)
        OkHttpClient okHttpClient = new OkHttpClient();
        Request myGetRequest = new Request.Builder()
                .url("https://hmin309-embedded-systems.herokuapp.com/message-exchange/messages/")
                .build();


        okHttpClient.newCall(myGetRequest).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                //le retour est effectué dans un thread différent
                final String text = response.body().string();
                final int statusCode = response.code();

                final GsonBuilder gsonBuilder = new GsonBuilder();
                final Gson gson = gsonBuilder.create();
                //gson.fromJson(text, Message2.class);
                //Message2 message = gson.fromJson(text, Message2.class);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //maybe need a updateElement() for onResume and inside onCreate when
                        //shaking the Wear device.
                        Message2[] message = gson.fromJson(text, Message2[].class);
                        initElement(message);
                        //textViewResult.setText(text);
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                textViewResult.setText("ECHEC");

            }
        });

        // New message button
        ImageButton sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

        Log.d(TAG, "onCreate: started.");



        // Display location

        //latTextView = findViewById(R.id.latTextView);
        //lonTextView = findViewById(R.id.lonTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLastLocation();

        // Enables Always-on
        setAmbientEnabled();
    }

    // Manage sensor
    private final SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                long curTime = System.currentTimeMillis();

                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                    if (speed > SHAKE_THRESHOLD) {
                        //do something when device is shaken
                        Log.d(TAG, "Device shaken");
                    }

                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    @Override
    public void onClick(View view) {
        Log.d(TAG, "Send button has been clicked");
        //Toast.makeText(this, "Button Send clicked", Toast.LENGTH_SHORT).show();
        openSendMessageActivity();
    }

    public void openSendMessageActivity() {
        Intent intent = new Intent(this, SendMessageActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latTextView.setText(mLastLocation.getLatitude()+"");
            lonTextView.setText(mLastLocation.getLongitude()+"");
        }
    };

    // check permission for location access
    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    // request permission for location access
    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    // gives back result for location access permission response
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
            }
        }
    }

    // check if Location is enabled on the device
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


    // request the current location of the device, used in getLastLocation function
    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    // getting the Last Location of the Device (if user authorized it first)
    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    //latTextView.setText(location.getLatitude()+"");
                                    //lonTextView.setText(location.getLongitude()+"");
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    // function used to initialize elements, needs refactoring to incorporate other functions
    // currently in the onCreate
    private void initElement(Message2[] message) {
        Log.d(TAG, "initImagesBitmaps: preparing bitmaps.");

        for(int i = 0; i < message.length; i++) {
            mNames.add(message[i].getStudent_message());
        }
        initRecyclerView();
    }

    // initialize our RecylerView containing our list of messages (see RecyclerViewAdapter and Message classes)
    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: init reyclerview.");
        RecyclerView recyclerView = findViewById(R.id.message_list);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        Log.d(TAG, "ENTER Ambient");
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onExitAmbient() {
        Log.d(TAG, "EXIT Ambient");
        super.onExitAmbient();
    }

}

class Message2 {
    private int student_id;
    private float gps_lat;
    private float gps_long;
    private String student_message;


    public int getStudent_id() {
        return student_id;
    }

    public float getGps_lat() {
        return gps_lat;
    }

    public float getGps_long() {
        return gps_long;
    }

    public String getStudent_message() {
        return student_message;
    }
}

