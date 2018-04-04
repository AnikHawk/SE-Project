package com.example.samiul_siddiqui.mainpackage;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.samiul_siddiqui.yandex.R;
import com.example.samiul_siddiqui.yandex.yandexpackage.Detect;
import com.example.samiul_siddiqui.yandex.yandexpackage.Language;
import com.example.samiul_siddiqui.yandex.yandexpackage.Translate;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        textView = findViewById(R.id.textview);
        textView.setText(R.string.samiul);

        Translate.setKey("trnsl.1.1.20180324T123828Z.2b9ffa716760ad4e.d9995e9e7e47f773cdc06f3c7ad25139455efe1d");


        String translatedText = "";
        String mytext = "Hola, amigo!";

        try {

            translatedText = Translate.execute(mytext, Detect.execute(mytext), Objects.requireNonNull(Language.fromString("bn")));
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            textView.setText(translatedText + " from " + Detect.execute(mytext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
