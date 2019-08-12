package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class  FetchImageEngine extends AsyncTask<String, Void, String[]> implements ImageEngine {


    String LOG_TAG = FetchImageEngine.class.getSimpleName();


    public SetAdapter adapter;

    public  FetchImageEngine(){}

    FetchImageEngine(SetAdapter adapter){
        this.adapter = adapter;
    }


    @Override
    protected  String[] doInBackground(String...params) {
        if (params.length == 0)
            return null;
        String searchString = params[0];

        Uri uri = runEngine(params[0]);

        String uriString = uri == null? null : uri.toString();

        String[] result = {searchString, uriString};



        return result;

    }

    private Uri runEngine( String searchstring){

        Uri uri = buildUrl(searchstring);

        Uri result = handleJSON(getJSONData(uri));

        return result;
    }



    private String getJSONData(Uri SearchUri){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;


        try{

            Uri buildUri;

            buildUri = SearchUri;

            URL url = new URL(buildUri.toString());
            Log.v(LOG_TAG,"The built Uri " + url);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
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


            return buffer.toString();

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








    @Override
    protected void onPostExecute(String[] Result) {

        String searchString = Result[0];
        String uriString = Result[1];


        Log.d(LOG_TAG, "pixabay searched string " + uriString);


        if (uriString == null){
            nextEngine(searchString);
        }else{
            Uri uri = Uri.parse(uriString);
            adapter.addImageUrl(searchString, uri);

        }

    }

    @Override
    public void nextEngine(String searchString){

    }

    public Uri buildUrl (String queryParameter){
            return  null;
    }

    public Uri handleJSON(String JSONData){
        return null;
    }

}
