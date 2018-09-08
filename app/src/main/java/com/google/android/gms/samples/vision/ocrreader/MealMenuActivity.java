package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.ImageAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

/**
 * Created by mgo983 on 8/5/18.
 */

public class MealMenuActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {

    private int MY_DATA_CHECK_CODE = 0;
    public static TextToSpeech myTTS;
    private RecyclerWordAdapter adapter;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_meal);



        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        //get meal from intent
        String mealText = getIntent().getStringExtra(DetectImageActivity.MEAL_TO_GET);

        if (mealText == null)
            mealText = "Beer-Battered Wisconsin Cheese Curds" /*getIntent().getStringExtra(MainActivity.MEAL)*/;

        mealText = mealText.replaceAll("[0-9]","");

        //final String mealText = getIntent().getStringExtra(MainActivity.MEAL);
        TextView mealTextView = (TextView) findViewById(R.id.meal_text);
        mealTextView.setText(mealText);
        //mealTextView.setText("Pepperoni Pizza with pineapple toppings");

        myTTS = new TextToSpeech(this, this);

        // we need finalMealText for static click context
        final String finalMealText = mealText;

        mealTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(finalMealText, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        final TextView textViewNoResult = (TextView) findViewById(R.id.no_result);
        textViewNoResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTTS.speak(textViewNoResult.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        //initialize adapter
        adapter = new RecyclerWordAdapter(this, R.layout.gridview_item, myTTS, mealText);

        //place each word in a single text view
        String [] wordInMealText = mealText.split(" ");


        //Async task expects a string array, so we make one of length one
        // adapter is set in the fetchMeal AsyncTask
        String[] mealSearchString = new String[1];
        mealSearchString[0] = mealText;
        FetchMealDetails fetchMealDetails = new FetchMealDetails(adapter, this);
        fetchMealDetails.execute(mealSearchString);

        //GridView wholeWordGridView = (GridView) findViewById(R.id.gridview_edit_meal);
        //wholeWordGridView.setAdapter(adapter);

        /* for testing replace FetchMealDetails with testadapter
        testadapter(adapter);*/

        ImageButton imageButton = (ImageButton) findViewById(R.id.back_to_food_list);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    private void testadapter(WordAdapter adapter){
        String foodItem [] = {"https://www.mcdonalds.com/content/dam/usa/documents/mcdelivery/mcdelivery_new11.jpg", "meat"};
        adapter.addItem(foodItem);
        adapter.addItem(foodItem);
        adapter.addItem(foodItem);
        adapter.addItem(foodItem);

        GridView wholeWordGridView = (GridView) findViewById(R.id.gridview_edit_meal);
        wholeWordGridView.setAdapter(adapter);
    }

@Override
public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView){
        int numberOfColumns = 4;
    recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
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
}
