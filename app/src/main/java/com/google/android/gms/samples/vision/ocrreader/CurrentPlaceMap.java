package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.samples.vision.ocrreader.Adapter.PossiblePlacesAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CurrentPlaceMap extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleMap mMap;

    Bundle mSavedInstance;

    PlacesClient placesClient;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    String LOG_TAG = CurrentPlaceMap.class.getSimpleName();

    private FusedLocationProviderClient fusedLocationProviderClient;

    private AddressResultReceiver resultReceiver;

    public static String RESTAURANT_ADDRESS = "com.google.android.gms.samples.vision.ocrreader.RESTAURANT_ADDRESS";



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
                List<LatLng> latLngList = new ArrayList<>();

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

                        latLngList.add(latLng);
                        //googleMap.addMarker(new MarkerOptions().position(latLng).title((String) data[PossiblePlacesAdapter.RESTAURANT_ADDRESS]));

                        //zoom = true;

                    }

                    //adapter.addItem(data);
                    placeLikelihood.getPlace().getPhotoMetadatas();

                }

                addHeatMap(latLngList, googleMap);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPlace, 18));

                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
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

                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(Marker marker) {
                                            startIntentService(location);
                                            return false;
                                        }
                                    });






                                }

                            }
                        });



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


    private void addHeatMap(List<LatLng> list, GoogleMap mMap) {


        // Create a heat map tile provider, passing it the latlngs of the police stations.
        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
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
        public AddressResultReceiver(Handler handler){
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
            displayAddressOutput(addressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                //Toast.makeText(getString(R.string.address_found));
            }

        }
    }

    private void displayAddressOutput(String addressOutput){
        Log.d(LOG_TAG + " output address", addressOutput);

        Intent intent = new Intent(getApplicationContext(), GeographyActivity.class);
        intent.putExtra(RESTAURANT_ADDRESS, addressOutput);
        startActivity(intent);
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

}
