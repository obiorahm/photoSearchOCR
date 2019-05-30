package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.OpenRestaurantMenuActivity;
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


    public final static String SAUCES = "sauces";
    public final static String DRINKS = "drinks";
    public final static String NUTRITION = "nutrition";
    public final static String MEATS = "meats";

    private static int STATE_NORMAL = 0;

    /*private int normal = R.drawable.smaller_layer_drawable;
    private int select = R.drawable.smaller_layer_selected;*/

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

    public FoodItemOrderOptionAdapter(Context context, HashMap<String, Object[]> order, String current_order){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.order = order;
        this.current_order = current_order;
    }

    //private int[] STATES = { normal, select};


    @Override
    public FoodItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.option_item, null);
        //FoodItemOrderOptionAdapter.ViewHolder viewHolder = new FoodItemOrderOptionAdapter.ViewHolder(convertView);

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

        holder.mImageView.setOnClickListener((View view) -> {
                /*int nextstate = next_state(state.get(pos));

                holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));

                state.set(pos, nextstate);*/
            handleOnClickListener(holder, label);
            next_state(holder);

        });




    }


    public void handleOnClickListener(FoodItemOrderOptionAdapter.ViewHolder holder, String label){

        ExpandOptionAdapter expandOptionAdapter = new ExpandOptionAdapter(context, order, current_order);
        OpenRestaurantMenuActivity openRestaurantMenuActivity = (OpenRestaurantMenuActivity) context;

        openRestaurantMenuActivity.hide_food_image_recycler();

        RecyclerView recyclerView = openRestaurantMenuActivity.findViewById(R.id.order_option_items);

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
        recyclerView.setLayoutManager(foodItemLayoutManager);

        recyclerView.setAdapter(expandOptionAdapter);
        recyclerView.setVisibility(View.VISIBLE);
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




    private void next_state(FoodItemOrderOptionAdapter.ViewHolder holder){
        if (holder.mImageView.isSelected()){
            holder.mImageView.setSelected(true);
        }else{
            holder.mImageView.setSelected(false);
        }
    }

}
