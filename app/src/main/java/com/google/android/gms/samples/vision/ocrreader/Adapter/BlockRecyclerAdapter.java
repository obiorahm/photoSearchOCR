package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.OrderInstructions;
import com.google.android.gms.samples.vision.ocrreader.ProcessTextBlock;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by mgo983 on 9/7/18.
 */

public class BlockRecyclerAdapter extends RecyclerView.Adapter<BlockRecyclerAdapter.ViewHolder> {

    Context context;
    LayoutInflater inflater;
    TextToSpeech myTTS;
    ArrayList<String> mData = new ArrayList<String>();
    ArrayList<ImageRecyclerAdapter> mImageRecyclerAdapter = new ArrayList<ImageRecyclerAdapter>();
    private OrderInstructions orderInstructions;
    private View parentView;

   LinkedHashMap<String, String[]> mDataPair = new LinkedHashMap<>();





    public class ViewHolder extends RecyclerView.ViewHolder{

        RecyclerView mRecyclerView;

        public ViewHolder(View convertView){
            super(convertView);
            mRecyclerView= convertView.findViewById(R.id.option_icons);
        }
    }

    private void staticPopulate(){
        String[] big = {"big.png", "bigger.png", "biggest.png"};
        mDataPair.put("Can I order an appetizer size or half-size entrée?", big);

        String [] share = {"share.png"};
        mDataPair.put("Can I split a dish with someone at my table?", share);

        String [] vegetableOrMeat = {"vegetables.png", "main_dish.png"};
        mDataPair.put("Could you give me a larger portion of vegetables and a smaller portion of the main dish? ", vegetableOrMeat);

        String [] substitute = {"substitution.png"};
        mDataPair.put("What can I substitute?", substitute);

        String [] slime = {"mayonnaise.png", "dressing.png", "cheese_sauce.png", "mustard.png"};
        mDataPair.put("Could you leave off the (sour cream, cheese sauce, dressing, mayonnaise, etc.)?", slime);

        String [] sliced = {"sliced.png", "whole.png"};
        mDataPair.put("Can you make this dish with sliced chicken breast?", sliced);

        String [] vegetarian = {"vegetarian.png"};
        mDataPair.put("Which dishes do you recommend for vegetarians?", vegetarian);

        String [] nutrition = {"nutrition_facts.png"};
        mDataPair.put("Do you have nutrition information on any of your dishes?", nutrition);

        String[] water = {"ice.png", "water.png", "warm.png"};
        mDataPair.put("No ice please?", water);

        String[] meat = { "blue_rare.png", "rare.png" , "medium_rare.png", "medium.png","medium_well.png", "well_done.png"};
        mDataPair.put("Can I have my meat well done?", meat);

    }

    private void dynamicPopulate(String wholeOrder){
        ProcessTextBlock processTextBlock = new ProcessTextBlock(wholeOrder);
        String wholeOrderProcessed = processTextBlock.processText();
        String [] ArrayWholeOrderProcessed = wholeOrderProcessed.split(",");

        //String [] share = {"share.png"};

        for (String val : ArrayWholeOrderProcessed){
            String temp_val = val.trim();
            if (!(temp_val.equals("") || temp_val == null)){
                String[] share = { val};
                mDataPair.put(val, share);

            }

        }
    }

    public BlockRecyclerAdapter(Context context, int resources, TextToSpeech myTTS, View parent,OrderInstructions orderInstructions, String wholeOrder){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.myTTS = myTTS;
        this.orderInstructions = orderInstructions;
        this.parentView = parent;

        dynamicPopulate(wholeOrder);
        staticPopulate();
    }

    public BlockRecyclerAdapter(Context context, int resources, TextToSpeech myTTS, View parent, OrderInstructions orderInstructions){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.myTTS = myTTS;
        this.orderInstructions = orderInstructions;
        this.parentView = parent;

        staticPopulate();
    }

    /*public void addItem(String wordInMeal){

        mData.add(wordInMeal);



        ImageRecyclerAdapter imageRecyclerAdapter = new ImageRecyclerAdapter(context, parentView, myTTS,  orderInstructions);

        String [] options = mDataPair.get(wordInMeal);

        for(String option : options){
            String expanded_pair[] = {option, wordInMeal};
            imageRecyclerAdapter.addItem(expanded_pair);
        }
        mImageRecyclerAdapter.add(imageRecyclerAdapter);




    }*/

    public void addItem(){

     /* Initialize image adapter here so that states are saved when the views are loaded
         * Don't initialize in get view else new image adapters are created
         * */

        for (String key : mDataPair.keySet()){
            ImageRecyclerAdapter imageRecyclerAdapter = new ImageRecyclerAdapter(context, parentView, myTTS,  orderInstructions);

            String [] options = mDataPair.get(key);
            for(String option : options){
                String expanded_pair[] = {option, key};
                imageRecyclerAdapter.addItem(expanded_pair);
            }
            mImageRecyclerAdapter.add(imageRecyclerAdapter);

        }

    }

    @Override
    public BlockRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_select_item, null);

        return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final BlockRecyclerAdapter.ViewHolder holder, final int position) {

        //final String texttoSpeak =  mData.get(position);

        ImageRecyclerAdapter imageRecyclerAdapter = mImageRecyclerAdapter.get(position);

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);


        holder.mRecyclerView.setLayoutManager(layoutManager);
        holder.mRecyclerView.setAdapter(imageRecyclerAdapter);


    }

    @Override
    public int getItemCount(){
        return mDataPair.size();
    }


}
