package com.google.android.gms.samples.vision.ocrreader;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RecipeListAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by mgo983 on 8/7/18.
 */

public class RecipeDialog extends DialogFragment {

    RecipeListAdapter recipeListAdapter;
    private String LOG_TAG = RecipeDialog.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recipe_list, container, false);

        recipeListAdapter = new RecipeListAdapter(getActivity(), R.layout.recipe_item_list);
        try {
            JSONArray ingredientArray = new JSONArray(this.getArguments().getString(WordAdapter.RECIPE_INGREDIENTS));

            Log.d(LOG_TAG, ingredientArray.length() + "");
            for (int i = 0; i < ingredientArray.length(); i++){
                String ingredient [] = new String[2];

                Log.d(LOG_TAG, ingredientArray.getString(i) + "");
                //first add the ingredient
                ingredient[0] = ingredientArray.getString(i);
                recipeListAdapter.addItem(ingredient);
            }

            ListView listView = (ListView) rootView.findViewById(R.id.recipe_list_view);
            listView.setAdapter(recipeListAdapter);
        }catch (JSONException e){
            Log.e(LOG_TAG, e + "");
        }

        return rootView;
    }

}
