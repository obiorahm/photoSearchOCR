package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.samples.vision.ocrreader.Adapter.WordAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by mgo983 on 8/6/18.
 */

public class FetchMealDetails extends AsyncTask<String, Void, String> {

    public final static String BASEURL = "https://api.edamam.com/search";
    public final static String API_KEY = "37345295e38efe1ea020cbc391ee11a8";
    public final static String API_ID = "0940281c";
    public final static String NUM_TO = "10";
    public final static String NUM_FROM = "0";

    private final String LOG_TAG = FetchMealDetails.class.getSimpleName();

    //private WordAdapter adapter;
    private RecyclerWordAdapter adapter;
    private Context context;
    private UseRecyclerActivity useRecyclerActivity;



    /*public FetchMealDetails(WordAdapter wordAdapter, Context context){
        adapter = wordAdapter;
        this.context = context;
    }*/

    public FetchMealDetails(RecyclerWordAdapter recyclerWordAdapter, Context context){
        adapter = recyclerWordAdapter;
        this.context = context;
        useRecyclerActivity = ((UseRecyclerActivity) context);
    }



    @Override
    protected  String doInBackground(String...params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;


        try{

            Uri uri = buildEdmamUri(params[0]);

            URL url = new URL(uri.toString());
            Log.v(LOG_TAG,"The built Uri " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0){
                return null;
            }

            //Log.d(LOG_TAG, buffer.toString());


            String  testString = buffer.toString();
            return testString;

        }catch (IOException e){
            Log.e(LOG_TAG, "Error", e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }



    private Uri buildEdmamUri(String queryParameter){
        final String API_KEY_NAME = "app_key";
        final String API_ID_NAME = "app_id";
        final String NUM_TO_NAME = "to";
        final String NUM_FROM_NAME = "from";
        final String QUERY = "q";

        Uri buildUri;

        buildUri = Uri.parse(BASEURL).buildUpon()
                .appendQueryParameter(API_ID_NAME, API_ID)
                .appendQueryParameter(API_KEY_NAME,API_KEY)
                .appendQueryParameter(NUM_FROM_NAME, NUM_FROM)
                .appendQueryParameter(NUM_TO_NAME,NUM_TO)
                .appendQueryParameter(QUERY, queryParameter)

                .build();
        return buildUri;
    }


    @Override
    protected void onPostExecute(String Result){
        //JSONObject newJSONObject = new JSONObject(Result);
        try{
            EdmanJasonReader edmanJasonReader = new EdmanJasonReader(Result);
            ArrayList<String[]> edmanInfo = edmanJasonReader.getRecipe();

            // make progress bar invisible
            ProgressBar searchingEdmame = ((Activity) context).findViewById(R.id.searching_edmame);
            searchingEdmame.setVisibility(View.GONE);

            // no results returned
            if (edmanInfo.size() == 0){
                TextView textViewNoResult = ((Activity) context).findViewById(R.id.no_result);
                textViewNoResult.setVisibility(View.VISIBLE);
            }

            for (String[] recipeInformation : edmanInfo){
                adapter.addItem(recipeInformation);
            }

        }catch (NullPointerException e){
            Log.e(LOG_TAG, e + "");
        }

        /*GridView wholeWordGridView =  ((Activity) context).findViewById(R.id.gridview_edit_meal);
        wholeWordGridView.setAdapter(adapter);*/

        //setView(((Activity) context), (RecyclerView) ((Activity) context).findViewById(R.id.gridview_edit_meal));
        useRecyclerActivity.setView(adapter, (RecyclerView) ((Activity) context).findViewById(R.id.gridview_edit_meal));


    }

}
