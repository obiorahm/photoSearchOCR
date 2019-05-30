package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.samples.vision.ocrreader.Adapter.RecyclerWordAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

/**
 * Created by mgo983 on 9/6/18.
 */

public class UseRecyclerActivity extends Activity  {

    public static TextToSpeech myTTS;

    //selected meal, restaurant
    public static String selected_item = "";

    public static RecyclerView last_parent_di;

    public static RelativeLayout last_rl_parent;

    public static Object [] last_selected_object;

    public void setView(RecyclerWordAdapter adapter, RecyclerView recyclerView, ArrayList<String[]> edamanInfo){}

    public void setView(RecyclerWordAdapter adapter, ArrayList<String[]> edamanInfo){}

    public void setView( ArrayList<String []> edmanInfo){}

    public void processWebResults(Document document){}

    public void beginFetchRestaurantLogos(HashMap<String, String[]> restaurantInfo){}

    public String LOG_TAG;

    private static final String DB_REF_WORD = "word";

    private final static String WORD_IMAGE_REFERENCE  = "symbols";




    /**
     *
     * @param lngLatPack contains JSONdata for location in string
     */
    public void addLongLatToAdapter(HashMap<String, String[]> lngLatPack){

    }

    /**
     *
     * @param imageUrlPack
     */

    public void addImageUrlToAdapter(HashMap<String, String[]> imageUrlPack){

    }


    /**
     *
     * @param streetViewPanoramaView view to place the panorama
     * @param longitude the longitude and latitude of the restaurant
     *
     */

    public void setUpPanorama(StreetViewPanoramaView streetViewPanoramaView, String longitude, String Latitude ){

    }

    /**
     *
     * @param imageView comes from the TextByTextAdapter
     */
    public void displayTextByTextImage(ImageView imageView){

    }


    public void loadImage(final String  token,  ImageView imageView){

        if (token == null)
            return;

        String searchString = token.toLowerCase().trim();
        Log.d(LOG_TAG, " token " + searchString );
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD);

        databaseReference.orderByKey().startAt(searchString).endAt(searchString+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "got here");
                if (!dataSnapshot.exists()){
                    return;
                }else {
                    for (DataSnapshot child : dataSnapshot.getChildren()){

                        for(DataSnapshot grandChild : child.getChildren()){

                            final String grandChildValue [] = grandChild.getValue().toString().split("/");

                            if (DynamicOptions.foodCategories.contains(grandChildValue[0])){
                                //now that we have the file name retrieve image from firebase storage
                                StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                                firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + grandChildValue[0] + "/" + grandChildValue[2])
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                Glide.with(getApplicationContext()).load(uri).into(imageView);
                                                return;

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOG_TAG, "could not load image");
                                    }
                                });
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

}
