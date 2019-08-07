package com.google.android.gms.samples.vision.ocrreader;

import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

public class ObjectsToHide {

    public ProgressBar descriptionProgressBar;
    public RecyclerView descriptionRecyclerView;


    public ObjectsToHide(){

    }

    public ObjectsToHide(ProgressBar descriptionProgressBar,
                         RecyclerView descriptionRecyclerView){
        this.descriptionProgressBar = descriptionProgressBar;
        this.descriptionRecyclerView = descriptionRecyclerView;
    }

}

