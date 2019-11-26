/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.detection.tracking;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;

import java.util.*;

/**
 * A tracker that handles non-max suppression and matches existing objects to new detections.
 */
public class MultiBoxTracker {
    private static final float TEXT_SIZE_DIP = 18;
    private static Integer[] COLORS = makeColorGradient(.2f, .2f, .2f, 0, 2, 4, 21);
    private final Queue<Integer> availableColors = new LinkedList<Integer>();
    private final Paint boxPaint = new Paint();
    private final float textSizePx;
    private int frameWidth;
    private int frameHeight;
    private int sensorOrientation;
    private float[][][][] results;

    public MultiBoxTracker(final Context context) {
        for (final int color : COLORS) {
            availableColors.add(color);
        }

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Style.FILL);
        boxPaint.setStrokeWidth(10.0f);
        boxPaint.setStrokeCap(Cap.ROUND);
        boxPaint.setStrokeJoin(Join.ROUND);
        boxPaint.setStrokeMiter(100);
        boxPaint.setAntiAlias(true);
        boxPaint.setTextSize(32 * context.getResources().getDisplayMetrics().density);

        textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, context.getResources().getDisplayMetrics());
    }

    public synchronized void setFrameConfiguration(
            final int width, final int height, final int sensorOrientation) {
        frameWidth = width;
        frameHeight = height;
        this.sensorOrientation = sensorOrientation;
    }

    public synchronized void trackResults(final float[][][][] results, final long timestamp) {
        this.results = results;
    }

    private static final String[] labels = new String[]{
            "background",
            "aeroplane",
            "bicycle",
            "bird",
            "boat",
            "bottle",
            "bus",
            "car",
            "cat",
            "chair",
            "cow",
            "dining table",
            "dog",
            "horse",
            "motorbike",
            "person",
            "potted plant",
            "sheep",
            "sofa",
            "train",
            "tv"
    };
    public synchronized void draw(final Canvas canvas) {
        final boolean rotated = sensorOrientation % 180 == 90;
        final float multiplier =
                Math.min(
                        canvas.getHeight() / (float) (rotated ? frameWidth : frameHeight),
                        canvas.getWidth() / (float) (rotated ? frameHeight : frameWidth));
        int w = (int) (multiplier * (rotated ? frameHeight : frameWidth));
        int h = (int) (multiplier * (rotated ? frameWidth : frameHeight));
                if (results != null) {
            int pos = 0;
            float xw = w / 257f;
            float xh = h / 257f;
            RectF r = new RectF();
            HashSet<Integer> used = new HashSet<>();
            for (int y = 0; y < 257; y++) {
                for (int x = 0; x < 257; x++) {
                    pos = getIndexOfMax(results[0][y][x]);
                    if (pos > 0) {
                        used.add(pos);
                        boxPaint.setColor(COLORS[pos]);
                        r.left = (x / 257f * w) - xw;
                        r.top = (y / 257f * h) - xh;
                        r.right = (x / 257f * w) + xw;
                        r.bottom = (y / 257f * h) + xh;
                        canvas.drawRect(r, boxPaint);
                    }
                }
            }
            float x = 5;
            float large = Math.max(canvas.getWidth(), canvas.getHeight());
            float small = Math.min(canvas.getWidth(), canvas.getHeight());
            float skip = boxPaint.getTextSize();
            float y = (large - small) / 4 + skip/2f;
            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(boxPaint.getTextSize());
            for (int i : used) {
                boxPaint.setColor(COLORS[i]);
                textPaint.setColor(COLORS[i]);
                canvas.save();
//                canvas.drawText(labels[i],x,y,boxPaint);
                int width = (int) boxPaint.measureText(labels[i]);
                canvas.translate(x, y);
                StaticLayout staticLayout = new StaticLayout(labels[i], textPaint, (int) width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                staticLayout.draw(canvas);
                y += skip;
                canvas.restore();
            }

        }
    }

    public int getIndexOfMax(float array[]) {
        if (array.length == 0) {
            return -1; // array contains no elements
        }
        float max = array[0];
        int pos = 0;

        for (int i = 1; i < array.length; i++) {
            if (max < array[i]) {
                pos = i;
                max = array[i];
            }
        }
        return pos;
    }

    public static Integer[] makeColorGradient(float frequency1, float frequency2, float frequency3, float phase1, float phase2, float phase3, int len) {
        Integer[] c = new Integer[len];
        int center = 128;
        int width = 127;
        for (int i = 0; i < len; ++i) {
            int red = (int) (Math.sin(frequency1 * i + phase1) * width + center);
            int grn = (int) (Math.sin(frequency2 * i + phase2) * width + center);
            int blu = (int) (Math.sin(frequency3 * i + phase3) * width + center);
            c[i] = Color.rgb(red, grn, blu);
        }
        List<Integer> list = Arrays.asList(c);
        Collections.shuffle(list);

        return list.toArray(new Integer[]{});
    }
}
