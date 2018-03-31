
package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Language;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Translate;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;


public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";
    public static boolean resetFlag = false;
    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String WordByWord = "WordByWord";
    public static final String LineByLine = "LineByLine";
    public static final String BlockByBlock = "BlockByBlock";
    public static final String Translation = "Translation";
    public static final String SelectedLanguage = "SelectedLanguage";
    public static final String TextBlockObject = "String";

    public boolean wordByWord;
    public boolean lineByLine;
    public boolean blockByBlock;
    public boolean translation;

    public String translateTo = "";

    private ClipboardManager myClipboard;
    private ClipData myClip;
    boolean isUp;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    EditText textHolder;
    com.github.clans.fab.FloatingActionButton copyButton;
    com.github.clans.fab.FloatingActionButton cutButton;
    com.github.clans.fab.FloatingActionButton translateButton;
    RelativeLayout bottomView;
    AVLoadingIndicatorView load;
    boolean isExpanded;
    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    ExpandCollapseExtention animator = new ExpandCollapseExtention();


    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ocr_capture);
        textHolder = findViewById(R.id.textHolder);
        copyButton = findViewById(R.id.copyButton);
        cutButton = findViewById(R.id.cutButton);
        translateButton = findViewById(R.id.translateButton);
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        bottomView = findViewById(R.id.bottomView);
        load = findViewById(R.id.loading);
        ExpandCollapseExtention.collapse(bottomView);
        isExpanded = false;




        cutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cutButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        copyToClipboard(textHolder.getText().toString());
                        textHolder.setText("");
                        ExpandCollapseExtention.collapse(bottomView);
                        isExpanded = false;
                    }
                });
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        copyToClipboard(textHolder.getText().toString());
                        //ExpandCollapseExtention.collapse(bottomView);
                    }
                });
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                translateButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        load.show();
                         class FetchTranslatedData extends AsyncTask<String, Integer, String> {

                            @Override
                            protected void onPreExecute() {
                                load.show();
                            }
                            protected String doInBackground(String... strings) {
                                String textToBeTranslated = strings[0];
                                String translatedText = "";
                                try {
                                    Thread.sleep(1000);
                                    while(translatedText==null || translatedText.equals(""))
                                    translatedText = Translate.execute(textToBeTranslated, Language.ENGLISH, Language.BANGLA);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return translatedText;
                            }

                            protected void onPostExecute(String result) {
                                //super.onPostExecute(result);
                                EditText textHolder = findViewById(R.id.textHolder);
                                textHolder.setText(result);
                                load.hide();

                            }
                        }
                        new FetchTranslatedData().execute(textHolder.getText().toString());
                        //copyToClipboard(textHolder.getText().toString());
                        //textHolder.setText();
                        //if (isExpanded) ExpandCollapseExtention.collapse(bottomView);
                        //isExpanded = false;
                    }
                });
            }
        });


        textHolder.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!isExpanded) ExpandCollapseExtention.expand(bottomView);
                isExpanded = true;
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
        wordByWord = getIntent().getBooleanExtra(WordByWord, false);
        lineByLine = getIntent().getBooleanExtra(LineByLine, false);
        blockByBlock = getIntent().getBooleanExtra(BlockByBlock, false);
        translation = getIntent().getBooleanExtra(Translation, false);

        translateTo = getIntent().getStringExtra(SelectedLanguage);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();


    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }


    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.

        //pass imageView
        View view = getWindow().getDecorView().getRootView();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        OcrDetectorProcessor ocrDetectorProcessor = new OcrDetectorProcessor(mGraphicOverlay, view, translation, wordByWord, lineByLine, blockByBlock, translateTo);
        textRecognizer.setProcessor(ocrDetectorProcessor);

        if (resetFlag) {
            ocrDetectorProcessor = new OcrDetectorProcessor(mGraphicOverlay, view, translation, wordByWord, lineByLine, blockByBlock, translateTo);
            textRecognizer.setProcessor(ocrDetectorProcessor);
            resetFlag = false;
        }

        if (!textRecognizer.isOperational()) {

            Log.w(TAG, "Detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }


        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    /**
     * Restarts the camera.
     */
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
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

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


    private boolean onTap(float rawX, float rawY) {


        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;

        if (graphic != null) {
            ExpandCollapseExtention.expand(bottomView);
            isExpanded = true;
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null && translation) {
                String textToBeTranslated = text.getValue();
                String translatedText = "";
                try {
                    translatedText = Translate.execute(textToBeTranslated, Language.ENGLISH, Language.BANGLA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                textHolder.setText(translatedText);
            } else if (text != null && text.getValue() != null) {
                textHolder.setText(text.getValue());
            } else {
                Log.d(TAG, "text data is null");
            }
        } else {
//            View view = getWindow().getDecorView().getRootView();
//            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
//            OcrDetectorProcessor ocrDetectorProcessor = new OcrDetectorProcessor(mGraphicOverlay, view, translation, wordByWord, lineByLine, blockByBlock, translateTo);
//            textRecognizer.setProcessor(ocrDetectorProcessor);
//            resetFlag = false;
            Log.d(TAG, "no text detected");

        }
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }


        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    public void copyToClipboard(String copyText) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copyText);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Your OTP", copyText);
            clipboard.setPrimaryClip(clip);
        }
        Toast toast = Toast.makeText(getApplicationContext(),
                "Text copied to clipboard", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.LEFT, 50, 50);
        toast.show();
    }


    public static void animateViewFromBottomToTop(final View view) {

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {

                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int TRANSLATION_Y = view.getHeight();
                view.setTranslationY(TRANSLATION_Y);
                view.setVisibility(View.GONE);
                view.animate()
                        .translationYBy(-TRANSLATION_Y)
                        .setDuration(500)
                        .setStartDelay(200)
                        .setListener(new AnimatorListenerAdapter() {

                            @Override
                            public void onAnimationStart(final Animator animation) {

                                view.setVisibility(View.VISIBLE);
                            }
                        })
                        .start();
            }
        });
    }
}
