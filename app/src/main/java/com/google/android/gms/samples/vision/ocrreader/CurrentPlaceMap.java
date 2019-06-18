package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.samples.vision.ocrreader.Adapter.PossiblePlacesAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CurrentPlaceMap extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    Bundle mSavedInstance;

    PlacesClient placesClient;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    String LOG_TAG = CurrentPlaceMap.class.getSimpleName();




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




    }
    float zoom = 256;

    private void getCurrentLocation(GoogleMap googleMap){


        // Use fields to define the data types to return.
        List<Place.Field > placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES, Place.Field.PHOTO_METADATAS, Place.Field.ID);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            placesClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {

                LatLng firstPlace = new LatLng(-34, 151);

                for (com.google.android.libraries.places.api.model.PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Log.i(LOG_TAG, String.format("Place '%s' has likelihood: %f" + placeLikelihood.getPlace().getTypes() /*+ placeLikelihood.getPlace().getLatLng().toString()*/,
                            placeLikelihood.getPlace().getAddress(),
                            placeLikelihood.getLikelihood()));


                    //getGeocode(placeLikelihood.getPlace().getLatLng().latitude, placeLikelihood.getPlace().getLatLng().longitude);
                    Place place = placeLikelihood.getPlace();
                    Object [] data =  new Object[7];


                    // Get the photo metadata.
                    if (place.getPhotoMetadatas() != null){
                        PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);

                        // Get the attribution text.
                        //String attributions = photoMetadata.getAttributions();

                        // Create a FetchPhotoRequest.
                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                                .setMaxWidth(500) // Optional.
                                .setMaxHeight(300) // Optional.
                                .build();
                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                            Bitmap bitmap = fetchPhotoResponse.getBitmap();

                            data[PossiblePlacesAdapter.BITMAP] = bitmap;
                            //adapter.notifyDataSetChanged();
                        });

                    }
                    LatLng latLng = firstPlace = placeLikelihood.getPlace().getLatLng();


                    data[PossiblePlacesAdapter.RESTAURANT_ADDRESS] = placeLikelihood.getPlace().getAddress();

                    if (latLng != null){
                        data[PossiblePlacesAdapter.LATITUDE] = latLng.latitude;
                        data[PossiblePlacesAdapter.LONGITUDE] = latLng.longitude;

                        googleMap.addMarker(new MarkerOptions().position(latLng).title((String) data[PossiblePlacesAdapter.RESTAURANT_ADDRESS]));

                        //zoom = true;

                    }

                    //adapter.addItem(data);
                    placeLikelihood.getPlace().getPhotoMetadatas();

                }


                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPlace, 18));



                //get first place
                //PlaceLikelihood firstPlace = response.getPlaceLikelihoods().get(0);



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


    @Override
    public void onMapReady(GoogleMap googleMap){


        // retrieve address
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();

            getCurrentLocation(googleMap);


        }

        /*mMap = googleMap;

        // Add a marker in Sydney, Australia, and more the camera.
        LatLng sydney = new LatLng(-34, 151);
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(sydney);
        circleOptions.radius(100000.0);
        circleOptions.fillColor(Color.RED);
        circleOptions.strokeColor(Color.BLUE);
        mMap.addCircle(circleOptions);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.setOnMarkerClickListener((Marker marker) -> {
            return false;
        });*/

    }


    private void addHeatMap() {
        List<LatLng> list = null;

        // Get the data: latitude/longitude positions of police stations.
        try {
            list = readItems(R.raw.police_stations);
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
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
}
