package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.MealMenuActivity;
import com.google.android.gms.samples.vision.ocrreader.R;



import java.util.ArrayList;


import java.util.TreeSet;


/**
 * Created by mgo983 on 8/7/18.
 */

public class RecipeListAdapter extends ArrayAdapter {

    private ArrayList<ArrayList<String>> ingredients = new ArrayList<ArrayList<String>>();

    private LayoutInflater inflater;
    private Context context;
    private String LOG_TAG = RecipeListAdapter.class.getSimpleName();
    private TextToSpeech myTTS;

    //ImageAdapter adapter;



    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_FULL_CONTENT = 1;
    private static final int TYPE_MAX_COUNT = TYPE_FULL_CONTENT + 1;

    private TreeSet imageSet = new TreeSet();
    private TreeSet fullContentSet = new TreeSet();



    public RecipeListAdapter(Context context, int resource){
        super(context, resource);
        this.context = context;
        inflater = LayoutInflater.from(context);
        //ImageAdapter for list in this list adapter
        //this.adapter = adapter;
    }

    public void addItem(ArrayList an_ingredient){
        ingredients.add(an_ingredient);
        notifyDataSetChanged();
    }

    public void addImage(ArrayList imageUrl){
        ingredients.add(imageUrl);
        imageSet.add(ingredients.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType (int position){
        if(imageSet.contains(position)){
            return TYPE_IMAGE;
        }else{
            return TYPE_FULL_CONTENT;
        }
    }

    @Override
    public int getViewTypeCount(){
        return TYPE_MAX_COUNT;
    }


    @Override
    public int getCount(){
        return ingredients.size();
    }

    @Override
    public ArrayList getItem(int position){
        return (ArrayList) ingredients.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {

        final int type = getItemViewType(position);

        if (convertView == null){
            switch (type){
                case TYPE_FULL_CONTENT:
                    convertView = inflater.inflate(R.layout.recipe_item_list, null);
                    break;
                case TYPE_IMAGE:
                    convertView = inflater.inflate(R.layout.ingredient_image, null);


            }
        }


        switch (type){
            case TYPE_FULL_CONTENT:

                ViewHolderTypeFullContent viewHolder = new ViewHolderTypeFullContent();
                viewHolder.mTextView = (TextView) convertView.findViewById(R.id.ingredient_name);

                Log.d(LOG_TAG, "the ingredients" + ingredients.get(position).get(0));

                //position 0 contains the recipe itself
                String anIngridient = ingredients.get(position).get(0);

                String ingredientImageUri = ingredients.get(position).get(1);



                viewHolder.mTextView.setText(anIngridient);

                final String spoken_food = anIngridient;

                viewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myTTS = ((MealMenuActivity) context).myTTS;

                        myTTS.speak(spoken_food, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });

                viewHolder.mImageView = convertView.findViewById(R.id.ingredient_image);
                Glide.with(context).load(ingredientImageUri).into(viewHolder.mImageView);

                //getImageUrls(ingredients.get(position).get(0),position ,viewHolder.mImageView);
                viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final View corresponding_view = parent.getChildAt(position + 1);
                        ViewHolderTypeImage viewHolderTypeImage = new ViewHolderTypeImage();
                        if (corresponding_view != null){
                            int visibility = corresponding_view.getVisibility();
                            if (visibility == View.GONE){
                                corresponding_view.setVisibility(View.VISIBLE);
                                viewHolderTypeImage.mListView = corresponding_view.findViewById(R.id.ingredient_image_list_view);
                                viewHolderTypeImage.mListView.setVisibility(View.VISIBLE);


                            }else{
                                corresponding_view.setVisibility(View.GONE);
                                viewHolderTypeImage.mListView = corresponding_view.findViewById(R.id.ingredient_image_list_view);
                                viewHolderTypeImage.mListView.setVisibility(View.GONE);


                            }

                        }

                    }
                });

                break;
            case TYPE_IMAGE:
                //adapter.addItem(ingredients.get(position));


                //add item to adapter here
                //getImageUrls(ingredients.get(position).get(0), imageAdapter);

                ImageAdapter imageAdapter = new ImageAdapter(context, R.layout.ingredient_image_item);

                ViewHolderTypeImage viewHolderTypeImage = new ViewHolderTypeImage();

                String uriToString = "";
                ArrayList<String> stringUri = ingredients.get(position);
                for (String child: stringUri){
                    Log.d(LOG_TAG + " uri content ", child);
                    imageAdapter.addItem(child);

                }

                viewHolderTypeImage.mListView = convertView.findViewById(R.id.ingredient_image_list_view);

                viewHolderTypeImage.mListView.setAdapter(imageAdapter);
                convertView.setVisibility(View.GONE);



        }


        return convertView;
    }


    public static class ViewHolderTypeFullContent{
        TextView mTextView;
        ImageView mImageView;

    }

    public static class ViewHolderTypeImage{
        ListView mListView;
        ImageView mImageView;
    }


}
