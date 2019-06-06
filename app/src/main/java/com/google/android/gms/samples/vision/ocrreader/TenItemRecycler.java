package com.google.android.gms.samples.vision.ocrreader;

import android.support.v7.widget.RecyclerView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.Adapter.FoodItemAdapter;

public class TenItemRecycler {

    public RecyclerView mRecyclerViewWholeMealView;
    public ImageButton mImageButtonCancelEdamamSearch;
    public ProgressBar mProgressBarSearchingEdamam;
    public TextView mTextViewNoResult;

    public TenItemRecycler(FoodItemAdapter.ViewHolder holder){
        mRecyclerViewWholeMealView = holder.mRecyclerViewWholeMealView;
        mImageButtonCancelEdamamSearch = holder.mImageButtonCancelEdamamSearch;
        mProgressBarSearchingEdamam = holder.mProgressBarSearchingEdamam;
        mTextViewNoResult = holder.mTextViewNoResult;
    }


    public TenItemRecycler(RecyclerView recyclerView, ImageButton imageButton, ProgressBar progressBar, TextView textView){
        mRecyclerViewWholeMealView = recyclerView;
        mImageButtonCancelEdamamSearch = imageButton;
        mProgressBarSearchingEdamam = progressBar;
        mTextViewNoResult = textView;
    }
}
