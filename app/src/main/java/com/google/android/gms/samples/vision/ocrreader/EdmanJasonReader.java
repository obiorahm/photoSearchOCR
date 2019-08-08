package com.google.android.gms.samples.vision.ocrreader;

import android.util.Log;

import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by mgo983 on 8/7/18.
 */

public class EdmanJasonReader {

    JSONObject JSONEntry;

    public static final int URL = 0;
    public static final int INGREDIENTS = 1;
    public static final int NAME = 2;
    public static final String EMPTY = "";

    private final String LOG_TAG = EdmanJasonReader.class.getSimpleName();

    public EdmanJasonReader(String JSONString){
        try{

            JSONEntry = new JSONObject(JSONString);

        }catch (JSONException e ){
            Log.e(LOG_TAG, e + "");

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + "");
        }
    }

    public EdmanJasonReader(){

    }


    public ArrayList<String []> getRecipe(HashMap<String, String> result){
        ArrayList<String []> AllRecipes = new ArrayList<>();

        try{

            Iterator resultIterator = result.entrySet().iterator();
            while (resultIterator.hasNext()) {
                HashMap.Entry item = (HashMap.Entry) resultIterator.next();

                JSONEntry = new JSONObject((String) item.getValue());

                JSONArray hits = JSONEntry.getJSONArray("hits");

                if (hits.length() == 0){
                    String[] recipeInfo = new String[3];
                    // set url
                    recipeInfo[URL] = EMPTY;
                    //get ingredients
                    recipeInfo[INGREDIENTS] = EMPTY;
                    //include meal name
                    recipeInfo[NAME] = (String) item.getKey();

                    //Log.d(LOG_TAG, recipeInfo[2]);
                    AllRecipes.add(recipeInfo);

                }else{
                    for (int i = 0; i < hits.length(); i++){
                        String[] recipeInfo = new String[3];
                        JSONObject recipe = hits.getJSONObject(i);
                        // get url
                        recipeInfo[URL] = recipe.getJSONObject("recipe").getString("image");
                        //get ingredients
                        recipeInfo[INGREDIENTS] = recipe.getJSONObject("recipe").getJSONArray("ingredientLines").toString();
                        //include meal name
                        recipeInfo[NAME] = (String) item.getKey();

                        Log.d(LOG_TAG, recipeInfo[2]);
                        AllRecipes.add(recipeInfo);

                    }

                }


            }

        }catch (JSONException e ){
            Log.e(LOG_TAG, e + "");

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + "");
        }

        return AllRecipes;
    }

}
