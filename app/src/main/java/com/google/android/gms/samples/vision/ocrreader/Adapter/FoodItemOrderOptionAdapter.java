package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;

/**
 * Created by mgo983 on 12/19/18.
 */

public class FoodItemOrderOptionAdapter extends RecyclerView.Adapter<FoodItemOrderOptionAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String> mData = new ArrayList<>();
    ArrayList<Integer> state = new ArrayList<>();

    TextToSpeech myTTS;
    Context context;

    private static int STATE_NORMAL = 0;

    private int normal = R.drawable.smaller_layer_drawable;
    private int select = R.drawable.smaller_layer_selected;

    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
        }

    }

    public FoodItemOrderOptionAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    private int[] STATES = { normal, select};


    @Override
    public FoodItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.option_item, null);
        FoodItemOrderOptionAdapter.ViewHolder viewHolder = new FoodItemOrderOptionAdapter.ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final FoodItemOrderOptionAdapter.ViewHolder holder, int position){

        final int pos = position;

        String url = mData.get(position);

        int firstPos = url.lastIndexOf('/');
        int lastPos = url.lastIndexOf('.');

        String label = url.substring(firstPos + 1, lastPos);

        holder.mTextView.setText(label);

        Glide.with(context).load(url).into(holder.mImageView);

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*int nextstate = next_state(state.get(pos));

                holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));

                state.set(pos, nextstate);*/
                next_state(holder);

            }
        });

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


    public void addItem(ArrayList<String> foodItems){
        //mData = foodItems;
    }


    private int next_state(int state_id){
        int new_state = state_id + 1;
        if (new_state >= STATES.length)
            return STATE_NORMAL;
        else
            return new_state;

    }

    private void next_state(FoodItemOrderOptionAdapter.ViewHolder holder){
        if (holder.mImageView.isSelected()){
            holder.mImageView.setSelected(true);
        }else{
            holder.mImageView.setSelected(false);
        }
    }

}
