package com.google.android.gms.samples.vision.ocrreader.ui.project;

import android.widget.RelativeLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Calendar;

/**
 * Created by mgo983 on 12/20/18.
 */

public class InterceptRelativeLayout extends RelativeLayout {

    public InterceptRelativeLayout(Context context){
        super(context);
    }

    public InterceptRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    long startClickTime = Calendar.getInstance().getTimeInMillis();
    final int MAX_CLICK_DURATION = 200;



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                Log.d("InterceptRelative down", " "+ startClickTime + " ");
                break;
                //return true;

            }

        }
        return true;

    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getActionMasked()) {

            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                Log.d("InterceptRelative up", " " + clickDuration + " ");
                if (clickDuration < MAX_CLICK_DURATION) {
                    //callOnClick();
                }
                break;
            }
        }

        return super.onTouchEvent(e);
    }

}
