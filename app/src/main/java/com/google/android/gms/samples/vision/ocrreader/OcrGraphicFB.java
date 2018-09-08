package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlayFB;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

/**
 * Created by mgo983 on 8/24/18.
 */

public class OcrGraphicFB extends GraphicOverlayFB.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;

    private Paint sRectPaint;
    private static Paint sTextPaint;
    private FirebaseVisionText.TextBlock mText = null;

    private FirebaseVisionText.Line mLine = null;

    OcrGraphicFB(GraphicOverlayFB overlay, FirebaseVisionText.TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(35.0f);

        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }

    public void  setsRectPaint(int color){
        sRectPaint.setColor(color);
        //redraw the overlay as this graphic has been added
        postInvalidate();
    }

    public void setRecPaintStrokeWidth(float strokeWidth){
        sRectPaint.setStrokeWidth(strokeWidth);
    }

    public int getsRectPaint(){
        return sRectPaint.getColor();
    }

    OcrGraphicFB(GraphicOverlayFB overlay, FirebaseVisionText.Line line ){
        super(overlay);

        mLine = line;
        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(35.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();


    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public FirebaseVisionText.TextBlock getTextBlock() {
        return mText;
    }

    public FirebaseVisionText.Line getLine() {return  mLine; }

    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionText.TextBlock text = mText;
        FirebaseVisionText.Line line = mLine;
        /**if (text == null) {
            return;
        }*/

        if (text != null){
            // Draws the bounding box around the TextBlock.
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            canvas.drawRect(rect, sRectPaint);

            // for each to draw line by line
            for (FirebaseVisionText.Line block_line : text.getLines()){



                float left = translateX(block_line.getBoundingBox().left);
                float bottom = translateY(block_line.getBoundingBox().bottom);
                canvas.drawText(block_line.getText(), left, bottom, sTextPaint);

            }

        }else if (line != null){

            RectF rect = new RectF(line.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            canvas.drawRect(rect, sRectPaint);


            float left = translateX(line.getBoundingBox().left);
            float bottom = translateY(line.getBoundingBox().bottom);
            canvas.drawText(line.getText(), left, bottom, sTextPaint);
        }



    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    public boolean contains(float x, float y) {
        FirebaseVisionText.TextBlock text = mText;
        FirebaseVisionText.Line line = mLine;
        if (text != null){
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);

        }
        else if(line != null){
            RectF rect = new RectF(line.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
        }else{
            return false;
        }
    }

}
