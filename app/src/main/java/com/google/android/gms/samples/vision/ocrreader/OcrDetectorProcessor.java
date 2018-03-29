
package com.google.android.gms.samples.vision.ocrreader;

import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;


public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    View view;
    public boolean translation;
    public boolean wordByWord;
    public boolean lineByLine;
    public boolean blockByBlock;

    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay, View view, boolean translation, boolean wordByWord, boolean lineByLine, boolean blockByBlock) {
        mGraphicOverlay = ocrGraphicOverlay;
        this.view = view;
        this.translation = translation;
        this.wordByWord = wordByWord;
        this.lineByLine = lineByLine;
        this.blockByBlock = blockByBlock;
    }

    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        for (int i = 0; i < items.size(); ++i) {
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item, view, translation, wordByWord, lineByLine, blockByBlock);
            mGraphicOverlay.add(graphic);
        }
    }


    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
