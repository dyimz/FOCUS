package com.example.focus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private double currentUserLatitude = 0.0;
    private double currentUserLongitude = 0.0;
    private SharedPreferences userPref;
    private SharedPreferences locationPref;

    ExpandableListViewAdapter listViewAdapter;
    ExpandableListView expandableListView;

    List<String> accountList;
    List<String> distanceList;
    HashMap<String, List<String>> accountDetailsList;
    HashMap<String, String> borrowerIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startService(new Intent(this, LocationService.class));

        Log.w("TAG", "oncreateHOME");

        userPref = getSharedPreferences("user", Context.MODE_PRIVATE);

        locationPref = getSharedPreferences("location", Context.MODE_PRIVATE);

        expandableListView = findViewById(R.id.listViewAccounts);
        expandableListView.setGroupIndicator(null);

        accountList = new ArrayList<String>();
        distanceList = new ArrayList<String>();

        accountDetailsList = new HashMap<String, List<String>>();

        listViewAdapter = new ExpandableListViewAdapter(this, accountList, distanceList, accountDetailsList, borrowerIdMap);
        expandableListView.setAdapter(listViewAdapter);

        showAccountList();
        handler.post(fetchData);
    }

    private boolean shouldUpdateList(List<Float> newDistances) {
        if (distanceList.size() != newDistances.size()) {
            return true;
        }

        for (int i = 0; i < distanceList.size(); i++) {
            float oldDistance = Float.parseFloat(distanceList.get(i).split(" ")[0]);
            float newDistance = newDistances.get(i);
            if (Math.abs(oldDistance - newDistance) > 0.5) {
                return true;
            }
        }
        return false;
    }

    private void showAccountList() {



        StringRequest request = new StringRequest(Request.Method.GET, API.locate_borrower_api, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("Collector Task");

                if (jsonObject.getBoolean("success")) {
                    List<String> newAccountList = new ArrayList<>();
                    HashMap<String, List<String>> newAccountDetailsList = new HashMap<>();
                    List<String> newDistanceList = new ArrayList<>();
                    List<Float> newDistances = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String borrowerFullName = object.getString("borrower_full_name");
                        String borrowerID = object.getString("borrower_id");
                        borrowerIdMap.put(borrowerFullName, borrowerID);

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

                            String latitude = object.getString("latitude");
                            String longitude = object.getString("longitude");

                            Double borrowerLatitude = Double.valueOf(latitude);
                            Double borrowerLongitude = Double.valueOf(longitude);

                            String collectorLatitude = locationPref.getString("latitude", "0.0");
                            String collectorLongitude = locationPref.getString("longitude", "0.0");

                            currentUserLatitude = Double.parseDouble(collectorLatitude);
                            currentUserLongitude = Double.parseDouble(collectorLongitude);

                            float[] results = new float[1];
                            Location.distanceBetween(currentUserLatitude, currentUserLongitude, borrowerLatitude, borrowerLongitude, results);
                            float distanceInKilometers = results[0] / 1000;
                            newDistances.add(distanceInKilometers);

                            String distanceString = String.format(Locale.getDefault(), "%.2f km", distanceInKilometers);
                            newDistanceList.add(distanceString);





                        }
                    }

                    if (shouldUpdateList(newDistances)) {
                        // Sort based on distances
                        List<Integer> indices = new ArrayList<>();
                        for (int i = 0; i < newDistances.size(); i++) {
                            indices.add(i);
                        }
                        Collections.sort(indices, (i1, i2) -> Float.compare(newDistances.get(i1), newDistances.get(i2)));

                        // Apply sorting to the main lists
                        List<String> sortedAccountList = new ArrayList<>();
                        List<String> sortedDistanceList = new ArrayList<>();
                        HashMap<String, List<String>> sortedAccountDetailsList = new HashMap<>();

                        for (int index : indices) {
                            String account = newAccountList.get(index);
                            sortedAccountList.add(account);
                            sortedDistanceList.add(newDistanceList.get(index));
                            sortedAccountDetailsList.put(account, newAccountDetailsList.get(account));
                        }

                        if (!newAccountList.isEmpty()) {
                            accountList.clear();
                            distanceList.clear();
                            accountDetailsList.clear();

                            accountList.addAll(sortedAccountList);
                            distanceList.addAll(sortedDistanceList);
                            accountDetailsList.putAll(sortedAccountDetailsList);


                            listViewAdapter.notifyDataSetChanged();

                        }

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
}