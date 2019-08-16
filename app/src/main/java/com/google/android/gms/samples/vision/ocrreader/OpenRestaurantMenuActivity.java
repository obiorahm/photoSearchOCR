package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantMenuAdapter;
import com.google.firebase.FirebaseApp;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.python.icu.util.BytesTrie;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;


/**
 * Created by mgo983 on 10/18/18.
 */

public class OpenRestaurantMenuActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {

    private RecyclerView recyclerView;
    private RestaurantMenuAdapter adapter;
    private String LOG_TAG = OpenRestaurantMenuActivity.class.getSimpleName();

    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;

    ImageView imageView;

    public static RelativeLayout last_parent_rl = null;

    public static AllOrders current_order;

    public static String ALL_ORDERS = "com.google.android.gms.samples.vision.ocrreader.AllOrders";


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        //we reuse the display_nearby_restaurants layout to display specific restaurant menus
        setContentView(R.layout.display_nearby_restaurants_intercept);

        current_order = new AllOrders();

        Log.d(LOG_TAG, "orders " + current_order);

        FoodItemAdapter.order.clear();

        last_rl_parent = null;

        Intent intent = getIntent();
        String url = intent.getStringExtra(RestaurantAdapter.RESTAURANT_URL);
        String name = intent.getStringExtra(RestaurantAdapter.RESTAURANT_NAME);
        Log.d(LOG_TAG, "my url " + url);

        //hide unnecessary views
        TextView textView1 = findViewById(R.id.current_location);
        textView1.setText(name);

        imageView = findViewById(R.id.location_image);


        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        recyclerView = findViewById(R.id.detected_location_list_view);
        adapter = new RestaurantMenuAdapter(this);

        // apparently the recycler view does not work without setting up a layout manager
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        FetchFoodItemWebPage fetchWebPage = new FetchFoodItemWebPage(this);
        Log.d(LOG_TAG , "current_url " + url);
        fetchWebPage.execute(url, "don't encode");


        //do back button
        ImageButton back_btn = findViewById(R.id.back_btn_dr);
        back_btn.setOnClickListener(view -> finish());


        ImageButton imageButtonClearOnSiteRecycler = findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonClearOnSiteRecycler.setOnClickListener( view -> takeDownRecyclerView());

        ImageButton imageButtonNext = findViewById(R.id.next_btn_dr);
        imageButtonNext.setOnClickListener(view -> getOrder());

        //print();

    }


    public void print(){
        //System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

        try{
        //InputStream inputStream = getAssets().open("farmhouse.png");
            InputStream modelIn = getAssets().open("en-lemmatizer.dict.txt");
            //InputStream modelIn = getClass().getResourceAsStream("/models/en-lemmatizer.dict");

        if (modelIn == null)
            Log.d(LOG_TAG, "modelIn is null ");

            SimpleLemmatizer simpleLemmatizer = new SimpleLemmatizer(modelIn);

            modelIn.close();

            String rawString = "thieves";

            //Instantiating POSTaggerME class

            InputStream inputStream = getAssets().open("en-pos-maxent.bin");

            POSModel modelpos = new POSModel(inputStream);

            //Instantiating POSTaggerME class

            POSTaggerME tagger = new POSTaggerME(modelpos);


            //Generating tags
            String[] tags = tagger.tag(rawString).split("/");


            String searchString = simpleLemmatizer.lemmatize(rawString, tags[1]);

            Log.d(LOG_TAG, "lemma " + searchString + " " +tags);

       }catch (IOException e){
            Log.e(LOG_TAG, e + " ");
        }
    }


    public void getOrder(){
        adapter.getSelectedItems(current_order);
        Log.d(LOG_TAG, current_order + " new order");
        adapter.reset_selected();

        Intent orderActivityIntent = new Intent(getApplicationContext(), NewOrderActivity.class);
        orderActivityIntent.putExtra(ALL_ORDERS, current_order);

        startActivity(orderActivityIntent);

    }

    public void takeDownRecyclerView(){


        //clear the recycler from the page
        RecyclerView recyclerView = findViewById(R.id.gridview_edit_meal);
        recyclerView.setVisibility(View.GONE);

        //clear textview if no results
        TextView textViewNoResult = findViewById(R.id.no_result);
        textViewNoResult.setVisibility(View.GONE);

        ImageButton imageButtonclearRecycler = findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonclearRecycler.setVisibility(View.GONE);

    }

    @Override
    public void processWebResults(Document document){

        getFoodItems(document);

        recyclerView.setAdapter(adapter);


    }



    public void getFoodItems(Document document){
        try{

            //get the Internet address of restaurant
            String internetAddress = document.select(".menu-link").attr("href");

            Log.d(LOG_TAG, "internet address " + internetAddress);

            Elements testElement = document.select(".menu-category");

            for(Element element : testElement){

                String categoryName = element.select(".category-name").text();
                String categoryDescription = element.select(".category-description").text();

                Log.d(LOG_TAG, categoryName);

                ArrayList categoryItems = new ArrayList();

                ArrayList itemsDescriptions = new ArrayList();

                Elements food_items = element.select(".item-title");

                Elements food_item_descriptions = element.select(".description");


                //get meal description

                for(Element food_item : food_items){
                    String string_food_item = food_item.text();

                    Log.d(LOG_TAG, string_food_item);
                    categoryItems.add(string_food_item);
                }



                for(Element description :food_item_descriptions){
                    String itemDescription = description.text();
                    itemsDescriptions.add(itemDescription);
                    Log.d(LOG_TAG, "description " + description);
                }
                String [] category = {categoryName, categoryDescription};
                adapter.addItem(category, categoryItems, itemsDescriptions);
            }



            if (internetAddress != null || ! (internetAddress.equals(""))){
                int start = internetAddress.indexOf("//");
                int start1 = internetAddress.indexOf(".");
                String newAddress = (internetAddress.length() > start + 2) ? internetAddress.substring(start + 2): internetAddress;
                int end = newAddress.indexOf("/");
                Log.d(LOG_TAG, "internet address " + start + " " + end);

                String extractAddress = (end > start) ? newAddress.substring(0, end) : internetAddress.substring(start1 + 1);

                Log.d(LOG_TAG, "internet address " + extractAddress);

                Glide.with(getApplicationContext()).load("https://logo.clearbit.com/" + extractAddress).into(imageView);
                imageView.setVisibility(View.VISIBLE);

            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }


    }

    public void hideOptionResults(){
//        hideSelectedOptionRecycler();

        hide_food_image_recycler();

    }

    public void hideSelectedOptionRecycler(){
        RecyclerView option_selection_recyclerView = findViewById(R.id.order_option_items);
        option_selection_recyclerView.setVisibility(View.GONE);
    }

    public void hide_food_image_recycler(){
        RecyclerView food_image_recyclerView = findViewById(R.id.gridview_edit_meal);
        food_image_recyclerView.setVisibility(View.GONE);

        TextView no_result_textView = findViewById(R.id.no_result);
        no_result_textView.setVisibility(View.GONE);

        ImageButton remove_food_image_imageButton = findViewById(R.id.cancel_gridview_edit_meal);
        remove_food_image_imageButton.setVisibility(View.GONE);

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
