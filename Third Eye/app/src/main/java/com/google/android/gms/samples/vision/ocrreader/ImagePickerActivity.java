package com.google.android.gms.samples.vision.ocrreader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.mapzen.speakerbox.Speakerbox;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImagePickerActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    HashMap<String,String> mp = new HashMap<>();
    String language = "eng";
    Bitmap image;
    Button getTextButton, selectImageButton;
    Spinner selectLang;
    private TessBaseAPI mTess;
    String datapath = "";
    ImageView imv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        getTextButton = findViewById(R.id.get_text_button);
        selectImageButton = findViewById(R.id.select_button);
        image = null;
        selectLang = findViewById(R.id.lang_spinner);
        final Context context = this;



        mp.put("English", "eng");
        mp.put("Bangla", "ben");

        selectLang.setOnItemSelectedListener(this);



        List<String> languageList = new ArrayList<String>();
        languageList.add("English");
        languageList.add("Bangla");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        selectLang.setAdapter(dataAdapter);

        //initialize Tesseract API

        datapath = getFilesDir()+ "/tesseract/";
        mTess = new TessBaseAPI();



        checkFile(new File(datapath + "tessdata/"));

        mTess.init(datapath, language);


        imv = findViewById(R.id.imageView);

        getTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    processImage();
                    TextView OCRTextView = findViewById(R.id.textView);
                    Speakerbox speakerBox = new Speakerbox(getApplication());
                    speakerBox.play(OCRTextView.getText().toString());
                }
                catch (Exception e)
                {

                }
            }

        });


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // generate picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start((Activity) context);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                // Get cropped Image's coordinates
                Rect rect = result.getCropRect();
                // Get cropped Image's BITMAP

                Uri uri = result.getUri();
                imv.setImageURI(uri);
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver() , uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // coordinates printed in Log
//                processImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void processImage(){
        String OCRresult;
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = findViewById(R.id.textView);
        OCRTextView.setText(OCRresult);
    }

    private void checkFile(File dir) {
        if (!dir.exists()&& dir.mkdirs()){
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/"+language+".traineddata";
            File datafile = new File(datafilepath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = datapath + "/tessdata/"+language+".traineddata";
            AssetManager assetManager = getAssets();

            InputStream instream = assetManager.open("tessdata/"+language+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }


            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = parent.getItemAtPosition(position).toString();
        language = mp.get(item);
        checkFile(new File(datapath + "tessdata/"));
        mTess.init(datapath, language);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
