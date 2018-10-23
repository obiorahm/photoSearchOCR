package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Locale;

/**
 * Created by mgo983 on 10/5/18.
 */

public class GeographyActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener{

    FusedLocationProviderClient mFusedLocationClient;
    Handler handler = new Handler();
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(handler);
    String mAddressOutput;
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




    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.display_nearby_restaurants);

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


        final TextView textView = findViewById(R.id.current_location);
        globalTextView = findViewById(R.id.current_location);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            checkPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                textView.setText("Longitute: " + location.getLongitude()+" Latitude: " + location.getLatitude());
                                startIntentService(location);
                                Log.d("Geography output", location.getLongitude() + "");
                            }else{
                                Log.d("Geography output", "location is null");

                            }
                        }
                    }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Geography", e + " ");
                }
            });


        }catch (SecurityException e){
            Log.e("Geography", e + " ");
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

    }


    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }

    }

    protected void  startIntentService(Location location){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    Activity thisActivity = this;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                mAddressOutput = "";
            }

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
               // showToast(getString(R.string.address_found));
            }
            displayAddressOutput();

        }

        private void displayAddressOutput(){
            globalTextView.setText(mAddressOutput + " ");
            FetchWebPage fetchWebPage = new FetchWebPage(thisActivity);
            fetchWebPage.execute(mAddressOutput, "encode");
        }
    }

    @Override
    public void processWebResults(Document document){

        try {
            String testString = document.body().html();
            Elements testElement = document.select(".restaurant-list .name a" );
            for (Element element : testElement){
                String[] item = {element.attr("href"), element.text()};
                adapter.addItem(item);
                Log.d(LOG_TAG, element.attr("href") +" " + element.text());
            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

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
    public void onResume(){
        super.onResume();

    }

}