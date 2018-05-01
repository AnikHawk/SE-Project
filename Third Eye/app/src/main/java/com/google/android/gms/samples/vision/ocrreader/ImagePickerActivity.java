package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Language;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Translate;
import com.googlecode.tesseract.android.TessBaseAPI;
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
import java.util.Locale;

import de.cketti.shareintentbuilder.ShareIntentBuilder;
import me.rishabhkhanna.customtogglebutton.CustomToggleButton;

public class ImagePickerActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static String pdfString = "";
    public static String qrString = "";
    FloatingActionButton copyButton;
    FloatingActionButton cutButton;
    FloatingActionButton pdfButton;
    FloatingActionButton qrGenButton;
    FloatingActionButton translateButton;
    FloatingActionButton shareButton;
    FloatingActionButton speakButton;
    FloatingActionMenu fab;
    CustomToggleButton getTextButton, selectImageButton;
    HashMap<String, String> mp = new HashMap<>();
    String language = "eng";
    Bitmap image;
    Spinner selectLang;
    String dataPath = "";
    ImageView imv;
    TextToSpeech textToSpeech;
    EditText textHolder;
    boolean isSpeaking = false;
    private TessBaseAPI tesseractBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        getTextButton = findViewById(R.id.get_text_button);
        selectImageButton = findViewById(R.id.select_button);
        copyButton = findViewById(R.id.copyButton);
        cutButton = findViewById(R.id.cutButton);
        pdfButton = findViewById(R.id.pdf_button);
        qrGenButton = findViewById(R.id.qr_generator_fab_button);
        translateButton = findViewById(R.id.translateButton);
        shareButton = findViewById(R.id.share_button);
        speakButton = findViewById(R.id.speakButton);
        textHolder = findViewById(R.id.textHolder);

        getTextButton.setChecked(false);
        selectImageButton.setChecked(false);
        image = null;
        selectLang = findViewById(R.id.lang_spinner);
        final Context context = this;

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        mp.put("English", "eng");
        mp.put("Bangla", "ben");

        final HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");

        selectLang.setOnItemSelectedListener(this);

        List<String> languageList = new ArrayList<>();
        languageList.add("English");
        languageList.add("Bangla");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, languageList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        selectLang.setAdapter(dataAdapter);

        //initialize Tesseract API

        dataPath = getFilesDir() + "/tesseract/";
        tesseractBase = new TessBaseAPI();
        checkFile(new File(dataPath + "tessdata/"));
        tesseractBase.init(dataPath, language);
        imv = findViewById(R.id.imageView);

        cutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyToClipboard(textHolder.getText().toString());
                textHolder.setText("");
            }
        });
        speakButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isSpeaking) {
                    textToSpeech.stop();
                    isSpeaking = false;
                } else {
                    isSpeaking = true;
                    String toSpeak = textHolder.getText().toString();
                    textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, map);
                }
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyToClipboard(textHolder.getText().toString());
            }
        });
        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OcrCaptureActivity.pdfString += ("\n" + textHolder.getText().toString());
                Intent intent = new Intent(ImagePickerActivity.this, PdfActivity.class);
                ImagePickerActivity.this.startActivity(intent);
            }
        });
        qrGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                qrString = textHolder.getText().toString();
                Intent intent = new Intent(ImagePickerActivity.this, QrCodeGeneratorActivity.class);
                intent.putExtras(bundle);
                intent.putExtra("QR STRING", qrString);
                ImagePickerActivity.this.startActivity(intent);
            }
        });
        translateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (language.equals("ben")) {
                    return;
                }
                String textToBeTranslated = textHolder.getText().toString();
                String translatedText = "";

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                Translate.setKey("trnsl.1.1.20180412T163251Z.ead1113e422fc75c.2aea1968e61b5c8585b850b8a8b056c9eb77b74c");
                try {
                    translatedText = Translate.execute(textToBeTranslated, Language.ENGLISH, Language.BANGLA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                textHolder.setText(translatedText);
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = ShareIntentBuilder.from(ImagePickerActivity.this)
                        .text(textHolder.getText().toString())
                        .build();
                ImagePickerActivity.this.startActivity(shareIntent);
            }
        });

        getTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    processImage();
                } catch (Exception e) {
                    e.printStackTrace();
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
                //Rect rect = result.getCropRect();
                // Get cropped Image's BITMAP

                Uri uri = result.getUri();
                imv.setImageURI(uri);
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // coordinates printed in Log
//                processImage();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error;
                error = result.getError();
                error.printStackTrace();
            }
        }
    }

    public void processImage() {
        String ocrResult;
        tesseractBase.setImage(image);
        ocrResult = tesseractBase.getUTF8Text();
        textHolder.setText(ocrResult);
    }


    private void checkFile(File dir) {
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        if (dir.exists()) {
            String dataFilePath = dataPath + "/tessdata/" + language + ".traineddata";
            File datafile = new File(dataFilePath);

            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            String filepath = dataPath + "/tessdata/" + language + ".traineddata";
            AssetManager assetManager = getAssets();

            InputStream inStream = assetManager.open("tessdata/" + language + ".traineddata");
            OutputStream outStream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, read);
            }


            outStream.flush();
            outStream.close();
            inStream.close();

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
        checkFile(new File(dataPath + "tessdata/"));
        tesseractBase.init(dataPath, language);
        if (language.equals("eng")) {
            textToSpeech.setLanguage(Locale.US);
        } else if (language.equals("ben")) {
            textToSpeech.setLanguage(new Locale("bn_BD"));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void copyToClipboard(String copyText) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setText(copyText);
            }
        } else {
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData
                    .newPlainText("Your OTP", copyText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }
        Toast toast = Toast.makeText(getApplicationContext(),
                "Text copied to clipboard", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.START, 50, 50);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        textToSpeech.stop();
        OcrCaptureActivity.pdfString = "";
        this.finish();
    }


}
