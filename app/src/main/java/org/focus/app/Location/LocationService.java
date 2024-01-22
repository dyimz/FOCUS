package org.focus.app.Location;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.focus.app.Constants.API;
import org.focus.app.R;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double currentUserLatitude = 0.0;
    private double currentUserLongitude = 0.0;
    private boolean firstlocationzzz = false;
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 123;
    private SharedPreferences userPref;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize location-related components here
        Log.w("TAG", "oncreateLOCATION");
        userPref = getSharedPreferences("user", Context.MODE_PRIVATE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);



    }

    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Tracking location in background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true); // This makes the notification un-clearable

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createLocationRequest() {
        Log.w("TAG", "request");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        Log.w("TAG", "callback");

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w("TAG", "no location result");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Display the location in a Toast
                    currentUserLatitude = location.getLatitude();
                    currentUserLongitude = location.getLongitude();
                    Log.w("TAG", "LOCATION = " + currentUserLatitude + " , " + currentUserLongitude);

                    String latitudeString = String.valueOf(currentUserLatitude);
                    String longitudeString = String.valueOf(currentUserLongitude);

                    SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("latitude", latitudeString);
                    editor.putString("longitude", longitudeString);
                    editor.apply();

                    if(!firstlocationzzz){
                        firstlocationzzz = true;
                        sendLocationHandler.post(sendLocationRunnable);
                    }

                    Handler handler = new Handler();
                    Runnable updateNotificationTask = new Runnable() {
                        @Override
                        public void run() {
                            // Update the notification content here
                            updateNotification("Updated Location Service", "LOCATION = " + currentUserLatitude + " , " + currentUserLongitude);
                            handler.postDelayed(this, 5 * 60 * 1000); // Schedule the next update in 5 minutes
                        }
                    };
                    handler.post(updateNotificationTask);
                }
            }
        };
    }

    private void updateNotification(String contentTitle, String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true); // This makes the notification un-clearable

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    private void sendLocationToDatabase() {

        StringRequest request = new StringRequest(Request.Method.POST, API.locate_api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    Log.e("Location Submit", "Latitude: " + currentUserLatitude + " Longitude: " + currentUserLongitude);

                } catch (Error error) {

                    Log.e("Location Submit", "Error processing response", error);

                }
            }
        }, error -> {

            Log.e("Location Submit", "Error sending location. Attempt ", error);

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("latitude", String.valueOf(currentUserLatitude));
                map.put("longitude", String.valueOf(currentUserLongitude));
                return map;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        request.setRetryPolicy(new DefaultRetryPolicy(
                5000, // Consider reducing this timeout
                0, // No automatic retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);


    }

    private Handler sendLocationHandler = new Handler();
    private Runnable sendLocationRunnable = new Runnable() {
        @Override
        public void run() {
            sendLocationToDatabase();
            sendLocationHandler.postDelayed(this, 10000); // Repeat every 1 Minute
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
