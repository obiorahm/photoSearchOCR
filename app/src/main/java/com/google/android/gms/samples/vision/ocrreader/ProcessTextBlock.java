package com.google.android.gms.samples.vision.ocrreader;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by mgo983 on 9/9/18.
 */

public class ProcessTextBlock {
    String textBlock;

    public ProcessTextBlock(String mTextBlock){
        textBlock = mTextBlock.toLowerCase();
    }

    private String replaceConjunctions(String text){

        return text.replaceAll(" on | or | with | and |\\&|\\.", ",");

    }

    private String removeSpecialCharacters(String text){
        return text.replaceAll("\\$|\\(.*?\\)|[0-9]", "");
    }


    public String processText(){
        String newText = textBlock;
        newText = removeSpecialCharacters(newText);
        newText = replaceConjunctions(newText);

        return newText; /*.split(",")*/
    }






}
