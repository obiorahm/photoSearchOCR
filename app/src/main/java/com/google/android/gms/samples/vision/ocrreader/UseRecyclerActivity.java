package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;

import opennlp.tools.stemmer.PorterStemmer;

/**
 * Created by mgo983 on 9/6/18.
 */

public class UseRecyclerActivity extends FragmentActivity  {

    public static TextToSpeech myTTS;

    //selected meal, restaurant
    public static String selected_item = "";

    public static String selected_url = "";

    public static RecyclerView last_parent_di;

    public static RelativeLayout last_rl_parent;

    public static Object [] last_selected_object;


    public void setView(RecyclerWordAdapter adapter, ArrayList<String[]> edamanInfo){}

    public void setView(RecyclerWordAdapter adapter, HashMap edamanInfo){}

    public void setView( ArrayList<String []> edmanInfo){}

    public void setView( HashMap<String, String []> edmanInfo){}

    public void processWebResults(Document document){}

    public String LOG_TAG;

    private static final String DB_REF_WORD = "word";

    private final static String WORD_IMAGE_REFERENCE  = "symbols";

    private int CHUNK_ROOT_POS = 0;

    private int CHUNK_POS = 1;

    private int IMAGE_URL_POS = 2;




    /**
     *
     * @param streetViewPanoramaView view to place the panorama
     * @param longitude the longitude and latitude of the restaurant
     *
     */

    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView, String longitude, String Latitude ){

    }


    public void setUpPanorama( ){

    }






    public void loadImage(final String[]  token,  SetAdapter adapter, boolean notFoodItem){

        if (token == null)
            return;

        String rawString = token[CHUNK_ROOT_POS];

        String searchString = rawString.toLowerCase().trim();
        Log.d(LOG_TAG, " token " + searchString );
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD);

        //databaseReference.orderByKey().startAt(searchString).endAt(searchString+"\uf8ff").addValueEventListener(new ValueEventListener() {
        databaseReference.orderByKey().equalTo(searchString).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "got here");
                if (!dataSnapshot.exists()){
                    searchWithStemmer(token, adapter, notFoodItem);
                }else {
                    for (DataSnapshot child : dataSnapshot.getChildren()){

                        for(DataSnapshot grandChild : child.getChildren()){

                            final String grandChildValue [] = grandChild.getValue().toString().split("/");

                            if (DynamicOptions.foodCategories.contains(grandChildValue[0]) || notFoodItem){
                                //now that we have the file name retrieve image from firebase storage
                                StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                                firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + grandChildValue[0] + "/" + grandChildValue[2])
                                        .getDownloadUrl()
                                        .addOnSuccessListener((Uri uri)->
                                    adapter.addImageUrl(rawString, uri))
                                        .addOnFailureListener((@NonNull Exception e)->
                                    Log.d(LOG_TAG, "could not load image")
                                );
                                Log.d(LOG_TAG, " grandChild " + grandChild.getValue().toString());

                                return;
                            }
                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }











    private void searchWithStemmer(final String[]  token,  SetAdapter adapter, boolean notFoodItem){

        PorterStemmer porterStemmer = new PorterStemmer();

        String rawString = token[CHUNK_ROOT_POS];

        String searchString = porterStemmer.stem(rawString);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD);

        databaseReference.orderByKey().equalTo(searchString).addValueEventListener(new ValueEventListener() {
        //databaseReference.orderByKey().startAt(searchString).endAt(searchString+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "got here");
                if (dataSnapshot.exists()){

                    for (DataSnapshot child : dataSnapshot.getChildren()){

                        for(DataSnapshot grandChild : child.getChildren()){

                            final String grandChildValue [] = grandChild.getValue().toString().split("/");

                            if (DynamicOptions.foodCategories.contains(grandChildValue[0]) || notFoodItem){
                                //now that we have the file name retrieve image from firebase storage
                                StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                                firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + grandChildValue[0] + "/" + grandChildValue[2])
                                        .getDownloadUrl()
                                        .addOnSuccessListener((Uri uri)-> {
                                    adapter.addImageUrl(rawString, uri);                                                //Glide.with(getApplicationContext()).load(uri).into(imageView);
                                    //setAdapter.mNotifyDataSetChanged();

                                }).addOnFailureListener((@NonNull Exception e)->{
                                            Log.d(LOG_TAG, "could not load image");
                                            searchWithEngine(rawString, adapter);

                                        }
                                );
                                Log.d(LOG_TAG, " grandChild " + grandChild.getValue().toString());

                                return;
                            }
                        }

                    }

                }else{
                    Log.d(LOG_TAG, "runEngine");
                    searchWithEngine(rawString, adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchWithEngine(String searchString, SetAdapter adapter){
        FetchImageEngine fetchImageEngine = new FetchImageEngine(adapter);
        fetchImageEngine.execute(searchString);


    }
}
