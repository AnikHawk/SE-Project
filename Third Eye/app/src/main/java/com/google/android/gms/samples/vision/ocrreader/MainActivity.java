package com.google.android.gms.samples.vision.ocrreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextRecognizer;
import com.nightonke.jellytogglebutton.JellyToggleButton;

import java.io.IOException;

import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.AutoFocus;
import static com.google.android.gms.samples.vision.ocrreader.OcrCaptureActivity.UseFlash;
import me.rishabhkhanna.customtogglebutton.CustomToggleButton;
public class MainActivity extends Activity implements View.OnClickListener {

    // Use a compound createButton so either checkbox or switch widgets work.
    private JellyToggleButton autoFocus;
    private JellyToggleButton useFlash;
    private JellyToggleButton wordByWord;
    private JellyToggleButton lineByline;
    private JellyToggleButton blockByBlock;
    private JellyToggleButton translation;
    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private Spinner langSpinner;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    public TextView statusMessage;
    //private TextView textValue;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreview = findViewById(R.id.preview);
        statusMessage = findViewById(R.id.app_name);
        autoFocus = findViewById(R.id.auto_focus);
        useFlash = findViewById(R.id.use_flash);
        wordByWord = findViewById(R.id.word_by_word);
        lineByline = findViewById(R.id.line_by_line);
        blockByBlock = findViewById(R.id.block_by_block);
        translation = findViewById(R.id.translation);

        wordByWord.setOnCheckedChangeListener(changeChecker);
        lineByline.setOnCheckedChangeListener(changeChecker);
        blockByBlock.setOnCheckedChangeListener(changeChecker);

        langSpinner = findViewById(R.id.lang_spinner);
        translation.setOnCheckedChangeListener(langChoice);

        findViewById(R.id.read_text).setOnClickListener(this);
        createCameraSource(true, false);
    }

    JellyToggleButton.OnCheckedChangeListener langChoice = new JellyToggleButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                langSpinner.setVisibility(View.VISIBLE);
            } else {
                langSpinner.setVisibility(View.INVISIBLE);
            }
        }
    };

    JellyToggleButton.OnCheckedChangeListener changeChecker = new JellyToggleButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView == wordByWord) {
                    lineByline.setChecked(false);
                    blockByBlock.setChecked(false);
                }
                if (buttonView == lineByline) {
                    wordByWord.setChecked(false);
                    blockByBlock.setChecked(false);
                }
                if (buttonView == blockByBlock) {
                    lineByline.setChecked(false);
                    wordByWord.setChecked(false);
                }
            }

            if (!wordByWord.isChecked() && !lineByline.isChecked() && !blockByBlock.isChecked()) {
                wordByWord.setChecked(true);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {
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

    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();
        View view = getWindow().getDecorView().getRootView();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(null)
                        .setFocusMode(null)
                        .build();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

    }

    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {}

    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}