package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.Order;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.io.IOException;
import java.util.ArrayList;

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
        private RelativeLayout mRelativeLayout;


        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
            mImageButton = parent.findViewById(R.id.remove_order);
            mRecyclerView = parent.findViewById(R.id.conversation_recycler);
            mRelativeLayout = parent.findViewById(R.id.containing_relative_layout);
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

        String finalText = ORDER_PREFIX + meal_name;

        holder.mTextView.setText(finalText);

        Glide.with(context).load(mUrl.get(position)).into(holder.mImageView);

        setOrderLanguage(holder, position);

        holder.mImageButton.setOnClickListener((View view) -> {

            FoodItemAdapter foodItemAdapter = (FoodItemAdapter) FoodItemAdapter.order.get(meal_name)[FoodItemAdapter.FOOD_ITEM_ADAPTER];
            foodItemAdapter.resetLayout(meal_name);
            FoodItemAdapter.order.remove(meal_name);

            holder.mTextView.setVisibility(View.GONE);
            holder.mImageView.setVisibility(View.GONE);
            holder.mImageButton.setVisibility(View.GONE);
            holder.mRecyclerView.setVisibility(View.GONE);
            holder.mRelativeLayout.setVisibility(View.GONE);
        });

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
            //String current_image_url = "";
            String current_image_url = getImageUrl(i, specifics[i]);
            String[] data = {current_sentence,current_image_url};
            if (!current_sentence.equals(""))
                languageAdapter.addItem(data);
        }

        holder.mRecyclerView.setAdapter(languageAdapter);
        LinearLayoutManager languageLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        holder.mRecyclerView.setLayoutManager(languageLayoutManager);
    }


    private final static int ZERO_SUM = 0;
    private final static int MORE_CHOICES = 1;


    public String getImageUrl(int consolidatedOptionPosition, int pos){
        String url = "";
        String allAssets [];
        AssetManager assetManager = context.getAssets();

        switch (consolidatedOptionPosition){
            case 0:
                url += Order.DRINKS;
                break;

            case 1:
                url += Order.MEATS + "/cooked";
                break;

            case 2:
                url += Order.MEATS + "/sliced";
                break;

            case 3:
                url += Order.NUTRITION;
                break;

            case 4:
                url += Order.SAUCES;
                break;

            case 5:
                url += Order.DRINKS;
                break;
        }

        try{
            allAssets = assetManager.list(url );

            int position = pos - 1;
            if (position >= 0)
                url =  "file:///android_asset/" + url + "/" +allAssets[pos - 1];

            Log.d(LOG_TAG, "alternative " + url);

        }catch (IOException e){
            Log.e(LOG_TAG, e +"");
        }catch (NullPointerException e){
            Log.e(LOG_TAG, e+ "");
        }

        return url;
    }

}


