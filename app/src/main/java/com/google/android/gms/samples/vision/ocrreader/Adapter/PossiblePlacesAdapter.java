package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.PlacesActivity;
import com.google.android.gms.samples.vision.ocrreader.R;

import java.util.ArrayList;


public class PossiblePlacesAdapter extends RecyclerView.Adapter<PossiblePlacesAdapter.ViewHolder> {

    ArrayList<String> mData = new ArrayList<>();
    Context context;
    LayoutInflater inflater;
    TextToSpeech myTTS;

    public PossiblePlacesAdapter(Context context, int resource){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((PlacesActivity) context).myTTS;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private RecyclerView mRecyclerView;
        private View mBackground;
        private RelativeLayout mRelativeLayout;
        private ImageButton mImageButton;
        private ImageView mImageView;
        //private StreetViewPanoramaView mStreetViewPanoramaView;
        //private Fragment mFragemnt;

        public ViewHolder(View convertView) {
            super(convertView);
            mBackground = convertView;
            mRecyclerView = convertView.findViewById(R.id.text_by_text);
            mRelativeLayout = convertView.findViewById(R.id.internal_relative_layout);
            mImageButton = convertView.findViewById(R.id.speak_whole_text);
            mImageView = convertView.findViewById(R.id.descriptive_image);
            //mStreetViewPanoramaView = convertView.findViewById(R.id.streetviewpanorama);
            //RecyclerView parentRecyclerView = (RecyclerView) convertView.getParent();


        }

    }




    @Override
    public PossiblePlacesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_text, null);
        PossiblePlacesAdapter.ViewHolder viewHolder = new PossiblePlacesAdapter.ViewHolder(convertView);


        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final PossiblePlacesAdapter.ViewHolder holder, final int position){

        final String word = mData.get(position);
        // setup horizontal text by text adapter
        TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(layoutManager);

        holder.mRecyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


    public void addItem (String place){ mData.add(place);}



}
