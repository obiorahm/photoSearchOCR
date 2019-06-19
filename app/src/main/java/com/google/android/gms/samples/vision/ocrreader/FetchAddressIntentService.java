package com.google.android.gms.samples.vision.ocrreader;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



/**
 * Created by mgo983 on 10/15/18.
 */

public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;

    public FetchAddressIntentService(){
        super("currentPlaceMap");

    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString( Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // ...

        if (intent == null){
            return;
        }

        String errorMessage = "";

        // Get the location to this service through an extra
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        List<Address> addresses = null;
        try{
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),
                    // In this sample, get just a single address
                    1);
        }catch (IOException e){
            Log.e("FetchAddressIntent", e + " ");
        }catch (IllegalArgumentException e){
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid latitude or longitude used";
            Log.e("FetchAddressIntent", e + " ");
        }

        //Handle case where no address was found
        if (addresses == null || addresses.size() == 0){
            if (errorMessage.isEmpty()){
                errorMessage = "No address is found";
                Log.e("FetchAddressIntent", errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragment = new ArrayList<String>();

            //Fetch the address lines using getAddressLine
            //Join them, and send them to the thread.

            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++){
                addressFragment.add(address.getAddressLine(i));
            }
            Log.i("FetchAddressIntent", "Address Found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragment));
        }
    }

}
