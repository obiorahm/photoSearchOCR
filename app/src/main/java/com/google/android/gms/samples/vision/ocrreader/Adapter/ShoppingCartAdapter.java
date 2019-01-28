package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.Test;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 1/10/19.
 */

public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<Integer []> mSpecificOptions = new ArrayList<>();
    private ArrayList<String> mUrl = new ArrayList<>();

    LayoutInflater inflater;
    TextToSpeech myTTS;
    Context context;


    String LOG_TAG = ShoppingCartAdapter.class.getSimpleName();

    public static final String ORDER_PREFIX = "I'll have ";


    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;
        private ImageButton mImageButton;
        private RecyclerView mRecyclerView;


        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
            mImageButton = parent.findViewById(R.id.remove_order);
            mRecyclerView = parent.findViewById(R.id.conversation_recycler);
        }

    }


    public ShoppingCartAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
    }


    @Override
    public ShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.personal_order_items, null);
        return new ShoppingCartAdapter.ViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(final ShoppingCartAdapter.ViewHolder holder, final int position){

        final String meal_name = mData.get(position);

        holder.mTextView.setText(ORDER_PREFIX + meal_name);

        Glide.with(context).load(mUrl.get(position)).into(holder.mImageView);

        holder.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FoodItemAdapter.order.remove(meal_name);
                holder.mTextView.setVisibility(View.GONE);
                holder.mImageView.setVisibility(View.GONE);
                holder.mImageButton.setVisibility(View.GONE);
            }
        });

        setOrderLanguage(holder, position);

    }




    public void addItem(String food_item_name, Integer [] specifics, String url ){
        mData.add(food_item_name);
        mSpecificOptions.add(specifics);
        mUrl.add(url);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount(){
        return mData.size();
    }

    private void setOrderLanguage(ViewHolder holder, int pos){
        LanguageAdapter languageAdapter= new LanguageAdapter(context);

        Integer[] specifics = mSpecificOptions.get(pos);
        for (int i = 0; i < specifics.length; i++){
            String current_sentence = Order.CONSOLIDATED_OPTION[i][specifics[i]];
            String current_image_url = "";
            String[] data = {current_sentence,current_image_url};
            if (!current_sentence.equals(""))
                languageAdapter.addItem(data);
        }

        holder.mRecyclerView.setAdapter(languageAdapter);
        LinearLayoutManager languageLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mRecyclerView.setLayoutManager(languageLayoutManager);
    }
}

