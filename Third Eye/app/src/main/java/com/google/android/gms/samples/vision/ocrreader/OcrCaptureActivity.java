
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Detect;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Language;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Translate;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.cketti.shareintentbuilder.ShareIntentBuilder;
import mazouri.statebutton.StateButton;

class BoxedText {

    int top;
    int left;
    String string;

    BoxedText(int t, int l, String s)
    {
        top = t;
        left = l;
        string = s;
    }
}

public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";
    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    public static String pdfString = "";
    public static String qrString = "";

    StateButton flashButton;
    StateButton focusButton;
    StateButton scanModeButton;
    public boolean wordByWord = true;
    public boolean lineByLine = false;
    public boolean blockByBlock = false;
    public boolean translation;
    public String translateTo = "";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    RelativeLayout bottomView;
    public EditText textHolder;
    boolean isExpanded;

    FloatingActionButton copyButton;
    FloatingActionButton cutButton;
    FloatingActionButton translateButton;
    FloatingActionButton pdfButton;
    FloatingActionButton qrGenButton;
    FloatingActionButton shareButton;
    FloatingActionButton pauseButton;
    FloatingActionMenu fab;
    AVLoadingIndicatorView load;
    AVLoadingIndicatorView effect;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    ExpandCollapseExtention animator = new ExpandCollapseExtention();

    private Language fromLang;
    private Language toLang;
    boolean frontFacing = false;
    public static Detector.Detections<TextBlock> detections;
    public static boolean isPaused = false;

    boolean autoFocus = true;
    boolean useFlash = false;
    int scanModeSelection = 1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_ocr_capture);
        flashButton = findViewById(R.id.flashButton);
        focusButton = findViewById(R.id.focusButton);
        scanModeButton = findViewById(R.id.scanModeButton);

        textHolder = findViewById(R.id.textHolder);
        mPreview = findViewById(R.id.preview);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        bottomView = findViewById(R.id.bottomView);
        ExpandCollapseExtention.collapse(bottomView);
        isExpanded = false;

        copyButton = findViewById(R.id.copyButton);
        cutButton = findViewById(R.id.cutButton);
        translateButton = findViewById(R.id.translateButton);
        pdfButton = findViewById(R.id.pdf_button);
        qrGenButton = findViewById(R.id.qr_generator_fab_button);
        shareButton = findViewById(R.id.share_button);
        pauseButton = findViewById(R.id.pause_button);
        load = findViewById(R.id.loading);
        fab = findViewById(R.id.fab);
        effect = findViewById(R.id.effect);
        effect.bringToFront();

        // read parameters from the intent used to launch the activity.
        translation = getIntent().getBooleanExtra("Translation", false);
        translateTo = getIntent().getStringExtra("SelectedLanguage");
        flashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String flashMode = mCameraSource.getFlashMode();
                if (flashMode != null) {
                    if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                        mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        flashButton.setState(StateButton.BUTTON_STATES.ENABLED);
                        useFlash = true;
                        Toast.makeText(getApplicationContext(), "Flash On", Toast.LENGTH_SHORT).show();
                    } else {
                        mCameraSource.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        flashButton.setState(StateButton.BUTTON_STATES.SELECTED);
                        useFlash = false;
                        Toast.makeText(getApplicationContext(), "Flash Off", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        focusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StateButton.BUTTON_STATES state = focusButton.getState();
                if (state == StateButton.BUTTON_STATES.SELECTED) {
                    mCameraSource.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    focusButton.setState(StateButton.BUTTON_STATES.ENABLED);
                    autoFocus = true;
                    Toast.makeText(getApplicationContext(), "Auto Focus On", Toast.LENGTH_SHORT).show();
                } else {
                    mCameraSource.cancelAutoFocus();
                    focusButton.setState(StateButton.BUTTON_STATES.SELECTED);
                    autoFocus = false;
                    Toast.makeText(getApplicationContext(), "Auto Focus Off", Toast.LENGTH_SHORT).show();
                }
            }
        });
        scanModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StateButton.BUTTON_STATES state = scanModeButton.getState();
                if (state == StateButton.BUTTON_STATES.SELECTED) {
                    registerForContextMenu(view);
                    openContextMenu(view);
                    scanModeButton.setState(StateButton.BUTTON_STATES.ENABLED);
                } else {
                    registerForContextMenu(view);
                    openContextMenu(view);
                    scanModeButton.setState(StateButton.BUTTON_STATES.SELECTED);
                }
            }
        });
//        facingButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                StateButton.BUTTON_STATES state = facingButton.getState();
//                if (state == StateButton.BUTTON_STATES.SELECTED) {
//                    frontFacing = true;
//                    facingButton.setState(StateButton.BUTTON_STATES.ENABLED);
//                } else {
//                    frontFacing = false;
//                    facingButton.setState(StateButton.BUTTON_STATES.SELECTED);
//                }
//                //createCameraSource(autoFocus[0],useFlash[0]);
//
//            }
//        });
        cutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyToClipboard(textHolder.getText().toString());
                textHolder.setText("");
                ExpandCollapseExtention.collapse(bottomView);
                isExpanded = false;
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                copyToClipboard(textHolder.getText().toString());
                //ExpandCollapseExtention.collapse(bottomView);
            }
        });
        translateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                load.show();
                @SuppressLint("StaticFieldLeak")
                class FetchTranslatedData extends AsyncTask<String, Integer, String> {
                    @Override
                    protected void onPreExecute() {
                        load.show();
                    }

                    protected String doInBackground(String... strings) {
                        String textToBeTranslated = strings[0];
                        String translatedText = "";
                        try {
                            fromLang = Detect.execute(textToBeTranslated);
                            toLang = Language.fromString(getTranslationLanguage());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                            while (translatedText.equals(""))
                                translatedText = Translate.execute(textToBeTranslated, fromLang, toLang);
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
            }
        });
        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfString +=("\n" + textHolder.getText().toString());
                Intent intent = new Intent(OcrCaptureActivity.this, PdfActivity.class);
                OcrCaptureActivity.this.startActivity(intent);
            }
        });
        qrGenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                qrString = textHolder.getText().toString();
                Intent intent = new Intent(OcrCaptureActivity.this, QrCodeGeneratorActivity.class);
                intent.putExtras(bundle);
                intent.putExtra("QR STRING", qrString);
                OcrCaptureActivity.this.startActivity(intent);
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = ShareIntentBuilder.from(OcrCaptureActivity.this)
                        .text(textHolder.getText().toString())
                        .build();
                OcrCaptureActivity.this.startActivity(shareIntent);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPaused) {
                    isPaused = false;
                    ExpandCollapseExtention.collapse(bottomView);
                }
                else {

                    List textList = new ArrayList<BoxedText>();
                    textHolder.setText("");
                    SparseArray<TextBlock> items = detections.getDetectedItems();
                    for (int i = 0; i < items.size(); ++i) {
                        TextBlock item = items.valueAt(i);
                        int itemTop = item.getBoundingBox().top;
                        int itemLeft = item.getBoundingBox().left;

                        textList.add(new BoxedText(itemTop,itemLeft, item.getValue()));
//
                    }
                    Collections.sort(textList, new Comparator<BoxedText>() {
                        @Override
                        public int compare(BoxedText textOne, BoxedText textTwo) {
                            if(textOne.top > textTwo.top)
                                return 1;
                           else if(textOne.top < textTwo.top)
                                return -1;
                            else {
                                if(textOne.left > textTwo.left)
                                    return 1;
                                else if(textOne.left < textTwo.left)
                                    return -1;
                                return 0;
                            }

                        }

                    });

                    for (int i = 0; i < textList.size(); ++i) {
                        BoxedText item = (BoxedText) textList.get(i);
                        textHolder.append(item.string);
                        textHolder.append(" \n");
                    }


                    if (!textHolder.getText().toString().equals("")) {
                        textHolder.setVisibility(View.VISIBLE);
                        ExpandCollapseExtention.Fullexpand(bottomView);
                    }
                    isPaused = true;
                }
            }
        });
//        textHolder.setOnTouchListener(new View.OnTouchListener() {
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (!isExpanded)
//                    ExpandCollapseExtention.expand(bottomView);
//                isExpanded = true;
//                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                return false;
//            }
//        });
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

    private String getTranslationLanguage() {
        if (translateTo.equalsIgnoreCase("Bangla"))
            return "bn";
        else if (translateTo.equalsIgnoreCase("English"))
            return "en";
        else if (translateTo.equalsIgnoreCase("French"))
            return "fr";
        else if (translateTo.equalsIgnoreCase("German"))
            return "de";
        else if (translateTo.equalsIgnoreCase("Italian"))
            return "it";
        else if (translateTo.equalsIgnoreCase("Spanish"))
            return "es";
        else if (translateTo.equalsIgnoreCase("Russian"))
            return "ru";
        else
            return null;
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
        View view = getWindow().getDecorView().getRootView();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        OcrDetectorProcessor ocrDetectorProcessor = new OcrDetectorProcessor(mGraphicOverlay,
                view, translation, wordByWord, lineByLine, blockByBlock, translateTo);
        textRecognizer.setProcessor(ocrDetectorProcessor);
        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;
            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(frontFacing ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK)
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
            boolean autoFocus = getIntent().getBooleanExtra("AutoFocus", false);
            boolean useFlash = getIntent().getBooleanExtra("UseFlash", false);
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
                Log.e(TAG, "Unable to generate camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private boolean onTap(float rawX, float rawY) {
        if (fab.isOpened()) {
            textHolder.setVisibility(View.VISIBLE);
            ExpandCollapseExtention.expand(bottomView);
        }
        if (!fab.isOpened()) {
            ExpandCollapseExtention.collapse(bottomView);
        }
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            ExpandCollapseExtention.expand(bottomView);
            isExpanded = true;
            textHolder.setVisibility(View.VISIBLE);
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null && translation) {
                String textToBeTranslated = text.getValue();
                String translatedText = "";
                try {
                    fromLang = Detect.execute(textToBeTranslated);
                    toLang = Language.fromString(getTranslationLanguage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (toLang != null) {
                        translatedText = Translate.execute(textToBeTranslated, fromLang, toLang);
                    }
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

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(contextMenu, view, menuInfo);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_scan_mode_menu, contextMenu);
        MenuItem word = contextMenu.findItem(R.id.word);
        MenuItem line = contextMenu.findItem(R.id.line);
        MenuItem block = contextMenu.findItem(R.id.block);
        if (scanModeSelection == 1) {
            word.setChecked(true);
        } else if (scanModeSelection == 2) {
            line.setChecked(true);
        } else if (scanModeSelection == 3) {
            block.setChecked(true);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.word:
                Toast.makeText(getApplicationContext(), "Word Scan Selected", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                scanModeSelection = 1;
                wordByWord = true;
                lineByLine = false;
                blockByBlock = false;
                return true;
            case R.id.line:
                Toast.makeText(getApplicationContext(), "Line Scan Selected", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                scanModeSelection = 2;
                wordByWord = false;
                lineByLine = true;
                blockByBlock = false;
                return true;
            case R.id.block:
                Toast.makeText(getApplicationContext(), "Block Scan Selected", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                scanModeSelection = 3;
                wordByWord = false;
                lineByLine = false;
                blockByBlock = true;
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_language_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.none:
                Toast.makeText(getApplicationContext(), "Translation Off", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "";
                translation = false;
                return true;
            case R.id.bn:
                Toast.makeText(getApplicationContext(), "Translate to Bangla", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "Bangla";
                translation = true;
                return true;
            case R.id.fr:
                Toast.makeText(getApplicationContext(), "Translate to French", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "French";
                translation = true;
                return true;
            case R.id.de:
                Toast.makeText(getApplicationContext(), "Translate to German", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "German";
                translation = true;
                return true;
            case R.id.it:
                Toast.makeText(getApplicationContext(), "Translate to Italian", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "Italian";
                translation = true;
                return true;
            case R.id.es:
                Toast.makeText(getApplicationContext(), "Translate to Spanish", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "Spanish";
                translation = true;
                return true;
            case R.id.ru:
                Toast.makeText(getApplicationContext(), "Translate to Russian", Toast.LENGTH_LONG).show();
                item.setChecked(true);
                translateTo = "Russian";
                translation = true;
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OcrCaptureActivity.pdfString = "";
        this.finish();
    }
}

