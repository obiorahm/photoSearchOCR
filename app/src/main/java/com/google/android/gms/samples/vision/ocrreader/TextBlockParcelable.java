package com.google.android.gms.samples.vision.ocrreader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import com.google.android.gms.vision.text.TextBlock;

/**
 * Created by mgo983 on 8/19/18.
 */

/*
public class TextBlockParcelable implements Parcelable {
    private SparseArray<Object> item;

    @Override
    public int describeContents(){
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeSparseArray(item);
    }

    /@Override
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        public TextBlockParcelable createFromParcel(Parcel in){
            return new TextBlockParcelable(in);
        }
    };

    private TextBlockParcelable(Parcel in){
        item = in.readSparseArray();
    }
}
*/