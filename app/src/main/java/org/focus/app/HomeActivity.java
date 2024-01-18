package org.focus.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.focus.app.AccountsList.ExpandableListViewAdapter;
import org.focus.app.Constants.API;
import org.focus.app.Location.LocationService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SharedPreferences userPref;


    ExpandableListViewAdapter listViewAdapter;
    ExpandableListView expandableListView;

    List<String> accountList;

    HashMap<String, List<String>> accountDetailsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        expandableListView = findViewById(R.id.listViewAccounts);
        expandableListView.setGroupIndicator(null);


        userPref = getSharedPreferences("user", Context.MODE_PRIVATE);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();


        checkLocationPermission();
        convertAddressToLatLng();

        accountList = new ArrayList<String>();
        accountDetailsList = new HashMap<String, List<String>>();

        listViewAdapter = new ExpandableListViewAdapter(this, accountList, accountDetailsList);
        expandableListView.setAdapter(listViewAdapter);

        showAccountList();

        handler.post(fetchData);



    }

    // Function to convert an address to latitude and longitude
    private void convertAddressToLatLng() {
        String addressString = "Block 138 Lot 45 Aquino Street, Upper Bicutan, Taguig City, Metro Manila";
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(addressString, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();

                Log.d("Location", "Latitude: " + latitude + ", Longitude: " + longitude);
            } else {
                Log.d("Location", "Address not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Location", "Geocoder IOException");
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Display the location in a Toast
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();


                    StringRequest request = new StringRequest(Request.Method.POST, API.locate_api, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{

                            } catch (Error error){
                                error.printStackTrace();
                            }
                        }
                    }, error -> {
                        Toast.makeText(HomeActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }){

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
                            map.put("latitude", String.valueOf(latitude));
                            map.put("longitude", String.valueOf(longitude));
                            return map;
                        }
                    };

                    RequestQueue requestQueue = Volley.newRequestQueue(HomeActivity.this);
                    request.setRetryPolicy(new DefaultRetryPolicy(
                            10000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    requestQueue.add(request);


                }
            }
        };
    }

    private void sendLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showLocationPermissionRationale();
        } else {
            startLocationService();
        }
    }

    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app requires location permission to function properly. Please grant the permission.")
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                })
                .create()
                .show();
    }

    private void startLocationService() {
        // Create an intent to launch the LocationService class
        Intent serviceIntent = new Intent(this, LocationService.class);

        // Start the service as a foreground service
        ContextCompat.startForegroundService(this, serviceIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if the requestCode matches the location permission request code
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the location service
                startLocationService();
            } else {
                // Permission denied, show the location permission rationale again
                showLocationPermissionRationale();
            }
        }
    }


    private void showAccountList(){
        StringRequest request = new StringRequest(Request.Method.GET, API.locate_borrower_api, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("Collector Task");

                if (jsonObject.getBoolean("success")) {
                    // Temporary lists to hold new data
                    List<String> newAccountList = new ArrayList<>();
                    HashMap<String, List<String>> newAccountDetailsList = new HashMap<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);

                        String borrowerFullName = object.getString("borrower_full_name");
                        // Check if this borrower's name is already in the list
                        if (!accountList.contains(borrowerFullName)) {
                            newAccountList.add(borrowerFullName);

                            String address = object.getString("address");
                            String contactNo = object.getString("contact_no");
                            String debtAmount = object.getString("debt_amount");
                            String debtStatus = object.getString("debt_status");

                            List<String> details = new ArrayList<>();
                            details.add("Address: " + address);
                            details.add("Contact No: " + contactNo);
                            details.add("Debt Amount: " + debtAmount);
                            details.add("Debt Status: " + debtStatus);
                            newAccountDetailsList.put(borrowerFullName, details);
                        }
                    }

                    // Update the main lists only if there is new data
                    if (!newAccountList.isEmpty()) {
                        accountList.addAll(newAccountList);
                        accountDetailsList.putAll(newAccountDetailsList);
                        listViewAdapter.notifyDataSetChanged();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


    private Handler handler = new Handler();
    private Runnable fetchData = new Runnable() {
        @Override
        public void run() {

            // Call the method to fetch data
            showAccountList();

            // Repeat this runnable code block again every 5 seconds
            handler.postDelayed(this, 5000);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        sendLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendLocation();

        handler.removeCallbacks(fetchData);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if the ACCESS_FINE_LOCATION permission is not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Check if it should show the permission rationale
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display the location permission rationale to the user
                showLocationPermissionRationale();
            }
        } else {
            // Permission granted, start the location service
            startLocationService();
            sendLocation();
        }
    }





}