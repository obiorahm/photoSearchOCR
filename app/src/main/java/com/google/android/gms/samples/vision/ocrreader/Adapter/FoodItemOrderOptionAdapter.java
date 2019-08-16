package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 12/19/18.
 */

public class FoodItemOrderOptionAdapter extends RecyclerView.Adapter<FoodItemOrderOptionAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String> mData = new ArrayList<>();
    ArrayList<Integer> state = new ArrayList<>();

    TextToSpeech myTTS;
    Context context;
    HashMap<String, Object[] > order = new HashMap<String, Object[]>();
    String current_order;
    RecyclerView expandOptionRecyclerView;


    public final static String SAUCES = "sauces";
    public final static String DRINKS = "drinks";
    public final static String NUTRITION = "nutrition";
    public final static String MEATS = "meats";

    private static int STATE_NORMAL = 0;
    private static int STATE_SELECT = 1;

    private int normal = R.drawable.smaller_layer_drawable;
    private int select = R.drawable.smaller_layer_selected;


    private int[] STATES = { normal, select};

    private ImageView lastImageView;
    private Integer lastState;

    private String LOG_TAG = FoodItemOrderOptionAdapter.class.getSimpleName();

    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
        }

    }

    public FoodItemOrderOptionAdapter(Context context, HashMap<String, Object[]> order, String current_order, RecyclerView recyclerView, TextToSpeech myTTS){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.order = order;
        this.current_order = current_order;
        this.expandOptionRecyclerView = new RecyclerView(context);
        this.expandOptionRecyclerView = recyclerView;
        this.myTTS = myTTS;

    }

    //private int[] STATES = { normal, select};


    @Override
    public FoodItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.option_item, null);

        return new FoodItemOrderOptionAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final FoodItemOrderOptionAdapter.ViewHolder holder, int position){

        //final int pos = position;

        String url = mData.get(position);

        int firstPos = url.lastIndexOf('/');
        int lastPos = url.lastIndexOf('.');

        final String label = url.substring(firstPos + 1, lastPos).replace("_", " ");

        holder.mTextView.setText(label);

        Glide.with(context).load(url).into(holder.mImageView);

        Log.d(LOG_TAG, url);

        int current_state = state.get(position);

        holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[current_state]));

        holder.mImageView.setOnClickListener((View view) -> {
            myTTS.speak(label,TextToSpeech.QUEUE_FLUSH,null, null);
            next_state(holder, position);
            handleOnClickListener(holder, label);


        });




    }


    public void handleOnClickListener(FoodItemOrderOptionAdapter.ViewHolder holder, String label){

        ExpandOptionAdapter expandOptionAdapter = new ExpandOptionAdapter(context, order, current_order);
        //OpenRestaurantMenuActivity openRestaurantMenuActivity = (OpenRestaurantMenuActivity) context;

        //openRestaurantMenuActivity.hide_food_image_recycler();


        switch (label){
            case SAUCES:
                expandOptionAdapter.addItem(SAUCES);
                break;
            case DRINKS:
                expandOptionAdapter.addItem(DRINKS);
                break;
            case NUTRITION:
                expandOptionAdapter.addItem(NUTRITION);
                break;
            case MEATS:
                expandOptionAdapter.addItem(MEATS);
        }
        LinearLayoutManager foodItemLayoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        expandOptionRecyclerView.setLayoutManager(foodItemLayoutManager);

        expandOptionRecyclerView.setAdapter(expandOptionAdapter);
        expandOptionRecyclerView.setVisibility(View.VISIBLE);
    }



    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String text){
        mData.add(text);
        state.add(STATE_NORMAL);
        notifyDataSetChanged();
    }




    private void next_state(FoodItemOrderOptionAdapter.ViewHolder holder, int position){
        if (lastImageView != null){
            lastImageView.setSelected(false);
            lastImageView.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
            lastState = STATE_NORMAL;
        }
        lastImageView = holder.mImageView;
        lastState = state.get(position);

        if (holder.mImageView.isSelected()){

            holder.mImageView.setSelected(false);
            holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));
            state.set(position, STATE_NORMAL);

        }else{
            holder.mImageView.setSelected(true);
            holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
            state.set(position, STATE_SELECT);

        }
        //notifyDataSetChanged();
    }

}
