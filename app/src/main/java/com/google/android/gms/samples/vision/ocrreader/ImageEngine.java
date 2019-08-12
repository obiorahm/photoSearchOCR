package com.google.android.gms.samples.vision.ocrreader;

import android.net.Uri;

public interface ImageEngine {

    public Uri buildUrl (String queryParameter);
    public Uri handleJSON(String JSONData);
    public void nextEngine(String searchString);

}
