package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.nightonke.jellytogglebutton.JellyToggleButton;
import me.rishabhkhanna.customtogglebutton.CustomToggleButton;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = "MainActivity";

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private static final int RC_HANDLE_CAMERA_PERM = 212;

    private CustomToggleButton realTimeButton;
    private CustomToggleButton imagePickerButton;
    private CustomToggleButton qrButton;
    private JellyToggleButton translation;
    private Spinner langSpinner;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    //LayoutInflater controlInflater = null;
    public TextView statusMessage;

    private static final int RC_OCR_CAPTURE = 9003;
    //private static final int RC_IMAGE_PICKER = 9004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

        surfaceView = findViewById(R.id.preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        statusMessage = findViewById(R.id.app_name);
        translation = findViewById(R.id.translation);
        realTimeButton = findViewById(R.id.real_time);
        imagePickerButton = findViewById(R.id.image_picker_button);
        qrButton = findViewById(R.id.qr_button);

        realTimeButton.setChecked(false);
        imagePickerButton.setChecked(false);
        qrButton.setChecked(false);

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QrCodeScannerActivity.class));
            }
        });
        imagePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImagePickerActivity.class));
            }
        });

        langSpinner = findViewById(R.id.lang_spinner);
        translation.setOnCheckedChangeListener(langChoice);

        findViewById(R.id.real_time).setOnClickListener(this);
    }

    JellyToggleButton.OnCheckedChangeListener langChoice = new JellyToggleButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (isChecked) {
                langSpinner.setVisibility(View.VISIBLE);
            } else {
                langSpinner.setVisibility(View.INVISIBLE);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (v.getId() == realTimeButton.getId()) {
            realTimeButton.setChecked(true);
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra("Translation", translation.isChecked());
            intent.putExtra("SelectedLanguage", String.valueOf(langSpinner.getSelectedItem()));
            startActivityForResult(intent, RC_OCR_CAPTURE);
            realTimeButton.setChecked(false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {}

    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        while (!isCameraPermissionGranted()){}
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    boolean isCameraPermissionGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
                return false;
            }
            else return true;
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
