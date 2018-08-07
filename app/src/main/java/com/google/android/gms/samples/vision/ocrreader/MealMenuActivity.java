package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.Locale;

/**
 * Created by mgo983 on 8/5/18.
 */

public class MealMenuActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;
    private WordAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_meal);

        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        final String mealText = "Beer-Battered Wisconsin Cheese Curds" /*getIntent().getStringExtra(MainActivity.MEAL)*/;
        TextView mealTextView = (TextView) findViewById(R.id.meal_text);
        mealTextView.setText(mealText);
        //mealTextView.setText("Pepperoni Pizza with pineapple toppings");

        myTTS = new TextToSpeech(this, this);

        mealTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(mealText, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        //initialize adapter
        adapter = new WordAdapter(this, R.layout.gridview_item, myTTS);

        //place each word in a single text view
        String [] wordInMealText = mealText.split(" ");
        for(int i = 0; i < wordInMealText.length; i++){
            adapter.addItem(wordInMealText[i]);
        }

        //Async task expects a string array, so we make one of length one
        String[] mealSearchString = new String[1];
        mealSearchString[0] = mealText;
        FetchMealDetails fetchMealDetails = new FetchMealDetails();
        fetchMealDetails.execute(mealSearchString);

        GridView wholeWordGridView = (GridView) findViewById(R.id.gridview_edit_meal);
        wholeWordGridView.setAdapter(adapter);

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
}
