package com.rdi.compassproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by rudi on 29-12-2016.
 */

public class CompassView extends View{

    private final static String TAG = CompassView.class.getSimpleName();
    private Paint mPaint;

    private Bitmap mBitmapFace = null;
    private Bitmap mBitmapDial = null;
    private Bitmap mBitmapMark = null;

    private Rect mCanvasRect;
    private Rect mFaceRect;
    private Rect mFitRect;

    private Matrix mDialMatrix;
    private float mDialDegree;

    private Matrix mMarkMatrix;

    private float mMarkDegree;

    float scale;
    int offsetX, offsetY;

    private boolean isInitialized = false;
    private boolean isMarkSet = false;

    public CompassView(Context context) {
        super(context);
        initialize();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CompassView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCanvasRect.set(0, 0, canvas.getWidth(), canvas.getHeight());

        // Calculate scale to fit compass face.
        scale = Math.min(  (float) mCanvasRect.width()/mFaceRect.width(), (float) mCanvasRect.height()/mFaceRect.height() );

        // Calculate offset to center the face.
        offsetX = (int) ( (mCanvasRect.width() - mFaceRect.width()*scale)/2f ) ;
        offsetY = (int) ( (mCanvasRect.height() - mFaceRect.height()*scale)/2f ) ;

        // Calculate destination Rect
        mFitRect.set( offsetX, offsetY, (int)(mFaceRect.width()*scale) + offsetX, (int)(mFaceRect.height()*scale) + offsetY );

        // Draw compass face;
        canvas.drawBitmap(mBitmapFace, mFaceRect, mFitRect, mPaint);

        if(isMarkSet) {
            // Draw compass mark
            mMarkMatrix.reset();
            mMarkMatrix.preScale(scale, scale);
            mMarkMatrix.preTranslate(-mBitmapMark.getWidth() / 2, -mBitmapMark.getHeight() / 2 - mCanvasRect.width()/2);
            mMarkMatrix.postRotate(mMarkDegree + mDialDegree);
            mMarkMatrix.postTranslate(mCanvasRect.width() / 2, mCanvasRect.height() / 2);
            canvas.drawBitmap(mBitmapMark, mMarkMatrix, mPaint);
        }

        // Draw compass dial
        mDialMatrix.reset();
        mDialMatrix.preScale(scale, scale);
        mDialMatrix.preTranslate( -mBitmapDial.getWidth()/2, -mBitmapDial.getHeight()/2);
        mDialMatrix.postRotate(mDialDegree);
        mDialMatrix.postTranslate(mCanvasRect.width()/2, mCanvasRect.height()/2);

        canvas.drawBitmap(mBitmapDial, mDialMatrix, mPaint);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Recycle bitmaps to prevent memory leaks.
        if (mBitmapFace != null && !mBitmapFace.isRecycled()) {
            mBitmapFace.recycle();
            mBitmapFace = null;
        }
        if (mBitmapDial != null && !mBitmapDial.isRecycled()) {
            mBitmapDial.recycle();
            mBitmapDial = null;
        }
        if (mBitmapMark != null && !mBitmapMark.isRecycled()) {
            mBitmapMark.recycle();
            mBitmapMark = null;
        }
    }

    private void initialize() {

        Log.i(TAG, "CompassView Initialization...");

        mPaint = new Paint();
        mCanvasRect = new Rect();

        // Bitmaps are already scaled to ldpi, mdpi, hdpi...
        mBitmapFace = BitmapFactory.decodeResource(getResources(), R.drawable.compass_face);
        mBitmapDial = BitmapFactory.decodeResource(getResources(), R.drawable.compass_dial);
        mBitmapMark = BitmapFactory.decodeResource(getResources(), R.drawable.compass_mark);

        mFaceRect = new Rect(0, 0, mBitmapFace.getWidth(), mBitmapFace.getHeight());
        mFitRect = new Rect();

        mDialMatrix = new Matrix();
        mDialDegree = 0f;

        mMarkMatrix = new Matrix();
        mMarkDegree = 0f;

        isInitialized = true;
    }

    public void setDegree(float degree){
        mDialDegree = degree;
        invalidate();
    }

    public void setMark(float markBearing){
        mMarkDegree = markBearing;
        isMarkSet = true;
        invalidate();
    }

    public void removeMark(){
        isMarkSet = false;
        invalidate();;
    }

    public void updateMark(float markBearing) {
        if(isMarkSet){
            mMarkDegree = markBearing;
            invalidate();
        }
    }
}
