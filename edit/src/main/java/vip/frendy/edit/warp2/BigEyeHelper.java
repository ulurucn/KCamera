package vip.frendy.edit.warp2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by frendy on 2018/7/10.
 */

public class BigEyeHelper implements CanvasView.OnCanvasChangeListener {

    private Bitmap mBitmapSrc, mBitmap;
    private CanvasView mCanvasView;
    private boolean attached = false;
    private boolean original = false;

    private boolean visible = true;

    private int touch_x, touch_y;

    private List<Point> touchPoints = new ArrayList<>();

    private List<Point> cachedPoints = new ArrayList<>();

    private int mStrength = 10;

    private int radius = 100;

    public BigEyeHelper() {
    }

    /*
     @param radius 视图圆圈的半径
     @param distance 视图圆圈的距离
     @param yPosition 视图圆圈的y轴上的调整距离，yPosition > 0 偏下，  yPosition < 0 偏上
     */
    public void initMorpher(int radius, int distance, int yPosition) {
        if (mCanvasView != null && mCanvasView.getBackground() != null) {
            mCanvasView.setFocusable(true);
            mBitmapSrc = ((BitmapDrawable) mCanvasView.getBackground()).getBitmap();
            mBitmap = mBitmapSrc;
            invalidate();
        }
    }

    public boolean isAttached() {
        return this.attached;
    }

    public void attachCanvasView(CanvasView canvasView) {
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
        if (!original) {
            if (mStrength != 0) {
                Log.i("eye", "draw eyes strength : " + mStrength);
                mBitmap = ShapeUtils.enlarge(mBitmapSrc, touchPoints, radius, mStrength);

//                mBitmap = mBitmapSrc;
//
//                for (Point point : touchPoints) {
//                    mBitmap = ShapeUtils.enlarge(mBitmap, point.x, point.y, radius, mStrength);
//                }
            }
            canvas.drawBitmap(mBitmap, 0, 0, null);
        } else {
            canvas.drawBitmap(mBitmapSrc, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
                touch_x = (int)event.getX();
                touch_y = (int)event.getY();

                Point point = new Point();
                point.x = touch_x;
                point.y = touch_y;

                touchPoints.add(point);

                invalidate();
                break;
        }
        return true;
    }

    @Override
    public void onPreGenerateBitmap() {
        visible = false;
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

    public void setVisible(boolean visible) {
        this.visible = visible;
        invalidate();
    }

    public boolean getVisible() {
        return visible;
    }

    public void setOriginal(boolean original) {
        this.original = original;
        invalidate();
    }

    public boolean getOriginal() {
        return original;
    }


    public void redo() {
        if (touchPoints != null && cachedPoints != null) {
            if (cachedPoints.size() > 0) {
                Point lastPath = cachedPoints.get(touchPoints.size() - 1);
                touchPoints.add(lastPath);
                cachedPoints.remove(lastPath);
                invalidate();
            }
        }
    }

    public void undo() {
        if (touchPoints != null && cachedPoints != null) {
            if (touchPoints.size() > 0) {
                Point lastPath = touchPoints.get(touchPoints.size() - 1);
                touchPoints.remove(lastPath);
                cachedPoints.add(lastPath);
                invalidate();
            }
        }
    }

    public boolean canBackward() {
        if (this.touchPoints != null && this.cachedPoints != null) {
            return this.touchPoints.size() > 0;
        } else {
            return false;
        }
    }

    public boolean canForward() {
        if (this.touchPoints != null && this.cachedPoints != null) {
            return this.cachedPoints.size() > 0;
        } else {
            return false;
        }
    }

    class Point {
        int x;
        int y;
    }
}

