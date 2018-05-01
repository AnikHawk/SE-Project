package com.google.android.gms.samples.vision.ocrreader;

//import android.app.ProgressDialog;

import android.os.AsyncTask;

import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Language;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Translate;

//import android.widget.EditText;

//import java.net.URL;

/**
 * Created by root on 3/31/18.
 */
public class FetchData extends AsyncTask<String, Integer, String> {

    @Override
    protected void onPreExecute() {

    }

    protected String doInBackground(String... strings) {
        String textToBeTranslated = strings[0];
        String translatedText = "";
        try {
            translatedText = Translate.execute(textToBeTranslated, Language.ENGLISH, Language.BANGLA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return translatedText;
    }


    protected void onPostExecute(String result) {
        //super.onPostExecute(result);
        //EditText textHolder = findViewById(R.id.textHolder);
    }
}