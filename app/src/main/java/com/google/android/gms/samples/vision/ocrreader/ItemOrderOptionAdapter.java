package com.google.android.gms.samples.vision.ocrreader;

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
        private static int STATE_SELECT = 1;

        private int normal = R.drawable.smaller_layer_drawable;
        private int select = R.drawable.smaller_layer_selected;


        private int[] STATES = { normal, select};

        private ImageView lastImageView;
        private Integer lastState;

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
                myTTS.speak(label,TextToSpeech.QUEUE_FLUSH,null, null);
                next_state(holder, position);


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





        private void next_state(ItemOrderOptionAdapter.ViewHolder holder, int position){
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
