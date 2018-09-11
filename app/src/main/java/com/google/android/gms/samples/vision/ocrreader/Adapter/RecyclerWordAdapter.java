package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.BlockSelectDialog;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.RecipeDialog;

import java.util.ArrayList;

/**
 * Created by mgo983 on 9/6/18.
 */

public class RecyclerWordAdapter extends RecyclerView.Adapter<RecyclerWordAdapter.ViewHolder> {

    private Context context;
    private TextToSpeech myTTS;
    private String mealText;
    private LayoutInflater inflater;
    private ArrayList<String[]> mData = new ArrayList<String[]>();

    public static String RECIPE_INGREDIENTS = "com.google.android.gms.samples.vision.ocrreader.RecyclerWordAdapter.RECIPE_INGREDIENTS";

    public static String MEAL_NAME = "com.google.android.gms.samples.vision.ocrreader.RecyclerWordAdapter.MEAL_NAME";

    public static String IMAGE_URI = "com.google.android.gms.samples.vision.ocrreader.RecyclerWordAdapter.IMAGE_URI";

    private final String LOG_TAG = RecyclerWordAdapter.class.getSimpleName();

    private boolean blockOrText = false;





    public  class ViewHolder extends RecyclerView.ViewHolder{
        TextView mTextView;
        ImageView mImageView;

        public ViewHolder(View convertView){
            super(convertView);
            //mTextView = convertView.findViewById(R.id.word_in_text);
            mImageView = convertView.findViewById(R.id.recipe_image);
        }

    }

    public RecyclerWordAdapter(Context context, int resource, TextToSpeech myTTS, String mealText, boolean blockOrText){
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.myTTS = myTTS;
        this.mealText = mealText;
        this.blockOrText = blockOrText;
    }

    @Override

    public RecyclerWordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View convertView = inflater.inflate(R.layout.gridview_item, null);
        return new ViewHolder(convertView);

    }

    @Override
    public void onBindViewHolder(final RecyclerWordAdapter.ViewHolder holder, final int position){



        final Uri uri = Uri.parse(mData.get(position)[0]);

        Log.d(LOG_TAG, mData.get(position)[0]);

        Glide.with(context).load(uri).into(holder.mImageView);


        //say all the words
        /*holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wholeString = getMealName();

                myTTS.speak(wholeString, TextToSpeech.QUEUE_FLUSH, null);
            }
        });*/


        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blockOrText){
                    setBlockDialog(uri);
                }else {
                    DialogFragment newFragment = new RecipeDialog();
                    Bundle bundle = new Bundle();
                    bundle.putString(RECIPE_INGREDIENTS, mData.get(position)[1]);
                    //bundle.putString(EXTRA_DIALOG_IMAGE, imgFile.toString());
                    bundle.putString(MEAL_NAME, mealText);
                    newFragment.setArguments(bundle);
                    newFragment.show(((Activity) context).getFragmentManager(),"recipeDialogFragment");

                }
            }

        });

    }

    private String getMealName(){
        String wholeString = mData.get(0)[1];
        for (int i = 1; i < mData.size(); i++){
            wholeString += " " + mData.get(i)[1];
        }
        return wholeString;
    }

    @Override
    public int getItemCount(){
        return mData.size();
    }

    public void addItem(String[] wordInMeal){
        mData.add(wordInMeal);
    }

    private void setBlockDialog(Uri uri){
        DialogFragment blockDialogFragment = new BlockSelectDialog();
        Bundle bundle = new Bundle();
        bundle.putString(FetchMealDetails.WHOLE_ORDER, mealText);
        bundle.putString( IMAGE_URI , uri.toString());
        blockDialogFragment.setArguments(bundle);
        blockDialogFragment.show(((Activity) context).getFragmentManager(), "blockDialogFragment");
    }

}
