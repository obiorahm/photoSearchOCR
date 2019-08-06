package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
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

/**
 * Created by mgo983 on 9/7/18.
 */

public class SimpleImageRecyclerAdapter extends RecyclerView.Adapter<SimpleImageRecyclerAdapter.ViewHolder> implements SetAdapter {

    private LayoutInflater inflater;
    private Context context;
    private TextToSpeech myTTS;
    private ArrayList<Integer> state = new ArrayList<>();
    private ArrayList<String[]> mData = new ArrayList<>();
    private HashMap<String, Uri> mUrls = new HashMap<>();

    private static int STATE_NORMAL = 0;

    private int normal = R.drawable.layer_drawable;
    private int select = R.drawable.option_button;
    private int reject = R.drawable.option_button_on;


    private int[] STATES = { normal, select, reject };

    private int WORD_POS = 0;

    private String LOGTAG = SimpleImageRecyclerAdapter.class.getSimpleName();

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView mImageView;
        private TextView mTextView;

        private ViewHolder(View convertView){
            super(convertView);
            mImageView = convertView.findViewById(R.id.icon_item);
            mTextView = convertView.findViewById(R.id.choice_text);

        }

    }

    public SimpleImageRecyclerAdapter(Context context, TextToSpeech myTTS){
        super();
        this.myTTS = myTTS;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addItem(String [] icon){
        state.add(STATE_NORMAL);
        ((UseRecyclerActivity) context).loadImage(icon,this, false);
        mData.add(icon);

        //mData.add(icon);
        notifyDataSetChanged();

    }

    @Override
    public void addImageUrl(String [] icon, Uri uri){
        mUrls.put(icon[WORD_POS], uri);
        notifyDataSetChanged();
    }

    @Override

    public void onBindViewHolder(final SimpleImageRecyclerAdapter.ViewHolder holder, int position) {

        int CHUNK= 1;
        int URL = 2;
        int CHUNK_ROOT = 0;

        final int pos = position;

        final String currData [] = mData.get(pos);

        final Uri uri = mUrls.get(currData[CHUNK_ROOT]);

        final String noun_chunk = currData[CHUNK];



        holder.mTextView.setText(noun_chunk);

        Glide.with(context).load(uri).into(holder.mImageView);

        holder.mImageView.setOnClickListener((View view)->
           {
                int nextstate = next_state(state.get(pos));

                myTTS.speak(noun_chunk, TextToSpeech.QUEUE_FLUSH, null, null);

                holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[nextstate]));

                state.set(pos, nextstate);

            });
        holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[state.get(pos)]));


    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    @Override
    public SimpleImageRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View convertView = inflater.inflate(R.layout.list_view_icon_item, null);

        return new ViewHolder(convertView);

    }

    private int next_state(int state_id){
        int new_state = state_id + 1;
        if (new_state >= STATES.length)
            return STATE_NORMAL;
        else
            return new_state;

    }


}
