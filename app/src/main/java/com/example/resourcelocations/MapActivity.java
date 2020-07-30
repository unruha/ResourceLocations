package com.example.resourcelocations;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //responsible for tracking current location of device
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //loads suggestions as user types address etc.
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Location mLastKnownLocation;
    //update user location if last known location is null
    private LocationCallback locationCallback;

    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private Button btnFind;
    private FloatingActionButton fabFoodShelter;
    private FloatingActionButton fabMedicalCenter;

    private ArrayList<FoodShelter> foodShelters;
    private Set<Marker> foodShelterMarkers;
    HashMap<Marker, FoodShelter> markerFoodShelterHashMap;

    private ArrayList<MedicalCenter> medicalCenters;
    private Set<Marker> medicalCenterMarkers;
    HashMap<Marker, MedicalCenter> markerMedicalCenterHashMap;

    String FoodShelterJSONData;
    String MedicalCenterJSONData;

    private boolean foodShelterMarkersOn;
    private boolean medicalCenterMarkersOn;

    private final float DEFAULT_ZOOM = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //perform call to API to get food shelter data
        new GetFoodShelterJSONData().execute();
        new GetMedicalCenterJSONData().execute();

        FoodShelterJSONData = "";
        MedicalCenterJSONData = "";

        foodShelterMarkersOn = true;
        medicalCenterMarkersOn = true;

        foodShelters = new ArrayList<FoodShelter>();
        foodShelterMarkers = new HashSet<Marker>();
        markerFoodShelterHashMap = new HashMap<Marker, FoodShelter>();

        medicalCenters = new ArrayList<MedicalCenter>();
        medicalCenterMarkers = new HashSet<Marker>();
        markerMedicalCenterHashMap = new HashMap<Marker, MedicalCenter>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        materialSearchBar = findViewById(R.id.searchBar);
        fabFoodShelter = findViewById(R.id.fabFoodShelter);
        fabMedicalCenter = findViewById(R.id.fabMedicalCenter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this, getString(R.string.GoogleAPIKey));
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        fabFoodShelter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (foodShelterMarkersOn) {
                    RemoveFoodShelterMarkers();
                    foodShelterMarkersOn = false;
                }
                else {
                    CreateFoodShelterMarkers();;
                    foodShelterMarkersOn = true;
                }
            }
        });

        fabMedicalCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (medicalCenterMarkersOn) {
                    RemoveMedicalCenterMarkers();
                    medicalCenterMarkersOn = false;
                }
                else {
                    CreateMedicalCenterMarkers();
                    medicalCenterMarkersOn = true;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setUpMap();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //determines what kind of marker the clicked marker is
                if (foodShelterMarkers.contains(marker)) {
                    FoodShelter foodShelter = markerFoodShelterHashMap.get(marker);
                    Intent intent = new Intent(MapActivity.this, FoodShelterInformationActivity.class);
                    intent.putExtra("FoodShelterObj", foodShelter);
                    startActivity(intent);
                    return true;
                }
                else if (medicalCenterMarkers.contains(marker)) {
                    MedicalCenter medicalCenter = markerMedicalCenterHashMap.get(marker);
                    Intent intent = new Intent(MapActivity.this, MedicalCenterInformationActivity.class);
                    intent.putExtra("MedicalCenterObj", medicalCenter);
                    startActivity(intent);
                    return true;
                }
                else {
                    Log.i("App", "Error identifying marker");
                    return false;
                }
            }
        });
    }

    //changes some UI settings such as moves down the "current location" button to the lower right
    private void setUpMap() {
        //Some UI changes to the map
        mMap.setMyLocationEnabled(true);
        //my location button is now shown
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        //want to move the current location button to the bottom left
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            //removing align parent top rule
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);
        }

        //check if gps is enabled or not and then request user to enable it if necessary
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        //stores whether or not the location settings are sufficient to carry out task
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //check if issue can be resolved
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        //51 is random number, just has to be same number as when we reference it
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //referencing the key to the resolvable
        if (requestCode == 51) {
            //user accepted the request? (enabled gps option)
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    //gets the current location of the device and zooms the camera in on that location
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        //ask mFusedLocationProviderClient to give us the last location
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        //if we found last known location
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            //"task" could still be null so we check if null is stored in mLastKnownLocation
                            //if task is not null, then move the camera to the last known location
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            }
                            else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }
                        else {
                            Toast.makeText(MapActivity.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //creates all the food shelter markers based on the list of food shelters
    private void CreateFoodShelterMarkers() {
        Log.i("App", "Creating food shelter markers");
        for (int i = 0; i < foodShelters.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(foodShelters.get(i).getLatitude(), foodShelters.get(i).getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bread)));
            markerFoodShelterHashMap.put(marker, foodShelters.get(i));
            foodShelterMarkers.add(marker);
            Log.i("App", "The name of the foodshelter set as a tag: " + foodShelters.get(i).getName());
        }
    }

    //creates all the medical center markers based on the list of medical centers
    private void CreateMedicalCenterMarkers() {
        Log.i("App", "Creating medical center markers");
        for (int i = 0; i < medicalCenters.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(medicalCenters.get(i).getLatitude(), medicalCenters.get(i).getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital)));
            markerMedicalCenterHashMap.put(marker, medicalCenters.get(i));
            medicalCenterMarkers.add(marker);
            Log.i("App", "The name of the foodshelter set as a tag: " + medicalCenters.get(i).getName());
        }
    }

    //removes all food shelter markers from the map
    private void RemoveFoodShelterMarkers() {
        Log.i("App", "Removing food shelter markers");
        for (Marker marker : foodShelterMarkers) {
            marker.remove();
        }
        foodShelterMarkers.clear();
        markerFoodShelterHashMap.clear();
    }

    //removes all medical center markers from the map
    private void RemoveMedicalCenterMarkers() {
        Log.i("App", "Removing medical center markers");
        for (Marker marker : medicalCenterMarkers) {
            marker.remove();
        }
        medicalCenterMarkers.clear();
        markerMedicalCenterHashMap.clear();
    }

    //parses a string that contains food shelter JSON data and stores it in food shelter objects and adds them to the food
        //shelters array
    private void parseFoodShelterJSON(String data){
        Log.i("App", "Parsing Food Shelter JSON");
        try {
            JSONArray jsonMainNode = new JSONArray(data);
            int jsonArrLength = jsonMainNode.length();

            for (int i = 0; i < jsonArrLength; i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                FoodShelter foodShelter = new FoodShelter(
                        jsonChildNode.getString("name"),
                        jsonChildNode.getString("description"),
                        jsonChildNode.getBoolean("availability"),
                        jsonChildNode.getDouble("latitude"),
                        jsonChildNode.getDouble("longitude"));
                foodShelters.add(foodShelter);
                Log.i("App", "Food shelter added " + foodShelter.getName());
            }

        } catch (JSONException e) {
            Log.i("App", "Error parsing data " + e.getMessage());
        }
    }

    //parses a string that contains medical center JSON data and stores it in medical center objects and adds them to the medical
        //centers array
    private void parseMedicalCenterJSON(String data){
        Log.i("App", "Parsing Medical Center JSON");
        try {
            JSONArray jsonMainNode = new JSONArray(data);
            int jsonArrLength = jsonMainNode.length();

            for (int i = 0; i < jsonArrLength; i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                MedicalCenter medicalCenter = new MedicalCenter(
                        jsonChildNode.getString("name"),
                        jsonChildNode.getString("description"),
                        jsonChildNode.getBoolean("availability"),
                        jsonChildNode.getDouble("latitude"),
                        jsonChildNode.getDouble("longitude"));

                medicalCenters.add(medicalCenter);
                Log.i("App", "medical center added " + medicalCenter.getName());
            }

        } catch (JSONException e) {
            Log.i("App", "Error parsing data " + e.getMessage());
        }
    }

    //gets food shelter JSON data from an API
    private class GetFoodShelterJSONData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String urlString = getString(R.string.APIFoodShelter);
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                int responseCode = con.getResponseCode();
                Log.i("App", "Sending 'GET' request to url: " + urlString);
                Log.i("App", "Response Code: " + responseCode);
                BufferedReader  in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.i("App", "Response: " + response.toString());

                String result = response.toString();

                return result;
            }
            catch (Exception e)
            {
                Log.i("App","Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FoodShelterJSONData = s;
            Log.i("App", "what the string holds: " + FoodShelterJSONData);
            parseFoodShelterJSON(FoodShelterJSONData);
            CreateFoodShelterMarkers();
        }
    }

    //gets medical center JSON data from an API
    private class GetMedicalCenterJSONData extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String URL = getString(R.string.APIMedicalCenter);
                URL url = new URL(URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                int responseCode = con.getResponseCode();
                Log.i("App", "Sending 'GET' request to url: " + URL);
                Log.i("App", "Response Code: " + responseCode);
                BufferedReader  in = new BufferedReader(new InputStreamReader(con.getInputStream()));

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                Log.i("App", "Response: " + response.toString());

                String result = response.toString();

                return result;
            }
            catch (Exception e)
            {
                Log.i("App","Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            MedicalCenterJSONData = s;
            Log.i("App", "what the string holds: " + MedicalCenterJSONData);
            parseMedicalCenterJSON(MedicalCenterJSONData);
            CreateMedicalCenterMarkers();
        }
    }
}
