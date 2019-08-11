package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.ShoppingCartAdapter;
import com.google.firebase.FirebaseApp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by mgo983 on 1/10/19.
 */

public class OrderActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener{

    private static  final int NUMBER_OF_IMAGES = 1;

    ShoppingCartAdapter shoppingCartAdapter;

    private static final String LOG_TAG = OrderActivity.class.getSimpleName();

    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;




    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.geography_order);

        Serializable serializableOrder = getIntent().getSerializableExtra(OpenRestaurantMenuActivity.ALL_ORDERS);
        AllOrders currentOrder = (AllOrders) serializableOrder;

        //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        shoppingCartAdapter = new ShoppingCartAdapter(this);

        TextView textView = findViewById(R.id.order_text_view);

        RecyclerView recyclerView = findViewById(R.id.shopping_cart_recycler);

        //fetchMealUrl(current_item_name);

        String complete_text = "";
        String [] order_items = new String[FoodItemAdapter.order.size()];
        int count = 0;
        Iterator resultIterator = FoodItemAdapter.order.entrySet().iterator();
        while (resultIterator.hasNext()) {
            HashMap.Entry item = (HashMap.Entry) resultIterator.next();
            String current_item_name = (String) item.getKey();


            complete_text += current_item_name;
            order_items[count] = current_item_name;
            count++;

            Object[] orderData = (Object[]) item.getValue();
            //Order current_order = (Order) item.getValue();
            Order current_order = (Order) orderData[FoodItemAdapter.ORDER];
            Integer[] order_details = current_order.getOrderValues();


            for (int i = 0; i < Order.OPTION_SIZE; i++){
                complete_text += order_details[i] + " ";
            }
        }

        FetchMealDetails fetchMealDetails = new FetchMealDetails(this, NUMBER_OF_IMAGES, FetchMealDetails.FOR_ORDER);
        fetchMealDetails.execute(order_items);

        textView.setText(complete_text);

        WordNetHypernyms wordNetHypernyms = new WordNetHypernyms();

        String[] hypernyms = {"drink", "liquid"};
        wordNetHypernyms.getHypernym(hypernyms,"chicken lamb seafood turkey bacon ham stake beef wings sausage");

    }


    @Override
    public void setView(RecyclerWordAdapter adapter, ArrayList<String[]> edamanInfo){


        for (String [] info : edamanInfo){


            String current_item_name = info[EdmanJasonReader.NAME];

            Object[] orderData = FoodItemAdapter.order.get(current_item_name);
            Order current_order = (Order) orderData[FoodItemAdapter.ORDER];

            //Order current_order = (Order) FoodItemAdapter.order.get(current_item_name);
            Integer[] order_details = current_order.getOrderValues();

            String url = info[EdmanJasonReader.URL];


            shoppingCartAdapter.addItem(current_item_name, order_details, url, orderData);

            Log.d(LOG_TAG, "shoppingCartAdapter " + info[EdmanJasonReader.URL]);


        }

        RecyclerView recyclerView = findViewById(R.id.shopping_cart_recycler);

        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(foodItemLayoutManager);

        recyclerView.setAdapter(shoppingCartAdapter);
    }

/**
 * Possible hypernyms
 * beverage drink drinkable brew beverage liquid espresso coffee
 */

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
