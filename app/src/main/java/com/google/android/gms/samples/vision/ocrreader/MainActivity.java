package com.google.android.gms.samples.vision.ocrreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {

    // Use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private CompoundButton wordByWord;
    private CompoundButton lineByline;
    private CompoundButton blockByBlock;
    private CompoundButton translation;

    private Spinner langSpinner;

    private TextView statusMessage;
    private TextView textValue;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = findViewById(R.id.status_message);
        textValue = findViewById(R.id.text_value);


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
    }

    CompoundButton.OnCheckedChangeListener langChoice = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                langSpinner.setVisibility(View.VISIBLE);
            } else {
                langSpinner.setVisibility(View.INVISIBLE);
            }
        }
    };

    CompoundButton.OnCheckedChangeListener changeChecker = new CompoundButton.OnCheckedChangeListener() {

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
            intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());
            intent.putExtra(OcrCaptureActivity.WordByWord, wordByWord.isChecked());
            intent.putExtra(OcrCaptureActivity.LineByLine, lineByline.isChecked());
            intent.putExtra(OcrCaptureActivity.BlockByBlock, blockByBlock.isChecked());
            intent.putExtra(OcrCaptureActivity.Translation, translation.isChecked());

            intent.putExtra(OcrCaptureActivity.SelectedLanguage, String.valueOf(langSpinner.getSelectedItem()));

            startActivityForResult(intent, RC_OCR_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == RC_OCR_CAPTURE) {
//            if (resultCode == CommonStatusCodes.SUCCESS) {
//                if (data != null) {
//                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
//                    statusMessage.setText(R.string.ocr_success);
//                    textValue.setText(text);
//                    Log.d(TAG, "Text read: " + text);
//                } else {
//                    statusMessage.setText(R.string.ocr_failure);
//                    Log.d(TAG, "No Text captured, intent data is null");
//                }
//            } else {
//                statusMessage.setText(String.format(getString(R.string.ocr_error),
//                        CommonStatusCodes.getStatusCodeString(resultCode)));
//            }
//        }
//        else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
    }
}