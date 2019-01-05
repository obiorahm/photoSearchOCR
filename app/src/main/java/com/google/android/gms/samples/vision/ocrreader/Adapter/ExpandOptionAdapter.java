package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.content.Context;
import android.content.res.AssetManager;
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
import com.google.android.gms.samples.vision.ocrreader.UseRecyclerActivity;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by mgo983 on 1/3/19.
 */

public class ExpandOptionAdapter extends RecyclerView.Adapter<ExpandOptionAdapter.ViewHolder> {

    private ArrayList<String[]> mData = new ArrayList<>();
    private ArrayList<Integer> mState = new ArrayList<>();
    private ArrayList<Integer> mType = new ArrayList<>();
    private ArrayList<String> mTopLevel = new ArrayList<>();

    LayoutInflater inflater;
    TextToSpeech myTTS;
    Context context;

    String LOG_TAG = ExpandOptionAdapter.class.getSimpleName();

    private int normal = R.drawable.smaller_layer_drawable;
    private int select = R.drawable.smaller_layer_selected;
    private int reject = R.drawable.option_button_on;

    private int[] STATES = { normal, select, reject};


    public class ViewHolder extends RecyclerView.ViewHolder{


        private ImageView mImageView;
        private TextView mTextView;

        public ViewHolder(View parent){
            super(parent);
            mImageView = parent.findViewById(R.id.order_option);
            mTextView = parent.findViewById(R.id.order_option_text);
        }

    }

    public ExpandOptionAdapter(Context context){
        super();
        inflater = LayoutInflater.from(context);
        this.context = context;
        myTTS = ((UseRecyclerActivity) context).myTTS;

    }


    @Override
    public ExpandOptionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.enlarged_option_item, null);
        return new ExpandOptionAdapter.ViewHolder(convertView);
    }


    @Override
    public void onBindViewHolder(final ExpandOptionAdapter.ViewHolder holder, final int position){

        String image_file_name = mData.get(position)[NORMAL];
        int end_char = image_file_name.indexOf(".");
        String label = image_file_name.substring(0, end_char);

        holder.mTextView.setText(label);

        String option_image_url = "file:///android_asset/" + mTopLevel.get(position) + "/" + image_file_name;
        Log.d(LOG_TAG, "Top Level" + option_image_url);

        Glide.with(context).load(option_image_url).into(holder.mImageView);

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleOnClickListener( position, holder);
            }
        });


    }


    @Override
    public int getItemCount(){
        return mData.size();
    }



    private final static String SAUCES = "sauces";
    private final static String DRINKS = "drinks";
    private final static String NUTRITION = "nutrition";
    private final static String MEATS = "meats";


    private final static int NORMAL = 0;
    private final static int SELECT = 1;
    private final static int REJECT = 2;

    public void addItem(String top_level_option){



        AssetManager assetManager = context.getAssets();
        String allAssets[];

        String second_level_option = "";
        String empty_second_level_array [] = {second_level_option};
        String second_level_option_array [] = empty_second_level_array;
        String meats_second_level_array [] = {"/cooked", "/sliced"};

        int [] type = {ZERO_SUM};
        int [] meat_option_types = {MORE_CHOICES, ZERO_SUM};

        try{


            switch (top_level_option){
                case SAUCES:
                    type[0] = MORE_CHOICES;

                    break;
                case DRINKS:
                    type[0] = ZERO_SUM;

                    break;
                case NUTRITION:
                    type[0] = ZERO_SUM;

                    break;
                case MEATS:
                    second_level_option_array = meats_second_level_array;
                    type = meat_option_types;
                    break;
            }

            for (int i = 0; i < second_level_option_array.length; i++){
                allAssets = assetManager.list(top_level_option + second_level_option_array[i]);
                mData.add(allAssets);
                mState.add(NORMAL);
                mTopLevel.add(top_level_option + second_level_option_array[i]);
                mType.add(type[i]);
                notifyDataSetChanged();

            }

        }catch (IOException e){
            Log.e(LOG_TAG, e + "");
        }


    }

    private final static int ZERO_SUM = 0;
    private final static int MORE_CHOICES = 1;

    private void handleOnClickListener(int pos, ExpandOptionAdapter.ViewHolder holder){
        int ICON_TYPE = mType.get(pos);
        switch (ICON_TYPE){
            case ZERO_SUM:
                switch_state(pos, holder);
                break;
            case MORE_CHOICES:

                rotate_choices(pos, holder);
                break;
        }
    }

    /**
     * switch_state
     * @param pos
     * @param holder
     */
    private void switch_state (int pos, ExpandOptionAdapter.ViewHolder holder){
        int new_state = mState.get(pos) + 1;
        if (new_state > REJECT){
            new_state = NORMAL;
        }
        mState.set(pos, new_state);
        holder.mImageView.setBackground(ContextCompat.getDrawable(context, STATES[new_state]));
    }


    private void rotate_choices(int pos, ExpandOptionAdapter.ViewHolder holder ){
        int new_choice = mState.get(pos) + 1;
        int total = mData.get(pos).length;
        if (new_choice >= total){
            new_choice = NORMAL;
        }
        mState.set(pos, new_choice);
        String image_file_name = mData.get(pos)[new_choice];
        int end_char = image_file_name.indexOf(".");
        String label = image_file_name.substring(0, end_char);

        holder.mTextView.setText(label);
        String url = "file:///android_asset/" + mTopLevel.get(pos) + "/" + image_file_name;
        Glide.with(context).load(url).into(holder.mImageView);
        myTTS.speak(label,TextToSpeech.QUEUE_FLUSH,null);

    }
}
