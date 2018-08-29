package com.google.android.gms.samples.vision.ocrreader.ui.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by mgo983 on 8/27/18.
 */

public class ImageViewPreview extends ViewGroup {

    private String TAG = ImageViewPreview.class.getSimpleName();

    private Context mContext;
    private ImageView mImageView;
    private Bitmap mBitmap;
    private GraphicOverlayFB mGraphicOverlay;

    public ImageViewPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        mImageView = new ImageView(context);
        addView(mImageView);
    }

    public void setmBitmap(Bitmap bitmap){
        mBitmap = bitmap;
    }

    public void setmGraphicOverlay(GraphicOverlayFB graphicOverlay){
        mGraphicOverlay = graphicOverlay;
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int previewWidth = 320;
        int previewHeight = 240;


        if (mBitmap != null) {
            previewWidth = mBitmap.getWidth();
            previewHeight = mBitmap.getHeight();
            Log.d(TAG, " previewWidth " + previewWidth + " previewHeight " + previewHeight );
        }


        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            // One dimension will be cropped.  We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            if (mGraphicOverlay != null) {
                Log.d(TAG, "graphicOverlay is not null ");
                //Size size = mCameraSource.getPreviewSize();
                int min = Math.min(previewWidth, previewHeight);
                int max = Math.max(previewWidth, previewHeight);
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mGraphicOverlay.setCameraInfo(min, max, 0);
                } else {
                    mGraphicOverlay.setCameraInfo(max, min, 1);
                }

            }
            mImageView.setImageBitmap(mBitmap);

            //startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG,"Do not have permission to start the camera", se);
        }
    }

    private boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
