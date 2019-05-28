package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

// Add an import statement for the client library.
import com.google.android.libraries.places.api.Places;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewSource;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.FirebaseApp;


import java.io.IOException;

/**
 * Created by mgo983 on 10/5/18.
 */

public class GeographyActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener, GoogleApiClient.OnConnectionFailedListener, OnStreetViewPanoramaReadyCallback, Runnable, LocationListener{

    TextView globalTextView;
    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;

    public static String RESTAURANT_NAME = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_NAME";

    public static String RESTAURANT_URL = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_URL";

    public static String selected_url = "";

    private RecyclerView recyclerView;

    private RestaurantAdapter adapter;

    private String LOG_TAG = GeographyActivity.class.getSimpleName();

    private Bundle mSavedInstance;

    double LONGITUDE = 151.20689;

    double LATITUDE = -33.87365;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public Handler handler = new Handler();

    public boolean test = false;



    PlacesClient placesClient;

    String address;



    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.display_nearby_restaurants);

        mSavedInstance = savedInstance;

        String apiKey = "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU";
        Places.initialize(getApplicationContext(), apiKey);
        // Create a new Places client instance.
        placesClient = Places.createClient(this);


        // retrieve address
        Intent intent = getIntent();
        address = intent.getStringExtra(PlacesActivity.RESTAURANT_ADDRESS);


        AssetManager assetManager = getAssets();
        try{
            final String allAssets[] = assetManager.list("general");

            if (allAssets != null){
                String locationIcon = allAssets[0];
                ImageView locationImageView = findViewById(R.id.location_image);
                Glide.with(this).load(Uri.parse("file:///android_asset/general/" + locationIcon)).into(locationImageView);

            }




        }catch (IOException e){
            Log.e(LOG_TAG, e +" ");
        }catch (NullPointerException e){
            Log.e(LOG_TAG, e +" ");

        }

        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        recyclerView = findViewById(R.id.detected_location_list_view);
        adapter = new RestaurantAdapter(this);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);


        globalTextView = findViewById(R.id.current_location);

        if (test){
            testData();
        }else{

            if (address != null)
                getCurrentLocation(address);
        }


        ImageButton nxt_btn = findViewById(R.id.next_btn_dr);
        nxt_btn.setOnClickListener((View v) ->{
            Intent openRestaurantIntent = new Intent(getApplicationContext(), OpenRestaurantMenuActivity.class);
            openRestaurantIntent.putExtra(RESTAURANT_NAME, UseRecyclerActivity.selected_item);
            openRestaurantIntent.putExtra(RESTAURANT_URL, selected_url);
            startActivity(openRestaurantIntent);
        });

        ImageButton back_btn = findViewById(R.id.back_btn_dr);
        back_btn.setOnClickListener((View view) ->{
            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivityIntent);
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        globalTextView = findViewById(R.id.current_location);
        String lngLat = "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude();
        globalTextView.setText(lngLat);
        Log.d(LOG_TAG, "Latitude " + location.getLatitude() + " " + location.getLongitude() );
        FetchAddress fetchAddress = new FetchAddress();

        String y []= { Double.toString(location.getLatitude()),Double.toString(location.getLongitude()) };
        fetchAddress.execute(y);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude",provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude",provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude",status + "");
    }



    @Override
    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView, String longitude, String latitude){
        LONGITUDE = longitude.equals("")? LONGITUDE : Double.valueOf(longitude);
        LATITUDE = latitude.equals("") ? LATITUDE : Double.valueOf(latitude);

        streetViewPanoramaView.onCreate(mSavedInstance);
        streetViewPanoramaView.getStreetViewPanoramaAsync(this);
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        panorama.setPosition(new LatLng(LATITUDE, LONGITUDE), 50, StreetViewSource.OUTDOOR);
        curr_panorama = panorama;
        handler.postDelayed(this, 1000);


    }

    StreetViewPanorama curr_panorama;
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
    public void onConnectionFailed(@NonNull  ConnectionResult connectionResult) {

    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getCurrentLocation(address);
                    }

                } /*else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }*/
                //return;
            }
            break;

        }
    }

    //Activity thisActivity = this;


private void testData(){

        String address = "860 Hinman Ave, Evanston, IL, 60202";

    globalTextView.setText(address);


    String items [][] ={{ "/il/evanston/272976-brothers-k-coffeehouse/menu/" ,"Brothers K Coffeehouse", "500 Main St Evanston, IL, 60202","","",""},
            { "/il/evanston/274001-lucky-platter/menu/", "Lucky Platter" ,  "514 Main St Evanston, IL, 60202","","",""},
            {   "/il/evanston/300039-siam-paragon/menu/", "Siam Paragon", "503 Main St Evanston, IL, 60202","","",""},
            {  "/il/evanston/126438-subway/menu/", "Subway", "506 Main St Evanston, IL, 60202","","",""}};

    for(String [] item : items){
        adapter.addItem(item);
    }
}



    private void getCurrentLocation(String address){

        displayCurrentLocation(address);

    }


    private void displayCurrentLocation(String address){
        globalTextView.setText(address);
        if (address != null){
            FetchWebPage fetchWebPage = new FetchWebPage(GeographyActivity.this, adapter);
            fetchWebPage.execute(address, "encode");
        }
        recyclerView.setAdapter(adapter);
    }


    //checks whether the user has the TTS data installed. If it is not, the user will be prompted to install it.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            } else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }
    @Override
    public void onInit(int initStatus){
        String apiKey = "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU";
        Places.initialize(getApplicationContext(), apiKey);

        //initialize firebase
        FirebaseApp.initializeApp(this);

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


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
