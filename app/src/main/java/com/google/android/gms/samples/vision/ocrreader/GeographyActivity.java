package com.google.android.gms.samples.vision.ocrreader;

import android.*;
import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewSource;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by mgo983 on 10/5/18.
 */

public class GeographyActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener, GoogleApiClient.OnConnectionFailedListener, OnStreetViewPanoramaReadyCallback, Runnable{

    TextView globalTextView;
    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;


    public static String selected_restaurant = "";

    public static RecyclerView last_parent_di;

    public static String RESTAURANT_NAME = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_NAME";

    public static String RESTAURANT_URL = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_URL";

    public static String selected_url = "";

    private RecyclerView recyclerView;

    private RestaurantAdapter adapter;

    private String LOG_TAG = GeographyActivity.class.getSimpleName();

    protected GeoDataClient mGeoDataClient;

    protected PlaceDetectionClient mPlaceDetectionClient;

    private GoogleApiClient mGoogleApiClient;

    private Bundle mSavedInstance;

    double LONGITUDE = 151.20689;

    double LATITUDE = -33.87365;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public Handler handler = new Handler();

    public boolean test = false;



    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.display_nearby_restaurants);

        mSavedInstance = savedInstance;

            // construct a GeoDataClient
            mGeoDataClient = Places.getGeoDataClient(this);

            // construct a PlaceDetectionClient.
            mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addOnConnectionFailedListener(this)
                    .build();

        //getPhotos();

        AssetManager assetManager = getAssets();
        try{
            final String allAssets[] = assetManager.list("general");

            String locationIcon = allAssets[0];
            String restaurantIcon = allAssets[1];
            //InputStream ims = getAssets().open("location.png");
            ImageView locationImageView = findViewById(R.id.location_image);
            Glide.with(this).load(Uri.parse("file:///android_asset/general/" + locationIcon)).into(locationImageView);


        }catch (IOException e){
            Log.e(LOG_TAG, e +" ");
        }

        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        recyclerView = findViewById(R.id.detected_location_list_view);
        adapter = new RestaurantAdapter(this, R.layout.horizontal_text);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);



        //final TextView textView = findViewById(R.id.current_location);
        globalTextView = findViewById(R.id.current_location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();

            if (test){
                testData();
            }else{

                getCurrentLocation();
            }

        }


        ImageButton nxt_btn = findViewById(R.id.next_btn_dr);
        nxt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openRestaurantIntent = new Intent(getApplicationContext(), OpenRestaurantMenuActivity.class);
                openRestaurantIntent.putExtra(RESTAURANT_NAME, UseRecyclerActivity.selected_item);
                openRestaurantIntent.putExtra(RESTAURANT_URL, selected_url);
                startActivity(openRestaurantIntent);
            }
        });

        ImageButton back_btn = findViewById(R.id.back_btn_dr);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainActivityIntent);
            }
        });


    }

    @Override
    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView){
        streetViewPanoramaView.onCreate(mSavedInstance);
        streetViewPanoramaView.getStreetViewPanoramaAsync(this);

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
    public void onConnectionFailed(ConnectionResult connectionResult) {

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
                                ActivityCompat.requestPermissions(GeographyActivity.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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
                        getCurrentLocation();
                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
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

    globalTextView.setText("860 Hinman Ave, Evanston, IL, 60202");


    String items [][] ={{ "/il/evanston/272976-brothers-k-coffeehouse/menu/" ,"Brothers K Coffeehouse", "500 Main St Evanston, IL, 60202","","",""},
            { "/il/evanston/274001-lucky-platter/menu/", "Lucky Platter" ,  "514 Main St Evanston, IL, 60202","","",""},
            {   "/il/evanston/300039-siam-paragon/menu/", "Siam Paragon", "503 Main St Evanston, IL, 60202","","",""},
            {  "/il/evanston/126438-subway/menu/", "Subway", "506 Main St Evanston, IL, 60202","","",""}};

    for(String [] item : items){
        adapter.addItem(item);
    }
}

    @Override
    public void processWebResults(Document document){

        ArrayList<String> addresses = new ArrayList<>();

        try {
            Elements testElement = document.select(".restaurant-list-item" );
            for (Element element : testElement){
                //get name
                Elements name_element = element.select(".name a");
                String restaurant_name = name_element.text();
                String restaurant_url = name_element.attr("href");
                String restaurant_address = "";
                Elements addressElements = element.select(".address");
                for (Element address : addressElements){
                    restaurant_address += address.text() + " ";
                }
                String placeId = "";
                String longitude = "";
                String latitude = "";
                String item[] = {restaurant_url, restaurant_name, restaurant_address, placeId, longitude, latitude};
                addresses.add(restaurant_address);
                adapter.addItem(item);
                Log.d(LOG_TAG, "restaurant name " +restaurant_name + " restaurant_url " + restaurant_url + " restaurant_address " + restaurant_address);
            }



        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }
        getLocationFromAddress(addresses);
        recyclerView.setAdapter(adapter);


    }

    private void getLocationFromAddress(ArrayList<String> addresses){
        /*FetchRestaurantPlaceID fetchRestaurantPlaceID = new FetchRestaurantPlaceID(this);
        fetchRestaurantPlaceID.execute(addresses);*/

        FetchRestaurantLongLat fetchRestaurantLongLat = new FetchRestaurantLongLat(this);
        fetchRestaurantLongLat.execute(addresses);

    }

    @Override
    public void addPlaceIdToAdapter(HashMap<String, String[]> placesId){
        adapter.addPlaceIds(placesId);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void addLongLatToAdapter(HashMap<String, String[]> lngLatPack){
        adapter.addLngLat(lngLatPack);
        //recyclerView.setAdapter(adapter);
    }

    @Override
    public void getRestaurantPhoto(String placesId, final ImageView imageView){
//        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos("ChIJ4Yie9T_QD4gRt9XlU-4KZTI");
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placesId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                Log.d(LOG_TAG, photoMetadataBuffer.getCount() + "count");

                if (photoMetadataBuffer.getCount() == 0)
                    return;
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();

                        imageView.setImageBitmap(bitmap);

                        //Glide.with(getApplicationContext()).load(bitmap).into(locationImageView);

                    }
                });

                photoMetadataBuffer.release();
            }
        });
    }

    private void getCurrentLocation(){
        try{
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null){
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                        PlaceLikelihood firstplace = likelyPlaces.get(0);

                        String address = (String) firstplace.getPlace().getAddress();
                        String placeId = firstplace.getPlace().getId();

                        displayCurrentLocation(address, placeId);

                        Log.d(LOG_TAG, "address " + address + " placeId " + placeId);
                        likelyPlaces.release();
                    }
                }
            });

        }catch (SecurityException e){
            Log.e(LOG_TAG, e + " ");
        }
    }

    private void displayCurrentLocation(String address, String placeId){
        globalTextView.setText(address);
        ImageView locationImageView = GeographyActivity.this.findViewById(R.id.location_image);
        getRestaurantPhoto(placeId, locationImageView );
        //getPhotos(placeId);
        FetchWebPage fetchWebPage = new FetchWebPage(GeographyActivity.this);
        fetchWebPage.execute(address, "encode");
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
        //initialize firebase
        FirebaseApp.initializeApp(this);
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
            myTTS.setSpeechRate(0.6f);
        }

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
            //getCurrentLocation();

            //locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //getCurrentLocation();

            //locationManager.removeUpdates(this);
        }
    }

}
