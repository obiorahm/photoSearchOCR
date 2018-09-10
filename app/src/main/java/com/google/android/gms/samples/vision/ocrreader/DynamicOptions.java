package com.google.android.gms.samples.vision.ocrreader;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by mgo983 on 9/9/18.
 */

public class DynamicOptions {

    LinkedHashMap<String, String[]> mDataPair = new LinkedHashMap<String, String[]>();
    String textBlock;
    static final String DB_REF_WORD = "word";

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


    public DynamicOptions(LinkedHashMap<String, String[]> mDataPair, String textBlock){
        this.mDataPair = mDataPair;
        this.textBlock = textBlock;
    }

    private void getFoodItemImages(){
        ProcessTextBlock processTextBlock = new ProcessTextBlock(textBlock);
        String processedText = processTextBlock.processText();
        String [] processedTexts = processedText.split(",");

        List<String> listProcessedTexts = Arrays.asList(processedText);

        //start from 1 zero is the title;
        process(listProcessedTexts, 1);


    }

    private void process (List<String> listProcessedTexts, int count){
        if (count >=  listProcessedTexts.size())
            return;

        String tokenizedString[] = listProcessedTexts.get(count).split(" ");
        //convert to list
        List<String> listTokenizedString = Arrays.asList(tokenizedString);
        findFoodInString(listTokenizedString, 0);
        process(listProcessedTexts, count + 1);

    }

    private void findFoodInString(List<String > listTokenizedString, int tokenCount){
        if (tokenCount >= listTokenizedString.size()){
            return;
        }
        String searchWord = listTokenizedString.get(tokenCount).toLowerCase();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(DB_REF_WORD).child(searchWord);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }
}
