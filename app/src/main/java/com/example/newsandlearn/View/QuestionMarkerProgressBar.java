package com.example.newsandlearn.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ProgressBar with markers for question timestamps
 */
public class QuestionMarkerProgressBar extends ProgressBar {
    
    private List<Integer> questionMarkers = new ArrayList<>();
    private Paint markerPaint;
    private int videoDuration = 100; // in seconds
    
    public QuestionMarkerProgressBar(Context context) {
        super(context);
        init();
    }
    
    public QuestionMarkerProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public QuestionMarkerProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        markerPaint = new Paint();
        markerPaint.setColor(0xFFFFB800); // Yellow/gold color for markers
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setStrokeWidth(4f);
    }
    
    public void setQuestionMarkers(List<Integer> timestamps, int durationSeconds) {
        this.questionMarkers = timestamps;
        this.videoDuration = durationSeconds;
        invalidate();
    }
    
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (questionMarkers == null || questionMarkers.isEmpty() || videoDuration == 0) {
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw markers for each question timestamp
        for (Integer timestamp : questionMarkers) {
            float position = (float) timestamp / videoDuration;
            float x = width * position;
            
            // Draw a vertical line marker
            canvas.drawLine(x, 0, x, height, markerPaint);
            
            // Draw a circle at the top
            canvas.drawCircle(x, height / 2f, 6f, markerPaint);
        }
    }
}
