package vip.frendy.edit.warp2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

/**
 * Created by frendy on 2018/7/10.
 */

public class BreastHelper implements CanvasView.OnCanvasChangeListener {

    private Bitmap mBitmapSrc, mBitmap, mBitmapOp1, mBitmapOp2;
    private CanvasView mCanvasView;
    private boolean attached = false;

    private int r_max = 300;
    private int r_1 = 100;
    private int r_2 = 100;
    private float x_1, y_1, x_2, y_2;
    private boolean isSelectedCircle1 = false;
    private boolean isSelectedCircle2 = false;
    private Paint mCirclePaint;
    private boolean showCircle = true;

    private float op_x, op_y;
    private RectF mRectOp1 = new RectF();
    private RectF mRectOp2 = new RectF();
    private boolean isSelectedOp1 = false;
    private boolean isSelectedOp2 = false;

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

    public void setOpBitmap(Bitmap op1, Bitmap op2) {
        mBitmapOp1 = op1;
        mBitmapOp2 = op2;
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

                if(mBitmapOp1 != null) {
                    mRectOp1.left = x_1 - r_1 - mBitmapOp1.getWidth() / 2 + 4;
                    mRectOp1.top = y_1 + r_1 - mBitmapOp1.getWidth() / 2;
                    mRectOp1.right = mRectOp1.left + mBitmapOp1.getWidth();
                    mRectOp1.bottom = mRectOp1.top + mBitmapOp1.getWidth();
                    canvas.drawBitmap(mBitmapOp1, null, mRectOp1, null);
                }
                if(mBitmapOp2 != null) {
                    mRectOp2.left = x_2 + r_2 - mBitmapOp2.getWidth() / 2 - 4;
                    mRectOp2.top = y_2 + r_2 - mBitmapOp2.getWidth() / 2;
                    mRectOp2.right = mRectOp2.left + mBitmapOp2.getWidth();
                    mRectOp2.bottom = mRectOp2.top + mBitmapOp2.getWidth();
                    canvas.drawBitmap(mBitmapOp2, null, mRectOp2, null);
                }
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
                    isSelectedOp1 = false;
                    isSelectedOp2 = false;
                    invalidate();
                } else if(isInCircle(event.getX(), event.getY(), x_2, y_2, r_2)) {
                    x_2 = event.getX();
                    y_2 = event.getY();
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = true;
                    isSelectedOp1 = false;
                    isSelectedOp2 = false;
                    invalidate();
                } else if(mBitmapOp1 != null && isInArea(event.getX(), event.getY(), mRectOp1)) {
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = false;
                    isSelectedOp1 = true;
                    isSelectedOp2 = false;
                    op_x = event.getX();
                    op_y = event.getY();
                } else if(mBitmapOp2 != null && isInArea(event.getX(), event.getY(), mRectOp2)) {
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = false;
                    isSelectedOp1 = false;
                    isSelectedOp2 = true;
                    op_x = event.getX();
                    op_y = event.getY();
                } else {
                    isSelectedCircle1 = false;
                    isSelectedCircle2 = false;
                    isSelectedOp1 = false;
                    isSelectedOp2 = false;
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
                } else if(isSelectedOp1) {
                    r_1 = getR1(event, r_1, op_x, op_y);
                    invalidate();
                } else if(isSelectedOp2) {
                    r_2 = getR2(event, r_2, op_x, op_y);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    public void onPreGenerateBitmap() {
        showCircle = true;
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

    private boolean isInArea(float x, float y, RectF rectF) {
        return (x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom);
    }

    private int getR1(MotionEvent event, int r, float op_x, float op_y) {
        double d = Math.sqrt((event.getX() - op_x) * (event.getX() - op_x) + (event.getY() - op_y) * (event.getY() - op_y));
        if(event.getX() - op_x > 0) {
            r = r - (int) (d / 50);
        } else {
            r = r + (int) (d / 50);
        }
        if(r > r_max) r = r_max;
        if(r < 10) r = 10;
        return r;
    }

    private int getR2(MotionEvent event, int r, float op_x, float op_y) {
        double d = Math.sqrt((event.getX() - op_x) * (event.getX() - op_x) + (event.getY() - op_y) * (event.getY() - op_y));
        if(event.getX() - op_x < 0) {
            r = r - (int) (d / 50);
        } else {
            r = r + (int) (d / 50);
        }
        if(r > r_max) r = r_max;
        if(r < 10) r = 10;
        return r;
    }
}
