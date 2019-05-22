package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.GeographyActivity;
import com.google.android.gms.samples.vision.ocrreader.PlacesActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.util.ArrayList;


public class PossiblePlacesAdapter extends RecyclerView.Adapter<PossiblePlacesAdapter.ViewHolder> {

    ArrayList<Object[]> mData = new ArrayList<>();
    Context context;
    LayoutInflater inflater;
    TextToSpeech myTTS;
    int RESTAURANT_ADDRESS = 0;
    int IS_SELECTED = 1;

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


        return new PossiblePlacesAdapter.ViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(final PossiblePlacesAdapter.ViewHolder holder, final int position){
        Object[] data = mData.get(position);

        final String word = (String) data[RESTAURANT_ADDRESS];
        // setup horizontal text by text adapter
        TextByTextAdapter adapter = new TextByTextAdapter(context, R.layout.recognized_text_item);

        String tokenizedString [] = word.split(" ");

        for (String child : tokenizedString){
            adapter.addItem(child);
        }


        holder.mRelativeLayout.setSelected((boolean) data[IS_SELECTED]);
        holder.mImageButton.setOnClickListener(view -> myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null));

        holder.mBackground.setOnClickListener(view -> control_select(holder, position));


        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
        holder.mRecyclerView.setLayoutManager(layoutManager);

        holder.mRecyclerView.setAdapter(adapter);

        //hide progressBar
        ProgressBar progressBar = ((PlacesActivity) context).findViewById(R.id.menu_progress);
        progressBar.setVisibility(View.GONE);
    }


    private void startActivity(){

    }

    @Override
    public int getItemCount(){
        return mData.size();
    }


    public void addItem (String place){
        Object[] data = new Object[2];

        data[RESTAURANT_ADDRESS] = (String) place;
        data[IS_SELECTED] = false;
        mData.add(data);
    notifyDataSetChanged();
    }

    Object[] last_selected_property;

    private void control_select(PossiblePlacesAdapter.ViewHolder holder, int position){

        Object[] currentProperties =  mData.get(position);


        if ( last_selected_property != null && last_selected_property != currentProperties){

            //update properties
            last_selected_property[IS_SELECTED] = false;
            //to enable redraw of view with new properties
            notifyDataSetChanged();
        }
        if( (boolean) currentProperties[IS_SELECTED]){
            holder.mRelativeLayout.setSelected(false);

            //update properties
            currentProperties[IS_SELECTED] = false;

        }else{

            PlacesActivity.selectedAddress = (String) currentProperties[RESTAURANT_ADDRESS];
            holder.mRelativeLayout.setSelected(true);

            //update properties
            currentProperties[IS_SELECTED] = true;


        }
        last_selected_property = currentProperties;


        //set up panorama


    }



}
