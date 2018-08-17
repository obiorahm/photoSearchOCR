package com.google.android.gms.samples.vision.ocrreader.Adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

    private ArrayList<ArrayList<String[]>> ingredients = new ArrayList<ArrayList<String[]>>();

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

                //Log.d(LOG_TAG, "the ingredients" + ingredients.get(position).get(0)[0]);

                //position 0 contains the recipe itself
                String anIngridient = ingredients.get(position).get(0)[0];

                String ingredientImageUri = ingredients.get(position).get(1)[0];



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
                final View newConvertView = null;


                viewHolder.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(LOG_TAG + " position ", position + "");

                        // handle getting position when user scrolls view off screen
                        ListView newParent = (ListView) parent;
                        int wantedPosition = position + 1; // Whatever position you're looking for
                        int firstPosition = newParent.getFirstVisiblePosition() - newParent.getHeaderViewsCount(); // This is the same as child #0
                        int wantedChild = wantedPosition - firstPosition;

                        // Say, first visible position is 8, you want position 10, wantedChild will now be 2
                        // So that means your view is child #2 in the ViewGroup:
                        if (wantedChild < 0 || wantedChild >= newParent.getChildCount()) {
                            int i = ((ListView) parent).getFirstVisiblePosition();
                            View firstItemView = parent.getChildAt(i);
                            //deal with scrolling
                            makeFirstItemInvisible(firstItemView, i);
                            getViewByPosition(wantedChild, newConvertView, parent );

                            Log.w(LOG_TAG, "Unable to get view for desired position, because it's not being displayed on screen.");
                            return;
                        }
                        final View corresponding_view = parent.getChildAt(wantedChild);
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
                makeItemsVisible(convertView, position);

                break;
            case TYPE_IMAGE:

                ImageAdapter imageAdapter = new ImageAdapter(context, R.layout.ingredient_image_item);

                ViewHolderTypeImage viewHolderTypeImage = new ViewHolderTypeImage();

                String uriToString = "";
                ArrayList<String[]> stringUri = ingredients.get(position);
                for (String[] child: stringUri){
                    Log.d(LOG_TAG + " uri content ", child[1]);
                    imageAdapter.addItem(child);

                }

                viewHolderTypeImage.mListView = convertView.findViewById(R.id.ingredient_image_list_view);
                //viewHolderTypeImage.mImageView = convertView.findViewById(R.id.test_image);

                LinearLayoutManager layoutManager= new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false);
                viewHolderTypeImage.mListView.setLayoutManager(layoutManager);

                viewHolderTypeImage.mListView.setAdapter(imageAdapter);

                // when the visibility is set to true don't default to gone
                if (convertView.getVisibility() == View.GONE)
                    convertView.setVisibility(View.GONE);

                /*viewHolderTypeImage.mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(LOG_TAG + " position ", position + "");

                    }
                });*/



        }


        return convertView;
    }

    private void makeFirstItemInvisible(View firstItem, int position){

        final int type = getItemViewType(position);
        switch (type){
            case TYPE_FULL_CONTENT:

                // make all containing elements invisible
                ViewHolderTypeFullContent viewHolderTypeFullContent = new ViewHolderTypeFullContent();
                viewHolderTypeFullContent.mImageView = firstItem.findViewById(R.id.ingredient_image);
                viewHolderTypeFullContent.mImageView.setVisibility(View.GONE);

                viewHolderTypeFullContent.mTextView = firstItem.findViewById(R.id.ingredient_name);
                viewHolderTypeFullContent.mTextView.setVisibility(View.GONE);

                viewHolderTypeFullContent.mCheckExclude = firstItem.findViewById(R.id.get_none);
                viewHolderTypeFullContent.mCheckExclude.setVisibility(View.GONE);

                viewHolderTypeFullContent.mCheckExtra = firstItem.findViewById(R.id.get_extra);
                viewHolderTypeFullContent.mCheckExtra.setVisibility(View.GONE);

                firstItem.setVisibility(View.GONE);
                break;
            case TYPE_IMAGE:
                break;



        }

    }


    private void makeItemsVisible(View anyItem, int position){

        final int type = getItemViewType(position);
        switch (type){
            case TYPE_FULL_CONTENT:

                // make all containing elements invisible
                ViewHolderTypeFullContent viewHolderTypeFullContent = new ViewHolderTypeFullContent();
                viewHolderTypeFullContent.mImageView = anyItem.findViewById(R.id.ingredient_image);
                viewHolderTypeFullContent.mImageView.setVisibility(View.VISIBLE);

                viewHolderTypeFullContent.mTextView = anyItem.findViewById(R.id.ingredient_name);
                viewHolderTypeFullContent.mTextView.setVisibility(View.VISIBLE);

                viewHolderTypeFullContent.mCheckExclude = anyItem.findViewById(R.id.get_none);
                viewHolderTypeFullContent.mCheckExclude.setVisibility(View.VISIBLE);

                viewHolderTypeFullContent.mCheckExtra = anyItem.findViewById(R.id.get_extra);
                viewHolderTypeFullContent.mCheckExtra.setVisibility(View.VISIBLE);

                anyItem.setVisibility(View.VISIBLE);
                break;
            case TYPE_IMAGE:
                break;



        }

    }


    public static class ViewHolderTypeFullContent{
        TextView mTextView;
        ImageView mImageView;
        CheckBox mCheckExtra;
        CheckBox mCheckExclude;

    }

    public static class ViewHolderTypeImage{
        RecyclerView mListView;
        //ImageView mImageView;
    }

    public void getViewByPosition(int position, View convertView, ViewGroup parent){
        convertView = inflater.inflate(R.layout.ingredient_image, null);
        makeItemsVisible(convertView, position);
        this.getView(position, convertView, parent);
    }


}
