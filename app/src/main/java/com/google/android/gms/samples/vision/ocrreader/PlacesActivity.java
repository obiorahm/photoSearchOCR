package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewSource;
import com.google.android.gms.samples.vision.ocrreader.Adapter.PossiblePlacesAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.FirebaseApp;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlacesActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener, OnStreetViewPanoramaReadyCallback, Runnable {

    private PossiblePlacesAdapter adapter;

    private String LOG_TAG = PlacesActivity.class.getSimpleName();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    PlacesClient placesClient;

    private int MY_DATA_CHECK_CODE = 0;

    private RecyclerView recyclerView;

    TextView globalTextView;

    public static String selectedAddress = null;

    public static String RESTAURANT_ADDRESS = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_ADDRESS";

    private Bundle mSavedInstance;

    double LONGITUDE = 151.20689;

    double LATITUDE = -33.87365;

    public Handler handler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.display_nearby_restaurants);

        String apiKey = "AIzaSyC-W1DgpWK4sfOPngXLGDA6j62aGxWMMaU";
        Places.initialize(getApplicationContext(), apiKey);
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

        AssetManager assetManager = getAssets();
        try{
            final String allAssets[] = assetManager.list("general");



            String locationIcon = "";
            if (allAssets[0] != null)
                    locationIcon = allAssets[0];
            //InputStream ims = getAssets().open("location.png");
            ImageView locationImageView = findViewById(R.id.location_image);
            Glide.with(this).load(Uri.parse("file:///android_asset/general/" + locationIcon)).into(locationImageView);


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
        adapter = new PossiblePlacesAdapter(this, R.layout.horizontal_text);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        //final TextView textView = findViewById(R.id.current_location);
        globalTextView = findViewById(R.id.current_location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();

                getCurrentLocation();


        }

        ImageButton nxtImageButton = findViewById(R.id.next_btn_dr);
        nxtImageButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), GeographyActivity.class);
            intent.putExtra(RESTAURANT_ADDRESS, selectedAddress);
            startActivity(intent);
        });


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
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(PlacesActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
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


    private void getCurrentLocation(){


        // Use fields to define the data types to return.
        List<Place.Field > placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES, Place.Field.PHOTO_METADATAS);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {

                for (com.google.android.libraries.places.api.model.PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Log.i(LOG_TAG, String.format("Place '%s' has likelihood: %f" + placeLikelihood.getPlace().getTypes(),
                            placeLikelihood.getPlace().getAddress(),
                            placeLikelihood.getLikelihood()));

                    Place place = placeLikelihood.getPlace();
                    Object [] data =  new Object[7];

                    // Get the photo metadata.
                    if (place.getPhotoMetadatas() != null){
                        PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

                        // Get the attribution text.
                        String attributions = photoMetadata.getAttributions();

                        // Create a FetchPhotoRequest.
                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(500) // Optional.
                                .setMaxHeight(300) // Optional.
                                .build();
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();

                            data[PossiblePlacesAdapter.BITMAP] = bitmap;
                            adapter.notifyDataSetChanged();
                        });

                    }
                        LatLng latLng = placeLikelihood.getPlace().getLatLng();


                        data[PossiblePlacesAdapter.RESTAURANT_ADDRESS] = placeLikelihood.getPlace().getAddress();
                        data[PossiblePlacesAdapter.LATITUDE] = latLng.latitude;
                        data[PossiblePlacesAdapter.LONGITUDE] = latLng.longitude;

                        adapter.addItem(data);
                        placeLikelihood.getPlace().getPhotoMetadatas();

                }


                //get first place
                PlaceLikelihood firstPlace = response.getPlaceLikelihoods().get(0);
                //displayCurrentLocation(firstPlace.getPlace().getAddress());

                //get next 10 places
                /*for (int i = 0; i < 10; i++){
                    PlaceLikelihood currentPlace = response.getPlaceLikelihoods().get(i);
                    adapter.addItem(currentPlace.getPlace().getAddress());
                }*/



            })).addOnFailureListener((exception) -> {
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


    private void displayCurrentLocation(String address){
        globalTextView.setText(address);

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
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        }
    }


}
