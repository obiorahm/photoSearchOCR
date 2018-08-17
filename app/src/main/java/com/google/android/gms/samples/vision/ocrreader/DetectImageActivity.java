package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
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

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;


@Override
    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_image_detection);

    mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.second_graphic_overlay);


    Context context = getApplicationContext();

    Frame outputFrame = null;

    ImageView imageView = findViewById(R.id.image_to_detect);





    //get image and convert to frame
    try{
        InputStream inputStream = getAssets().open("farmhouse.png");

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

        Glide.with(this).load(Uri.parse("file:///android_asset/farmhouse.png")).into(imageView);

        //bitmap to frame
        outputFrame = new Frame.Builder().setBitmap(bmp).build();

    }catch (IOException e){

        Log.e(LOG_TAG, e + "");

    }
    //detect image
    TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
    SparseArray<TextBlock> result = textRecognizer.detect(outputFrame);
    //textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

    for (int i = 0; i < result.size(); i++){
        if (result.get(i) != null){
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, result.get(i));
            mGraphicOverlay.add(graphic);
            Log.d(LOG_TAG + " size : ", result.get(i).getValue() + "");
        }
    }

}
}
