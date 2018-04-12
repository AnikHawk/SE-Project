package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextRecognizer;
//com.suke.widget.JellyToggleButton;
//import com.suke.widget.JellyToggleButton;
import com.nightonke.jellytogglebutton.JellyToggleButton;
//import me.rishabhkhanna.customtogglebutton.CustomToggleButton;

import java.io.IOException;

import me.rishabhkhanna.customtogglebutton.CustomToggleButton;

import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.AutoFocus;
import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.UseFlash;
public class MainActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    // Use a compound createButton so either checkbox or switch widgets work.
    private JellyToggleButton autoFocus;
    private JellyToggleButton useFlash;
    private JellyToggleButton wordByWord;
    private JellyToggleButton lineByline;
    private JellyToggleButton blockByBlock;
    private JellyToggleButton translation;
    private CameraSource mCameraSource;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Spinner langSpinner;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    private static final int RC_HANDLE_CAMERA_PERM = 212;
    private CustomToggleButton detectText;
    private CustomToggleButton imagePickerButton;
    private CustomToggleButton qrButton;
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    public TextView statusMessage;
    //private TextView textValue;
    private static final int RC_OCR_CAPTURE = 9003;
    private static final int RC_IMAGE_PICKER = 9004;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }

        surfaceView = (SurfaceView)findViewById(R.id.preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        statusMessage = findViewById(R.id.app_name);
        autoFocus = findViewById(R.id.auto_focus);
        useFlash = findViewById(R.id.use_flash);
        wordByWord = findViewById(R.id.word_by_word);
        lineByline = findViewById(R.id.line_by_line);
        blockByBlock = findViewById(R.id.block_by_block);
        translation = findViewById(R.id.translation);
        detectText = findViewById(R.id.read_text);
        imagePickerButton = findViewById(R.id.picker_button);
        qrButton = findViewById(R.id.qr_button);

        detectText.setChecked(false);
        imagePickerButton.setChecked(false);
        qrButton.setChecked(false);

        imagePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ImagePickerActivity.class));
            }
        });

        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QrCodeScannerActivity.class));
            }
        });

        wordByWord.setOnClickListener(new JellyToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                    wordByWord.setChecked(true);
                    lineByline.setChecked(false);
                    blockByBlock.setChecked(false);
            }
        });

        lineByline.setOnClickListener(new JellyToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                    wordByWord.setChecked(false);
                    lineByline.setChecked(true);
                    blockByBlock.setChecked(false);
            }
        });
        blockByBlock.setOnClickListener(new JellyToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                    wordByWord.setChecked(false);
                    lineByline.setChecked(false);
                    blockByBlock.setChecked(true);
            }
        });

        langSpinner = findViewById(R.id.lang_spinner);
        translation.setOnCheckedChangeListener(langChoice);


        findViewById(R.id.read_text).setOnClickListener(this);
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
        if (v.getId() == detectText.getId()) {
            // launch Ocr capture activity.
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(AutoFocus, autoFocus.isChecked());
            intent.putExtra(UseFlash, useFlash.isChecked());
            intent.putExtra(OcrCaptureActivity.WordByWord, wordByWord.isChecked());
            intent.putExtra(OcrCaptureActivity.LineByLine, lineByline.isChecked());
            intent.putExtra(OcrCaptureActivity.BlockByBlock, blockByBlock.isChecked());
            intent.putExtra(OcrCaptureActivity.Translation, translation.isChecked());

            intent.putExtra(OcrCaptureActivity.SelectedLanguage, String.valueOf(langSpinner.getSelectedItem()));

            startActivityForResult(intent, RC_OCR_CAPTURE);
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
}
