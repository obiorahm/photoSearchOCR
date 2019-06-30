package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewSource;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;



public class CurrentPlaceMap extends UseRecyclerActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextToSpeech.OnInitListener, OnStreetViewPanoramaReadyCallback, Runnable {

    Bundle mSavedInstance;

    PlacesClient placesClient;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    String LOG_TAG = CurrentPlaceMap.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationProviderClient;

    public static String RESTAURANT_ADDRESS = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_ADDRESS";

    RestaurantAdapter adapter;

    RecyclerView recyclerView;

    private int MY_DATA_CHECK_CODE = 0;

    public static double LONGITUDE = 151.20689;

    public static double LATITUDE = -33.87365;

    public Handler handler = new Handler();





    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_id);
        mapFragment.getMapAsync(this);



        mSavedInstance = savedInstanceState;

        String apiKey = "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU";
        Places.initialize(getApplicationContext(), apiKey);
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);


        ImageButton expandmore = findViewById(R.id.expand_more);
        ImageButton expandless = findViewById(R.id.expand_less);
        StreetViewPanoramaView streetViewPanoramaView = findViewById(R.id.streetviewpanorama);

        expandmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandmore.setVisibility(View.GONE);
                expandless.setVisibility(View.VISIBLE);
                streetViewPanoramaView.setVisibility(View.VISIBLE);
            }
        });


        expandless.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandmore.setVisibility(View.VISIBLE);
                expandless.setVisibility(View.GONE);
                streetViewPanoramaView.setVisibility(View.GONE);
            }
        });
    }


    //checks whether the user has the TTS data installed. If it is not, the user will be prompted to install it.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //if (myTTS == null)
                myTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    private void getCurrentLocation(GoogleMap googleMap){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //addHeatMap(latLngList, googleMap);

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener((Location location)-> {
                if (location != null){
                    LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(newLatLng).title("leave it!"));
                    googleMap.addCircle(new CircleOptions().center(newLatLng)
                            .fillColor(Color.LTGRAY)
                            .radius(10.0)
                            .strokeColor(Color.RED)
                    );

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.clickable(true);

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 17));
                    startIntentService(location);

                    googleMap.setOnMarkerClickListener((Marker marker)-> {
                        //startIntentService(location);
                        return false;
                    });

                }

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(LOG_TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            checkPermission();
        }
    }


    protected void startIntentService(Location location){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, new AddressResultReceiver(new Handler()));
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }
    @Override
    public void onMapReady(GoogleMap googleMap){


        // retrieve address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();

            getCurrentLocation(googleMap);


        }


    }




    public void checkPermission(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("title")
                        .setMessage("title")
                        .setPositiveButton(R.string.ok, (DialogInterface dialogInterface, int i) ->{
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(CurrentPlaceMap.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            //return false;
        }

    }


    class AddressResultReceiver extends ResultReceiver{
        private AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData){
            if(resultData == null){
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                displayAddressOutput(addressOutput);
                //Toast.makeText(getString(R.string.address_found));
            }

        }
    }

    private void displayAddressOutput(String addressOutput){
        Log.d(LOG_TAG + " output address", addressOutput);

        /*Intent intent = new Intent(getApplicationContext(), GeographyActivity.class);
        intent.putExtra(RESTAURANT_ADDRESS, addressOutput);
        startActivity(intent);*/

        setUpRecycler(addressOutput);
    }



    private void setUpRecycler(String address){
        recyclerView = findViewById(R.id.on_map_recycler);
        if (myTTS == null)
                myTTS = new TextToSpeech(this,this);
        adapter = new RestaurantAdapter( this, R.layout.restaurant_adapter, myTTS);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        if (address != null){
            getNearbyPlaces(address);
        }
    }


    private void getNearbyPlaces(String address){

        displayCurrentLocation(address);

    }


    private void displayCurrentLocation(String address){
        if (address != null){
            FetchWebPage fetchWebPage = new FetchWebPage(this, adapter);
            fetchWebPage.execute(address, "encode");
        }
        recyclerView.setAdapter(adapter);
    }



    @Override
    public void setUpPanorama(){
        //LONGITUDE = longitude.equals("")? LONGITUDE : Double.valueOf(longitude);
        //LATITUDE = latitude.equals("") ? LATITUDE : Double.valueOf(latitude);

        StreetViewPanoramaView streetViewPanoramaView = findViewById(R.id.streetviewpanorama);

        streetViewPanoramaView.onCreate(mSavedInstance);
        streetViewPanoramaView.getStreetViewPanoramaAsync(this);
    }

    StreetViewPanorama curr_panorama;

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        panorama.setPosition(new LatLng(LATITUDE, LONGITUDE), 50, StreetViewSource.OUTDOOR);
        curr_panorama = panorama;
        handler.postDelayed(this, 1000);


    }



    private void panScreen(){
// Keeping the zoom and tilt. Animate bearing by 60 degrees in 1000 milliseconds.
        long duration = 2000;
        StreetViewPanoramaCamera camera =
                new StreetViewPanoramaCamera.Builder()
                        .zoom(curr_panorama.getPanoramaCamera().zoom)
                        .tilt(curr_panorama.getPanoramaCamera().tilt)
                        .bearing(curr_panorama.getPanoramaCamera().bearing - 60)
                        .build();
        curr_panorama.animateTo(camera, duration);
        handler.postDelayed(this, duration);
    }


    @Override
    public void run(){
        panScreen();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle connectionHint){

    }

    @Override
    public void onConnectionSuspended(int cause){

    }

    @Override
    public void onInit(int initStatus){

    }


    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (myTTS != null) {
            myTTS.stop();
            myTTS.shutdown();
        }
        super.onDestroy();
    }

}
