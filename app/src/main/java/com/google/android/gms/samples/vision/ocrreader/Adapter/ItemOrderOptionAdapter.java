package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.SetAdapter;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class ItemOrderOptionAdapter extends RecyclerView.Adapter<ItemOrderOptionAdapter.ViewHolder> implements SetAdapter {


        LayoutInflater inflater;
        ArrayList<String[]> mData = new ArrayList<>();
        ArrayList<Integer> state = new ArrayList<>();
        HashMap<String, Uri> mUrl = new HashMap<>();

        TextToSpeech myTTS;
        Context context;





        private static int STATE_NORMAL = 0;

        private int normal = R.drawable.smaller_layer_drawable;
        private int select = R.drawable.option_button;
        private int reject = R.drawable.option_button_on;


        private int[] STATES = { normal, select, reject};


        private String LOG_TAG = ItemOrderOptionAdapter.class.getSimpleName();

        public class ViewHolder extends RecyclerView.ViewHolder{


            private ImageView mImageView;
            private TextView mTextView;

            public ViewHolder(View parent){
                super(parent);
                mImageView = parent.findViewById(R.id.order_option);
                mTextView = parent.findViewById(R.id.order_option_text);
            }

        }

        public ItemOrderOptionAdapter(Context context,  TextToSpeech myTTS){
            this.context = context;
            inflater = LayoutInflater.from(context);

            this.myTTS = myTTS;
        }

        //private int[] STATES = { normal, select};


        @Override
        public ItemOrderOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View convertView = inflater.inflate(R.layout.enlarged_option_item, null);

            return new ItemOrderOptionAdapter.ViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(final ItemOrderOptionAdapter.ViewHolder holder, int position){

            //final int pos = position;

            String[] text = mData.get(position);
            String label = text[1];
            String root_string = text[0];

            if (label != null)
                holder.mTextView.setText(label);

            Uri url = mUrl.get(root_string);

            if (url != null){
                Glide.with(context).load(url).into(holder.mImageView);
                Log.d(LOG_TAG, url + "");
            }

            int current_state = state.get(position);

            holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[current_state]));

            holder.mImageView.setOnClickListener((View view) -> {
                int nextstate = next_state(state.get(position));
                holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));
                myTTS.speak(label,TextToSpeech.QUEUE_FLUSH,null, null);
                state.set(position, nextstate);


            });

        }

        @Override
        public int getItemCount(){
            return mData.size();
        }

        public void addItem(String[] icon){
            mData.add(icon);
            state.add(STATE_NORMAL);


            ((UseRecyclerActivity) context).loadImage(icon, this, false);
            notifyDataSetChanged();
        }


    @Override
    public void addImageUrl(String word, Uri uri){
        mUrl.put(word, uri);
        notifyDataSetChanged();
    }

    private int next_state(int state_id){
        int new_state = state_id + 1;
        if (new_state >= STATES.length)
            return STATE_NORMAL;
        else
            return new_state;

    }

}
