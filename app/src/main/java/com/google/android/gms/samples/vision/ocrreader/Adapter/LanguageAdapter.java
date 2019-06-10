package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.OrderActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;

/**
 * Created by mgo983 on 1/27/19.
 */

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    LayoutInflater inflater;
    ArrayList<String[]> mData = new ArrayList<String[]>();
    Context context;

    private static final int SENTENCE = 0;
    private static final int IMAGE_URL = 1;


    public LanguageAdapter(Context context){
        this.context = context;
        inflater = LayoutInflater.from(context);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.conversation_image);
            mTextView = parent.findViewById(R.id.conversation_text);

        }

    }

    @Override
    public LanguageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.conversation_line_item, null);
        LanguageAdapter.ViewHolder viewHolder = new LanguageAdapter.ViewHolder(convertView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final LanguageAdapter.ViewHolder holder, int position){

        String[] current_conversation_line = mData.get(position);
        String orderOption = current_conversation_line[SENTENCE];
        holder.mTextView.setText(orderOption);
        holder.mTextView.setOnClickListener((View view)->{((UseRecyclerActivity) context).myTTS.speak(orderOption, TextToSpeech.QUEUE_FLUSH, null, null);});
        Glide.with(context).load(current_conversation_line[IMAGE_URL]).into(holder.mImageView);

    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


    public void addItem(String [] item){
        mData.add(item);
        notifyDataSetChanged();
    }


}
