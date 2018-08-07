package com.google.android.gms.samples.vision.ocrreader;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mgo983 on 8/7/18.
 */

public class EdmanJasonReader {

    JSONObject JSONEntry;

    private final String LOG_TAG = EdmanJasonReader.class.getSimpleName();

    public EdmanJasonReader(String JSONString){
        try{
            JSONEntry = new JSONObject(JSONString);

        }catch (JSONException e ){

        }
    }

    public ArrayList<String []> getRecipe(){
        ArrayList<String [] > tenRecipes = new ArrayList<>();
        try{
            JSONArray hits = JSONEntry.getJSONArray("hits");

            for (int i = 0; i < hits.length(); i++){
                String[] recipeInfo = new String[2];
                JSONObject recipe = hits.getJSONObject(i);
                recipeInfo[0] = recipe.getJSONObject("recipe").getString("image");
                Log.d(LOG_TAG, recipeInfo[0]);
                tenRecipes.add(recipeInfo);

            }
            return tenRecipes;

        }catch (JSONException e){

        }
        return null;
    }
}
