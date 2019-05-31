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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RestaurantMenuAdapter;
import com.google.firebase.FirebaseApp;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Locale;

import afu.org.checkerframework.checker.oigj.qual.O;

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


    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        //we reuse the display_nearby_restaurants layout to display specific restaurant menus
        setContentView(R.layout.display_nearby_restaurants_intercept);

        FoodItemAdapter.order.clear();

        last_rl_parent = null;

        Intent intent = getIntent();
        String url = intent.getStringExtra(GeographyActivity.RESTAURANT_URL);
        String name = intent.getStringExtra(GeographyActivity.RESTAURANT_NAME);
        Log.d(LOG_TAG, "my url " + url);

        //hide unnecessary views
        TextView textView1 = findViewById(R.id.current_location);
        textView1.setText(name);
        //textView1.setVisibility(View.GONE);

        imageView = findViewById(R.id.location_image);
        //imageView1.setVisibility(View.GONE);


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
        fetchWebPage.execute(url, "don't encode");


        //do back button
        ImageButton back_btn = findViewById(R.id.back_btn_dr);
        back_btn.setOnClickListener(view -> finish());


        ImageButton imageButtonClearOnSiteRecycler = findViewById(R.id.cancel_gridview_edit_meal);
        imageButtonClearOnSiteRecycler.setOnClickListener( view -> takeDownRecyclerView());

        ImageButton imageButtonNext = findViewById(R.id.next_btn_dr);
        imageButtonNext.setOnClickListener(view -> getOrder());


    }


    public void getOrder(){
        Intent orderActivityIntent = new Intent(getApplicationContext(), OrderActivity.class);
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

        /*try {
            Elements testElement = document.select(".category-name" );
            for (Element element : testElement){
                String[] item = {element.text()};
                adapter.addItem(item);

                String[] foodItems = getFoodItems(element);
                Log.d(LOG_TAG, element.attr("category-name") +" " + element.text());
            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + " null pointer");

        }*/

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

                /*Elements ElementCategoryName  = element.select(".category-name");
                Elements ElementCategoryDescription = element.select(".category-description");
                String categoryName = "";
                String categoryDescription = "";*/

                String categoryName = element.select(".category-name").text();
                String categoryDescription = element.select(".category-description").text();

                /*for(Element category_element : ElementCategoryName){
                    categoryName = category_element.text();
                }*/
                Log.d(LOG_TAG, categoryName);

                ArrayList categoryItems = new ArrayList();
                Elements food_items = element.select(".item-title");

                for(Element food_item : food_items){
                    String string_food_item = food_item.text();
                    Log.d(LOG_TAG, string_food_item);
                    categoryItems.add(string_food_item);
                }
                String [] category = {categoryName, categoryDescription};
                adapter.addItem(category, categoryItems);
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

    @Override
    public void setView(RecyclerWordAdapter adapter, ArrayList<String []> edmanInfo){
        // make progress bar invisible
        ProgressBar searchingEdmame = findViewById(R.id.searching_edmame);
        searchingEdmame.setVisibility(View.GONE);

        int FIRST_ITEM = 0;

        // no results returned
        if (edmanInfo.size() == 0 || edmanInfo.get(FIRST_ITEM)[EdmanJasonReader.URL].equals(EdmanJasonReader.EMPTY)){
            TextView textViewNoResult = findViewById(R.id.no_result);
            textViewNoResult.setVisibility(View.VISIBLE);
        }else{
            for (String[] recipeInformation : edmanInfo){
                adapter.addItem(recipeInformation);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.gridview_edit_meal);
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridview_edit_meal);
        recyclerView.setVisibility(View.VISIBLE);

    }


    @Override
    public void displayTextByTextImage(ImageView imageView){

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
