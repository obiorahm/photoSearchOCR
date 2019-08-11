package com.google.android.gms.samples.vision.ocrreader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CurrentOrder implements Serializable {

    public String mealName;
    public HashMap<String, String> descriptions;


    public CurrentOrder(String mealName, HashMap descriptions){
        this.mealName = mealName;
        this.descriptions = descriptions;
    }



    public void addDescription(HashMap descriptions){
        this.descriptions = descriptions;
    }


}
