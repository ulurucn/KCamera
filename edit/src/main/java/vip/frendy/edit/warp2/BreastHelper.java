package vip.frendy.edit.warp2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

/**
 * Created by frendy on 2018/7/10.
 */

public class BreastHelper implements CanvasView.OnCanvasChangeListener {

    private Bitmap mBitmapSrc, mBitmap;
    private CanvasView mCanvasView;
    private boolean attached = false;

    private int r_1 = 100;
    private int r_2 = 100;
    private float x_1, y_1, x_2, y_2;
    private boolean isSelectedCircle1 = false;
    private boolean isSelectedCircle2 = false;
    private Paint mCirclePaint;
    private boolean showCircle = true;

    private int mStrength = 0;

    public BreastHelper() {
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setColor(Color.parseColor("#d75372"));
    }

    public void initMorpher() {
        if (mCanvasView != null) {
            mCanvasView.setFocusable(true);
            mBitmapSrc = ((BitmapDrawable) mCanvasView.getBackground()).getBitmap();
            mBitmap = mBitmapSrc;

            x_1 = mCanvasView.getWidth() / 2 - r_1;
            y_1 = mCanvasView.getHeight() / 2;
            x_2 = mCanvasView.getWidth() / 2 + r_2;
            y_2 = mCanvasView.getHeight() / 2;
            invalidate();
        }
    }

    public boolean isAttached() {
        return this.attached;
    }

    public void setDrawingView(CanvasView canvasView) {
        if (canvasView == null) {
            if (mCanvasView != null) {
                mCanvasView.setOnCanvasChangeListener(null);
            }
            attached = false;
        } else {
            attached = true;
            canvasView.setOnCanvasChangeListener(this);
        }
        mCanvasView = canvasView;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mCirclePaint != null) {
            if(mStrength != 0) {
                mBitmap = ShapeUtils.enlarge(mBitmapSrc, (int) x_1, (int) y_1, r_1, mStrength);
                mBitmap = ShapeUtils.enlarge(mBitmap, (int) x_2, (int) y_2, r_2, mStrength);
            }
            canvas.drawBitmap(mBitmap, 0, 0, null);

            if(showCircle) {
                canvas.drawCircle(x_1, y_1, r_1, mCirclePaint);
                canvas.drawCircle(x_2, y_2, r_2, mCirclePaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isInCircle(event.getX(), event.getY(), x_1, y_1, r_1)) {
                    x_1 = event.getX();
                    y_1 = event.getY();
                    isSelectedCircle1 = true;
                    isSelectedCircle2 = false;
                    invalidate();
                } else if(isInCircle(event.getX(), event.getY(), x_2, y_2, r_2)) {
                    x_2 = event.getX();
                    y_2 = event.getY();
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = true;
                    invalidate();
                } else {
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = false;
                    showCircle = !showCircle;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isSelectedCircle1) {
                    x_1 = event.getX();
                    y_1 = event.getY();
                    invalidate();
                } else if(isSelectedCircle2) {
                    x_2 = event.getX();
                    y_2 = event.getY();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void invalidate() {
        if (mCanvasView != null) {
            mCanvasView.invalidate();
        }
    }

    public void setStrength(int strength) {
        mStrength = strength;
        invalidate();
    }

    public void showCircle(boolean show) {
        showCircle = show;
    }

    private boolean isInCircle(float eventX, float eventY, float x, float y, float r) {
        double d = Math.sqrt((eventX - x) * (eventX - x) + (eventY - y) * (eventY - y));
        return d <= r;
    }
}
