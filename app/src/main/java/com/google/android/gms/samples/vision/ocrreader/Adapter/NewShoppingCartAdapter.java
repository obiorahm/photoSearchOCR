package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.AllOrders;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.SetAdapter;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 1/10/19.
 */

public class NewShoppingCartAdapter extends RecyclerView.Adapter<NewShoppingCartAdapter.ViewHolder> implements SetAdapter {

    private HashMap<String, Uri> mUrl = new HashMap<>();
    private ArrayList<Object[]> mData = new ArrayList<>();

    LayoutInflater inflater;
    TextToSpeech myTTS;
    Context context;

    AllOrders allOrders;

    String LOG_TAG = NewShoppingCartAdapter.class.getSimpleName();

    private static final String ORDER_PREFIX = "I'll have ";


    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;
        private ImageButton mImageButton;
        private RecyclerView mRecyclerView;
        private RelativeLayout mRelativeLayout;
        private RecyclerView mOptionRecyclerView;
        private RecyclerView mExpandOptionRecyclerView;
        private ProgressBar mImageProgressBar;



        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
            mImageButton = parent.findViewById(R.id.remove_order);
            mRecyclerView = parent.findViewById(R.id.conversation_recycler);
            mRelativeLayout = parent.findViewById(R.id.containing_relative_layout);
            mOptionRecyclerView = parent.findViewById(R.id.food_item_options);
            mExpandOptionRecyclerView = parent.findViewById(R.id.order_option_items);
            mImageProgressBar = parent.findViewById(R.id.order_image_progress_bar);
        }

    }


    public NewShoppingCartAdapter(Context context, AllOrders currentOrder){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;
        allOrders = currentOrder;
    }


    @Override
    public NewShoppingCartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.personal_order_items, null);
        return new NewShoppingCartAdapter.ViewHolder(convertView);
    }



    private static final int MEAL_NAME = 0;
    private static final int MEAL_URL = 1;

    @Override
    public void onBindViewHolder(final NewShoppingCartAdapter.ViewHolder holder, final int position){

        Object[] orderData = mData.get(position);

        final String meal_name = (String) orderData[MEAL_NAME];

        Uri url = mUrl.get(meal_name);

        String finalText = ORDER_PREFIX + meal_name;

        holder.mTextView.setText(finalText);

        if (url != null){
            Glide.with(context).load(url).into(holder.mImageView);
            holder.mImageProgressBar.setVisibility(View.GONE);

        }



        holder.mImageButton.setOnClickListener((View view) -> {
            deleteItem(position);

        });

        holder.mRelativeLayout.setOnClickListener((View view)->{
            //set up food item adapter

        });

        holder.mTextView.setOnClickListener((View view)->myTTS.speak(finalText,TextToSpeech.QUEUE_FLUSH,null, null));

    }




    public void addItem(Object[] orderData){
            mData.add(orderData);
        String icon[] = new String[1];
        icon[0] = (String) orderData[0];
        ((UseRecyclerActivity) context).loadImage(icon, this, false);
            Log.d("FetchMealDetails", (String) orderData[0]);
            notifyDataSetChanged();


    }


    @Override
    public void addImageUrl(String word, Uri uri){
        mUrl.put(word, uri);
        notifyDataSetChanged();
    }



    @Override
    public int getItemCount(){
        return mData.size();

    }


    public void deleteItem(int position){
        int UID_POS = 1;
        int FOOD_ITEM_POS = 0;
        Object [] order =  mData.get(position);

        Log.d(LOG_TAG, allOrders + " new order");

        allOrders.removeOrder((String) order[UID_POS]);
        mUrl.remove(order[FOOD_ITEM_POS]);
        mData.remove(position);
        notifyDataSetChanged();
    }




}


