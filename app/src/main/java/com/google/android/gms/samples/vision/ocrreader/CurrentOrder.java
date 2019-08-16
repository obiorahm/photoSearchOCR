package com.google.android.gms.samples.vision.ocrreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CurrentOrder implements Serializable {

    public String mealName;
    public String categoryName;
    public HashMap<String, String> descriptions;


    public CurrentOrder(String mealName, HashMap descriptions, String categoryName){
        this.mealName = mealName;
        this.descriptions = descriptions;
        this.categoryName = categoryName;
    }



    public void addDescription(HashMap descriptions){
        this.descriptions = descriptions;
    }


}
