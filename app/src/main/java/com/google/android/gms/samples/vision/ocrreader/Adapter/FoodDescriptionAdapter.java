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


import com.google.android.gms.samples.vision.ocrreader.FetchNounDependency;
import com.google.android.gms.samples.vision.ocrreader.NounDependencyJsonHandler;
import com.google.android.gms.samples.vision.ocrreader.ObjectsToHide;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mgo983 on 10/22/18.
 */

public class FoodDescriptionAdapter extends RecyclerView.Adapter<FoodDescriptionAdapter.ViewHolder>  implements NounDependencyJsonHandler {

    LayoutInflater inflater;
    ArrayList<String> mData = new ArrayList<>();
    private ArrayList<SimpleImageRecyclerAdapter> mImageRecyclerAdapters = new ArrayList<>();
    private HashMap<String,  String> mListOfNouns = new HashMap<>();
    public static HashMap<String, Object[]> order = new HashMap<>();

    ObjectsToHide objectsToHide ;



    TextToSpeech myTTS;
    Context context;
    private String LOG_TAG = FoodDescriptionAdapter.class.getSimpleName();




    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageButton mSpeakImageButton;

        RecyclerView mDescriptionImageRecycler;


        public ViewHolder(View parent){
            super(parent);
            mSpeakImageButton = parent.findViewById(R.id.speak_recycler_all);
            mDescriptionImageRecycler = parent.findViewById(R.id.option_icons);

        }

    }

    public FoodDescriptionAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;

        objectsToHide = new ObjectsToHide(context);

    }

    public void setObjectsToHide(ObjectsToHide objectsToHide){
        this.objectsToHide = objectsToHide;
        notifyDataSetChanged();
    }

    @Override
    public FoodDescriptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.block_select_item, null);
        return new FoodDescriptionAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final FoodDescriptionAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);

        holder.mSpeakImageButton.setOnClickListener((View view)-> myTTS.speak(word, TextToSpeech.QUEUE_FLUSH,null,null));

        final SimpleImageRecyclerAdapter imageRecyclerAdapter = mImageRecyclerAdapters.get(position);

        LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);

        holder.mDescriptionImageRecycler.setLayoutManager(layoutManager);
        holder.mDescriptionImageRecycler.setAdapter(imageRecyclerAdapter);


    }



@Override
    public void processJson(String description){
    try{
        if (description != null){
            JSONObject data = new JSONObject(description);
            JSONArray chunk = data.getJSONArray("chunk");
            JSONArray chunk_root =  data.getJSONArray("chunk_root");
            JSONArray chunk_lemma = data.getJSONArray("chunk_lemma");
            for (int i = 0; i < chunk.length(); i++){

                String chunk_item = (String) chunk.get(i);
                String chunk_root_item = (String) chunk_root.get(i);
                String image_url = "";
                String chunk_lemma_item = ((JSONArray) chunk_lemma.get(i)).getString(0);

                if (!(mListOfNouns.containsKey(chunk_root_item))){
                    // initialize entry state to zero
                    mListOfNouns.put(chunk_item,chunk_root_item);
                    mData.add(chunk_item);

                    //Create itemRecycler
                    //Log.d(LOG_TAG, "object to hide " + objectsToHide.toString());
                    SimpleImageRecyclerAdapter imageRecyclerAdapter = new SimpleImageRecyclerAdapter(context, myTTS, objectsToHide);
                    String[] option = {chunk_root_item, chunk_item, "", chunk_lemma_item  };
                    imageRecyclerAdapter.addItem(option);

                    mImageRecyclerAdapters.add(imageRecyclerAdapter);
                    notifyDataSetChanged();

                }
                objectsToHide.descriptionProgressBar.setVisibility(View.GONE);
                objectsToHide.descriptionRecyclerView.setVisibility(View.VISIBLE);

        }

        }



    }catch (JSONException e){
        Log.e(LOG_TAG, e.toString());
    }
    }


    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String text){
        FetchNounDependency fetchNounDependency = new FetchNounDependency(this);
        fetchNounDependency.execute(text);
        notifyDataSetChanged();
    }


    public HashMap getOrderDescription(){
// Getting an iterator

        return mListOfNouns;

    }




}
