package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.ShoppingCartAdapter;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 1/10/19.
 */

public class OrderActivity extends UseRecyclerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.geography_order);

        ShoppingCartAdapter shoppingCartAdapter = new ShoppingCartAdapter(this);

        TextView textView = findViewById(R.id.order_text_view);

        RecyclerView recyclerView = findViewById(R.id.shopping_cart_recycler);


        String complete_text = "";
        Iterator resultIterator = FoodItemAdapter.order.entrySet().iterator();
        while (resultIterator.hasNext()) {
            HashMap.Entry item = (HashMap.Entry) resultIterator.next();
            String current_item_name = (String) item.getKey();
            complete_text += current_item_name;
            Order current_order = (Order) item.getValue();
            Integer[] order_details = current_order.getOrderValues();

            shoppingCartAdapter.addItem(current_item_name, order_details, "");

            for (int i = 0; i < Order.OPTION_SIZE; i++){
                complete_text += order_details[i] + " ";
            }
        }

        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(foodItemLayoutManager);

        recyclerView.setAdapter(shoppingCartAdapter);

        textView.setText(complete_text);

    }


}
