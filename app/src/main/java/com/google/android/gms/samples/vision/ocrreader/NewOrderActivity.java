package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.NewShoppingCartAdapter;
import com.google.firebase.FirebaseApp;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class NewOrderActivity extends UseRecyclerActivity implements TextToSpeech.OnInitListener {


    private static  final int NUMBER_OF_IMAGES = 1;

    NewShoppingCartAdapter newShoppingCartAdapter;

    private static final String LOG_TAG = NewOrderActivity.class.getSimpleName();

    //Text to speech variables
    private int MY_DATA_CHECK_CODE = 0;


    AllOrders currentOrders;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.geography_order);

        Serializable serializableOrder = getIntent().getSerializableExtra(OpenRestaurantMenuActivity.ALL_ORDERS);
        currentOrders = (AllOrders) serializableOrder;
        OpenRestaurantMenuActivity.current_order = currentOrders;

                //start text to speech
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

        if (myTTS == null)
            myTTS = new TextToSpeech(this, this);

        newShoppingCartAdapter = new NewShoppingCartAdapter(this, currentOrders );

        TextView textView = findViewById(R.id.order_text_view);

        RecyclerView recyclerView = findViewById(R.id.shopping_cart_recycler);



        String complete_text = "";

        if (currentOrders != null && currentOrders.orders != null){
            String [] order_items = new String[currentOrders.orders.size()];
            int count = 0;
            Iterator resultIterator = currentOrders.orders.entrySet().iterator();
            while (resultIterator.hasNext()) {
                HashMap.Entry item = (HashMap.Entry) resultIterator.next();
                CurrentOrder currOrder = (CurrentOrder) item.getValue();

                String current_item_name = currOrder.mealName;;

                order_items[count] = current_item_name;

                complete_text += current_item_name;

                Object[] order = new Object[2];

                order[0] = current_item_name;
                order[1] = item.getKey();

                newShoppingCartAdapter.addItem(order);


                count++;


            }
        }


        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(foodItemLayoutManager);

        recyclerView.setAdapter(newShoppingCartAdapter);


        textView.setText(complete_text);

        WordNetHypernyms wordNetHypernyms = new WordNetHypernyms();

        String[] hypernyms = {"drink", "liquid"};
        wordNetHypernyms.getHypernym(hypernyms,"chicken lamb seafood turkey bacon ham stake beef wings sausage");

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
