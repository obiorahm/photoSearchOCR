package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CurrentPlaceMap extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_id);
        mapFragment.getMapAsync(this);



    }


    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;

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
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
