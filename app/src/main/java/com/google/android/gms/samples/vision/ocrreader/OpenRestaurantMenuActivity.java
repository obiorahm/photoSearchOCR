package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantMenuAdapter;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.FetchWebPage;
import com.google.firebase.FirebaseApp;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Locale;

/**
 * Created by mgo983 on 10/18/18.
 */

public class OpenRestaurantMenuActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {

    private RecyclerView recyclerView;
    private RestaurantMenuAdapter adapter;
    private String LOG_TAG = OpenRestaurantMenuActivity.class.getSimpleName();

    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        //we reuse the display_nearby_restaurants layout to display specific restaurant menus
        setContentView(R.layout.display_nearby_restaurants);

        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        recyclerView = findViewById(R.id.detected_location_list_view);
        adapter = new RestaurantMenuAdapter(this, R.layout.horizontal_text);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        String url = intent.getStringExtra(GeographyActivity.RESTAURANT_URL);
        String name = intent.getStringExtra(GeographyActivity.RESTAURANT_NAME);
        Log.d(LOG_TAG, "my url " + url);



        FetchWebPage fetchWebPage = new FetchWebPage(this);
        fetchWebPage.execute(url, "don't encode");

    }

    @Override
    public void processWebResults(Document document){

        try {
            String testString = document.body().html();
            Elements testElement = document.select(".category-name" );
            for (Element element : testElement){
                String[] item = {element.text()};
                adapter.addItem(item);
                Log.d(LOG_TAG, element.attr("category-name") +" " + element.text());
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
