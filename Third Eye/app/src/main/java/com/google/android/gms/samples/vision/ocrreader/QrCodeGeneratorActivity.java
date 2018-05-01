package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import java.util.Random;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;
import me.rishabhkhanna.customtogglebutton.CustomToggleButton;

public class QrCodeGeneratorActivity extends AppCompatActivity {

    private static final String TAG = "QRCodeGeneratorActivity";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public CustomToggleButton generate, saveButton;
    EditText editText;
    EditText editName;
    ImageView qrImage;
    String inputValue;
    String qrString = "";
    String savePath = Environment.getExternalStorageDirectory().getPath() + "/QRCode/";
    Bitmap bitmap;
    QRGEncoder qrgEncoder;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_generator);
        qrImage = findViewById(R.id.QR_Image);
        editName = findViewById(R.id.name_edit);
        editText = findViewById(R.id.text_edit);
        generate = findViewById(R.id.generate_button);
        saveButton = findViewById(R.id.save_button);
        qrString = getIntent().getStringExtra("QR STRING");
        editText.setText(qrString);
        verifyStoragePermissions(this);

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generate.setChecked(false);
                generateQR();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setChecked(false);
                saveQR();
            }
        });
    }

    public void generateQR() {
        inputValue = editText.getText().toString().trim();
        if (inputValue.length() > 0) {
            WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Display display = null;
            if (manager != null) {
                display = manager.getDefaultDisplay();
            }
            Point point = new Point();
            if (display != null) {
                display.getSize(point);
            }
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);
            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                Log.v(TAG, e.toString());
            }
        } else {
            editText.setError("Required");
        }
    }

    public void saveQR() {
        boolean save;
        String result;
        try {

            String fileName = editName.getText().toString().trim();
            if (fileName.length() == 0) {
                Random r = new java.util.Random();
                fileName = Long.toString(r.nextLong() & Long.MAX_VALUE, 36);
            }

            save = QRGSaver.save(savePath, fileName, bitmap, QRGContents.ImageType.IMAGE_JPEG);
            result = save ? "Image Saved" : "Image Not Saved";
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
