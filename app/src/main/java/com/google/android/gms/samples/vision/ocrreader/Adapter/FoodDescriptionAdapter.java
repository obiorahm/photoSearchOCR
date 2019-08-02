package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.FetchNounDependency;
import com.google.android.gms.samples.vision.ocrreader.NounDependencyJsonHandler;
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
    HashMap<String,  String> mListOfNouns = new HashMap<>();
    private ArrayList<Integer> state = new ArrayList<>();
    public static HashMap<String, Object[]> order = new HashMap<>();


    private

    TextToSpeech myTTS;
    Context context;
    private String LOG_TAG = FoodDescriptionAdapter.class.getSimpleName();

    private final static int STATE_NORMAL = 0;
    private final static int STATE_SELECT = 1;
    private final static int STATE_CURRENT_SELECT = 2;

    private RelativeLayout last_selected_relative_layout;

    private int normal = R.drawable.border;
    private int select = R.drawable.text_border;
    private int current_selection = R.drawable.select_border;

    private int[] STATES = { normal, select, current_selection};


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageButton mSpeakImageButton;
        private ImageButton mDescriptiveImageButton;
        private TextView mTextView;
        private RelativeLayout mContainingRelativeLayout;
        private CheckBox mCheckBox;

        public ViewHolder(View parent){
            super(parent);

            mTextView = parent.findViewById(R.id.single_item);

            mSpeakImageButton = parent.findViewById(R.id.speak_whole_text);
            mDescriptiveImageButton = parent.findViewById(R.id.show_image);
            mContainingRelativeLayout = parent.findViewById(R.id.containing_relative_layout);

            mCheckBox = parent.findViewById(R.id.select_food_item);

        }

    }

    public FoodDescriptionAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;


    }

    @Override
    public FoodDescriptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.single_description_item, null);


        return new FoodDescriptionAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final FoodDescriptionAdapter.ViewHolder holder, int position){

        final String word = mData.get(position);
        holder.mTextView.setText(word);

        //load image
        ((UseRecyclerActivity) context).loadImage(mListOfNouns.get(word),holder.mDescriptiveImageButton, false);

        //select with relative layout instead
        holder.mContainingRelativeLayout.setSelected(false);


        holder.mSpeakImageButton.setOnClickListener((view) ->
                myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null, null));


        holder.mCheckBox.setOnClickListener((View view) ->
                changeState(holder, position));

        holder.mContainingRelativeLayout.setOnClickListener((View view)->{
            if(holder.mCheckBox.isChecked())
                holder.mCheckBox.setChecked(false);
            else
                holder.mCheckBox.setChecked(true);
            changeState(holder, position);
        });



    }

    private void changeState(FoodDescriptionAdapter.ViewHolder holder, int position){

        myTTS.speak(mData.get(position), TextToSpeech.QUEUE_FLUSH, null, null);

        if (holder.mCheckBox.isChecked()){

            if (last_selected_relative_layout != null){
                last_selected_relative_layout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_SELECT]));
            }
            last_selected_relative_layout = holder.mContainingRelativeLayout;



            holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_CURRENT_SELECT]));
            state.set(position,STATE_CURRENT_SELECT);


        }else{


            holder.mContainingRelativeLayout.setBackground(ContextCompat.getDrawable(context, STATES[STATE_NORMAL]));

            if (last_selected_relative_layout == holder.mContainingRelativeLayout)
                last_selected_relative_layout = null;

            state.set(position,STATE_NORMAL);


        }


    }

@Override
    public void processJson(String description){
    try{
        JSONObject data = new JSONObject(description);
        JSONArray chunk = data.getJSONArray("chunk");
        JSONArray chunk_root =  data.getJSONArray("chunk_root");
        for (int i = 0; i < chunk.length(); i++){

            String chunk_item = (String) chunk.get(i);
            String chunk_root_item = (String) chunk_root.get(i);

            if (!(mListOfNouns.containsKey(chunk_root_item))){
                // initialize entry state to zero
                state.add(STATE_NORMAL);
                mListOfNouns.put(chunk_item,chunk_root_item);
                mData.add(chunk_item);

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




}
