
package com.google.android.gms.samples.vision.ocrreader;

import android.util.SparseArray;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    View view;
    public boolean translation;
    private boolean wordByWord;
    private boolean lineByLine;
    private boolean blockByBlock;
    private String translateTo;
    private static boolean frameDone = true;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, View view, boolean translation, boolean wordByWord, boolean lineByLine, boolean blockByBlock, String translateTo) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.view = view;
        this.translation = translation;
        this.wordByWord = wordByWord;
        this.lineByLine = lineByLine;
        this.blockByBlock = blockByBlock;
        this.translateTo = translateTo;
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        if (frameDone && !OcrCaptureActivity.isPaused) {
            OcrCaptureActivity.detections = detections;
            frameDone = false;
            SparseArray<TextBlock> items = detections.getDetectedItems();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item, translation, wordByWord, lineByLine, blockByBlock, translateTo);
                mGraphicOverlay.add(graphic);

                try {
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(300);
                    //anim.setStartOffset(20);
                    //anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(Animation.ABSOLUTE);
                    mGraphicOverlay.startAnimation(anim);
                } catch (Exception e) {
                    //OcrCaptureActivity.resetFlag = true;
                }
            }

        }
        frameDone = true;
    }


    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
