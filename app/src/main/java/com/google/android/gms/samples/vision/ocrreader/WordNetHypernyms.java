package com.google.android.gms.samples.vision.ocrreader;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;

/**
 * Created by mgo983 on 1/28/19.
 */

public class WordNetHypernyms {

    HashSet<String> mHypernyms = new HashSet<String>();

    static final String WNDICT = "dict";
    static final String DOCUMENT = "document";
    static final File DocumentDir = new File(Environment.getExternalStoragePublicDirectory(DOCUMENT), WNDICT);
    static final String LOG_TAG = WordNetHypernyms.class.getSimpleName();


    public static final String[] DRINK_HYPERNYMS = {
      "beverage",
            "drink",
            "drinkable",
            "brew",
            "beverage",
            "liquid",
            "espresso",
            "coffee"
    };

    public  boolean isHypernym(String [] hypernyms, String sentence) {

        boolean booleanHypernym = false;
        mHypernyms.clear();
        //add measurement hypernyms
        for (String item : hypernyms) {
            mHypernyms.add(item);
        }


        try{
            String minus_special_characters = sentence.replaceAll("[0-9/]", "");

            //to lower case
            String to_lower_case = minus_special_characters.toLowerCase();

            // tokenize the string
            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
            String[] tokens = tokenizer.tokenize(to_lower_case);


            Log.d(LOG_TAG + " nlp", minus_special_characters);
            String x = tokens.length > 0 ? tokens[0] : "no string ";
            Log.d(LOG_TAG, "measurement tokens " + x);

            //get wordNet dictionary
            String path = DocumentDir.getAbsolutePath();

            URL url = new URL("file", null, path);

            // construct the dictionary object and open it
            IDictionary dict = new Dictionary(url);


            boolean dictIsOpen = dict.open();
            Log.d(LOG_TAG, " " + dictIsOpen);

            String finalWords = "";
            for (String item : tokens) {

                if (!(item == null)) {


                    // look up the stem of the word
                    WordnetStemmer wordnetStemmer = new WordnetStemmer(dict);
                    List<String> stem_list = wordnetStemmer.findStems(item, POS.NOUN);
                    for (String possible_stem : stem_list) {
                        Log.d("JWI stem", item + ": " + possible_stem);
                    }

                    // collect the first stem, it is most likely to be the stem we are looking for
                    String stem_item = stem_list.size() == 0 ? item : stem_list.get(0);
                    if (mHypernyms.contains(stem_item)){
                        booleanHypernym = true;
                        return booleanHypernym;
                    }

                }
            }

        }catch (MalformedURLException e){

        }catch (IOException e){

        }
        return booleanHypernym;

    }


    public boolean getHypernym(String[] hypernyms, String sentence) {
        boolean containsHypernym = false;
        mHypernyms.clear();

        try {

            //add measurement hypernyms
            for (String item : hypernyms) {
                mHypernyms.add(item);
            }


            //first do some natural language processing
            // remove all special characters and numbers
            String minus_special_characters = sentence.replaceAll("[0-9/]", "");

            //to lower case
            String to_lower_case = minus_special_characters.toLowerCase();

            // tokenize the string
            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
            String[] tokens = tokenizer.tokenize(to_lower_case);


            Log.d(LOG_TAG + " nlp", minus_special_characters);
            String x = tokens.length > 0 ? tokens[0] : "no string ";
            Log.d(LOG_TAG, "measurement tokens " + x);

            //get wordNet dictionary
            String path = DocumentDir.getAbsolutePath();

            URL url = new URL("file", null, path);

            // construct the dictionary object and open it
            IDictionary dict = new Dictionary(url);


            boolean dictIsOpen = dict.open();
            Log.d(LOG_TAG, " " + dictIsOpen);

            String finalWords = "";
            for (String item : tokens) {

                if (!(item == null)) {



                    // look up the stem of the word
                    WordnetStemmer wordnetStemmer = new WordnetStemmer(dict);
                    List<String> stem_list = wordnetStemmer.findStems(item, POS.NOUN);
                    for (String possible_stem : stem_list){
                        Log.d("JWI stem", item +": " + possible_stem );
                    }

                    // collect the first stem, it is most likely to be the stem we are looking for
                    String stem_item = stem_list.size() == 0 ? item : stem_list.get(0);

                    // look up first sense of the word item
                    IIndexWord idxWord = dict.getIndexWord(stem_item, POS.NOUN);

                    if (idxWord != null) {
                        IWordID wordID = idxWord.getWordIDs().get(0);
                        IWord word = dict.getWord(wordID);

                        ISynset synset = word.getSynset();

                        // get the hypernyms
                        List<ISynsetID> curr_hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

                        List<IWord> words;
                        for (ISynsetID sid : curr_hypernyms) {
                            words = dict.getSynset(sid).getWords();

                            for (Iterator<IWord> i = words.iterator(); i.hasNext(); ) {
                                String aHypernym = i.next().getLemma();
                                if (mHypernyms.contains(aHypernym))
                                    containsHypernym = true;

                                Log.d("JWI " + item, aHypernym);
                            }
                            //Log.d ("","}");
                        }
                        /*if (!containsHypernym && !(curr_hypernyms == null)) {
                            finalWords += item + " ";
                        }*/

                    }


                }


            }

            return containsHypernym;


        }  catch (MalformedURLException e) {
            Log.e(LOG_TAG, e + "");

        } catch (IOException e) {
            Log.e(LOG_TAG, e + "");
        }
        return containsHypernym;

    }
}
