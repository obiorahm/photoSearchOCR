package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * Created by mgo983 on 9/9/18.
 */

public class DynamicOptions {

    LinkedHashMap<String, String[]> mDataPair = new LinkedHashMap<String, String[]>();
    String textBlock;
    private static final String DB_REF_WORD = "word";
    private static final String LOG_TAG = DynamicOptions.class.getSimpleName();
    private ImageView imageView;
    private String foodText;
    private final static String WORD_IMAGE_REFERENCE  = "symbols";
    private  Context context;


    public static final ArrayList<String> foodCategories = new ArrayList<>();
    static {
        foodCategories.add("alcoholic_beverage");
        foodCategories.add("canned_food");
        foodCategories.add("condiments");
        foodCategories.add("dairy");
        foodCategories.add("drinks");
        foodCategories.add("fast_food");
        foodCategories.add("fish");
        foodCategories.add("fishes");
        foodCategories.add("food");
        foodCategories.add("foods");
        foodCategories.add("fruits");
        foodCategories.add("grains");
        foodCategories.add("herbs_and_spices");
        foodCategories.add("juices");
        foodCategories.add("junk_food");
        foodCategories.add("meat");
        foodCategories.add("sandwiches");
        foodCategories.add("seafood");
        foodCategories.add("snacks");
        foodCategories.add("soups");
        foodCategories.add("sweets");
        foodCategories.add("vegetables");
    }


    public DynamicOptions(ImageView imageView, String foodText, Context context){
        this.imageView =imageView;
        this.foodText = foodText;
        this.context = context;
        Log.d(LOG_TAG, " full text token " + foodText);
    }


    public void load(){
        SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
        String token [] = simpleTokenizer.tokenize(foodText);

        loadImage(token, 0);

    }

    private void loadImage(final String [] token, final int count){

        if (count >= token.length)
            return;

        String searchString = token[count].toLowerCase().trim();
        Log.d(LOG_TAG, " token " + searchString + " " + count + " " + token.length);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD);

        databaseReference.orderByKey().startAt(searchString).endAt(searchString+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "got here");
                if (!dataSnapshot.exists()){
                    loadImage(token, count + 1);
                    Log.d(LOG_TAG, " token data does not exist");
                }else {
                    for (DataSnapshot child : dataSnapshot.getChildren()){

                        for(DataSnapshot grandChild : child.getChildren()){

                            final String grandChildValue [] = grandChild.getValue().toString().split("/");

                            if (foodCategories.contains(grandChildValue[0])){
                                //now that we have the file name retrieve image from firebase storage
                                StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference();

                                firebaseStorage.child( WORD_IMAGE_REFERENCE + "/" + grandChildValue[0] + "/" + grandChildValue[2])
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                Glide.with(context).load(uri).into(imageView);
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

                    loadImage(token, count + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
