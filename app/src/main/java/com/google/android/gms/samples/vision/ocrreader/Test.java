package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by mgo983 on 9/10/18.
 */

public class Test {

    private static String LOG_TAG = DynamicOptions.class.getSimpleName();

    public Test(){}

    public static void testDynamicOptions(Context context){
        Log.d(LOG_TAG, "-------- Testing DynamicOptions --------");
        ImageView imageView = new ImageView(context);

        String [] mealText = {/*"Prawn Tempura",*/ "Crispy Tiger Prawn"/*, "Tempura",  /*"Ten-Shuyu Sauce"*/};
        for (String child : mealText){
            DynamicOptions dynamicOptions = new DynamicOptions(imageView, child, context);
            dynamicOptions.load();
        }
        RelativeLayout relativeLayout = ((Activity) context).findViewById(R.id.main_view);
        relativeLayout.addView(imageView);
    }

    public static void testProcessText(){
      String a = "$ab";
        String b  = "&$ab";
        String c = "9ab and ";
        String [] d = {a, b, c, "ab(serious)" };
        for (String child : d){
            ProcessTextBlock y = new ProcessTextBlock(child);
            Log.d("Process Text", child + " : " + y.processText());
            Log.d("Process Text", android.hardware.Camera.getNumberOfCameras() + " ");

        }
    }
}
