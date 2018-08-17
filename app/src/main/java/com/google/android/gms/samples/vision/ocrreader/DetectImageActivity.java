package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mgo983 on 8/17/18.
 */

public class DetectImageActivity extends Activity {

String LOG_TAG = DetectImageActivity.class.getSimpleName();
@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    Context context = getApplicationContext();

    Frame outputFrame = null;

    //get image and convert to frame
    try{
        InputStream inputStream = getAssets().open("farmhouse.png");

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

        //bitmap to frame
        outputFrame = new Frame.Builder().setBitmap(bmp).build();

    }catch (IOException e){

        Log.e(LOG_TAG, e + "");

    }
    //detect image
    TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
    SparseArray<TextBlock> result = textRecognizer.detect(outputFrame);

    for (int i = 0; i < result.size(); i++){
        if (result.get(i) != null){
            Log.d(LOG_TAG + " size : ", result.get(i).getValue() + "");
        }
    }

}
}
