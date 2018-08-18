package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.FetchMealDetails;
import com.google.android.gms.samples.vision.ocrreader.MealMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.RecipeDialog;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/6/18.
 */

public class WordAdapter extends ArrayAdapter {

    private LayoutInflater inflater;
    private Context context;
    private TextToSpeech myTTs;
    private ArrayList<String[]> mData = new ArrayList<String[]>();

    private final String LOG_TAG = WordAdapter.class.getSimpleName();

    public static String RECIPE_INGREDIENTS = "com.google.android.gms.samples.vision.ocrreader.Adapter.RECIPE_INGREDIENTS";

    public WordAdapter(Context context, int resource, TextToSpeech myTTS){
        super(context,resource);
        this.myTTs = myTTS;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addItem(String[] wordInMeal){
        mData.add(wordInMeal);
    }

    @Override
    public int getCount(){
        return mData.size();
    }

    @Override
    public String[] getItem(int position){
        return (String []) mData.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null){
            convertView = inflater.inflate(R.layout.gridview_item, null);
        }

        final TextView textView = (TextView) convertView.findViewById(R.id.word_in_text);
        //textView.setText(mData.get(position)[1]);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.recipe_image);

        Uri uri = Uri.parse(mData.get(position)[0]);

        Log.d(LOG_TAG, mData.get(position)[0]);

        Glide.with(context).load(uri).into(imageView);

        myTTs.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                textView.setHighlightColor(((Activity) context).getResources().getColor(R.color.common_google_signin_btn_text_dark_focused));
            }

            @Override
            public void onDone(String s) {
                textView.setHighlightColor(((Activity) context).getResources().getColor(R.color.common_google_signin_btn_text_light_disabled));

            }

            @Override
            public void onError(String s) {

            }
        });

        //say all the words
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wholeString = mData.get(0)[1];
                for (int i = 1; i < mData.size(); i++){
                    wholeString += " " + mData.get(i)[1];
                }
                myTTs.speak(wholeString, TextToSpeech.QUEUE_FLUSH, null);
            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new RecipeDialog();
                Bundle bundle = new Bundle();
                bundle.putString(RECIPE_INGREDIENTS, mData.get(position)[1]);
                //bundle.putString(EXTRA_DIALOG_IMAGE, imgFile.toString());

                newFragment.setArguments(bundle);
                newFragment.show(((Activity) context).getFragmentManager(),"what?");
            }

            });






        return convertView;
    }
}
