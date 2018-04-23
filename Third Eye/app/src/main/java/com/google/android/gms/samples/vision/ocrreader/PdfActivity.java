package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import me.rishabhkhanna.customtogglebutton.CustomToggleButton;

public class PdfActivity extends AppCompatActivity {
    private static final String LOG_TAG = "GeneratePDF";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private EditText editName;
    private EditText editText;
    public CustomToggleButton createButton;
    public CustomToggleButton resetButton;
    private String fileName;
    public String fileContent;
    public String value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        editName = findViewById(R.id.name_edit);
        editText = findViewById(R.id.text_edit);
        createButton = findViewById(R.id.create_button);
        resetButton = findViewById(R.id.reset_button);
        editText.setText(OcrCaptureActivity.pdfString);
        verifyStoragePermissions(this);
        createButton.setChecked(false);
        resetButton.setChecked(false);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileName = editName.getText().toString().trim();
                if(fileName.length() == 0)
                {
                    Random r = new java.util.Random ();
                    fileName = Long.toString (r.nextLong () & Long.MAX_VALUE, 36);
                }
                GeneratePDF(view, fileName);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editName.setText("");
                editText.setText("");
                OcrCaptureActivity.pdfString = "";
            }
        });
    }

    public void GeneratePDF(View view, String fName) {
        try {
            String fPath = Environment.getExternalStorageDirectory().getPath() + "/ThirdEye/PDF/" + fName + ".pdf";
            File file = new File(Environment.getExternalStorageDirectory(), fName + ".pdf");
            if (!file.exists()) {
                file.createNewFile();
                file.mkdir();
            }
            Log.d(LOG_TAG, "PDF Path: " + fPath);
            Font font = FontFactory.getFont("res/font/arialunicodems.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            FileOutputStream fOut = new FileOutputStream(file.getAbsoluteFile());
            Document document = new Document();
            fileContent = editText.getText().toString();
            Paragraph paragraph = new Paragraph(fileContent, font);
            PdfWriter.getInstance(document, fOut);
            document.open();
            document.add(paragraph);
            document.close();
            Toast.makeText(getApplicationContext(), fName + ".pdf created", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "File Not Found Exception", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();
        } catch (DocumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Document Exception", Toast.LENGTH_SHORT).show();
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
    }
}