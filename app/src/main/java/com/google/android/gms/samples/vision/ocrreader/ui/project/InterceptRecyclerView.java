package com.google.android.gms.samples.vision.ocrreader.ui.project;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Calendar;

/**
 * Created by mgo983 on 12/20/18.
 */

public class InterceptRecyclerView extends RecyclerView {

    public InterceptRecyclerView(Context context){
        super(context);
    }

    public InterceptRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    long startClickTime = Calendar.getInstance().getTimeInMillis();
    final int MAX_CLICK_DURATION = 200;



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);

        /*switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                Log.d("InterceptRecycler down", " "+ startClickTime + " ");

                break;
            }

        }
        return false;*/

    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {

        /*switch (e.getActionMasked()) {

            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                Log.d("InterceptRecycler up", " " + clickDuration + " ");
                if (clickDuration < MAX_CLICK_DURATION) {
                    callOnClick();
                }
                break;
            }
        }*/

        return super.onTouchEvent(e);
    }

}
