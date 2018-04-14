
package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.StrictMode;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Detect;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Language;
import com.google.android.gms.samples.vision.ocrreader.yandexpackage.Translate;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;


public class OcrGraphic extends GraphicOverlay.Graphic{

    private int mId;
    private static final int TEXT_COLOR = Color.WHITE;

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private final TextBlock mText;
    public GraphicOverlay transitionOverlay;
    public boolean translation;
    private boolean wordByWord;
    private boolean lineByLine;
    private boolean blockByBlock;
    private Language fromLang;
    private Language toLang;
    private String translateTo;

    OcrGraphic(GraphicOverlay overlay, TextBlock text, Boolean translation, Boolean wordByWord, Boolean lineByLine, Boolean blockByBlock, String translateTo) {
        super(overlay);
        transitionOverlay = overlay;
        mText = text;
        this.translation = translation;
        this.wordByWord = wordByWord;
        this.lineByLine = lineByLine;
        this.blockByBlock = blockByBlock;
        this.translateTo = translateTo;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(Color.GRAY);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(30.0f);
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate();
    }



    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }


    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
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

    @Override
    public void draw(Canvas canvas) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Translate.setKey("trnsl.1.1.20180324T013441Z.88494867aca5ce90.dc6e57b0858ef86256875388ec3923eae06ca17c");

        TextBlock text = mText;
        if (text == null) {
            return;
        }

//  Word by Word
        else if (wordByWord) {
            if(translation) try {
                fromLang = Detect.execute(text.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);

            canvas.drawRect(rect, sRectPaint);
            List<? extends Text> lineComponents = text.getComponents();
            for (Text currentLine : lineComponents) {
                List<? extends Text> textComponents = currentLine.getComponents();
                for (Text currentText : textComponents) {
                    float left = translateX(currentText.getBoundingBox().left);
                    float bottom = translateY(currentText.getBoundingBox().bottom);
                    // Draws the bounding box around the TextBlock.
                    rect = new RectF(currentText.getBoundingBox());
                    rect.left = translateX(rect.left);
                    rect.top = translateY(rect.top);
                    rect.right = translateX(rect.right);
                    rect.bottom = translateY(rect.bottom);
                    //canvas.drawRect(rect, sRectPaint);

                    if(translation) {
                        String textToBeTranslated = currentText.getValue();
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

                        setTextSizeForWidth(sTextPaint, rect.width(), translatedText);
                        canvas.drawText(translatedText, left, bottom, sTextPaint);
                    }
                    else {
                        setTextSizeForWidth(sTextPaint, rect.width(), currentText.getValue());
                        canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
                    }
                }
            }

        }


//        Line by Line
        else if (lineByLine) {
            if(translation) try {
                fromLang = Detect.execute(text.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            canvas.drawRect(rect, sRectPaint);
            List<? extends Text> lineComponents = text.getComponents();
            for (Text currentLine : lineComponents) {
                float left = translateX(currentLine.getBoundingBox().left);
                float bottom = translateY(currentLine.getBoundingBox().bottom);
                // Draws the bounding box around the TextBlock.
                rect = new RectF(currentLine.getBoundingBox());
                rect.left = translateX(rect.left);
                rect.top = translateY(rect.top);
                rect.right = translateX(rect.right);
                rect.bottom = translateY(rect.bottom);
                //canvas.drawRect(rect, sRectPaint);

                if (translation) {
                    String textToBeTranslated = currentLine.getValue();
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
                    setTextSizeForWidth(sTextPaint, rect.width(), translatedText);
                    canvas.drawText(translatedText, left, bottom, sTextPaint);
                } else {
                    setTextSizeForWidth(sTextPaint, rect.width(), currentLine.getValue());
                    canvas.drawText(currentLine.getValue(), left, bottom, sTextPaint);
                }
            }
        }


//       //Block by Block
        else if(blockByBlock){
            float left = translateX(text.getBoundingBox().left);
            float bottom = translateY(text.getBoundingBox().top);
            // Draws the bounding box around the TextBlock.
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.top);
            canvas.drawRect(rect, sRectPaint);
            if(translation) {
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
                String s = squeezeText(translatedText);
                for (String line : s.split("\n")) {
                    setTextSizeForWidth(sTextPaint,rect.width(),line);
                    canvas.drawText(line, left, bottom, sTextPaint);
                    bottom += sTextPaint.descent() - sTextPaint.ascent();
                }
                //setTextSizeForWidth(sTextPaint, rect.width(), translatedText);
                //canvas.drawText(translatedText, left, bottom, sTextPaint);
            }
            else{
                String s = squeezeText(text.getValue());
                for (String line : s.split("\n")) {
                    setTextSizeForWidth(sTextPaint,rect.width(),line);
                    canvas.drawText(line, left, bottom, sTextPaint);
                    bottom += sTextPaint.descent() - sTextPaint.ascent();
                }
                //setTextSizeForWidth(sTextPaint, rect.width(), text.getValue());
                //canvas.drawText(text.getValue(), left, bottom, sTextPaint);
            }
        }
    }



    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    private String squeezeText(String s){
        StringBuilder sb = new StringBuilder(s);
        int i = 0;
        while ((i = sb.indexOf(" ", i + 80)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        return sb.toString();

    }



}
