package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

public class ObjectsToHide {

    public ProgressBar descriptionProgressBar ;
    public RecyclerView descriptionRecyclerView ;


    public ObjectsToHide(){

    }

    public ObjectsToHide(Context context){
        descriptionProgressBar = new ProgressBar(context);
        descriptionRecyclerView = new RecyclerView(context);
    }

    public ObjectsToHide(ProgressBar descriptionProgressBar,
                         RecyclerView descriptionRecyclerView){
        this.descriptionProgressBar = descriptionProgressBar;
        this.descriptionRecyclerView = descriptionRecyclerView;
    }

}

