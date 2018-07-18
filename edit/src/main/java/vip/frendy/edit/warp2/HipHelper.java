package vip.frendy.edit.warp2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

public class HipHelper implements CanvasView.OnCanvasChangeListener {
    private static final String TAG = HipHelper.class.getSimpleName();

    // Mesh size
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    private MorphMatrix mMorphMatrix = new MorphMatrix(COUNT * 2);
    private final Matrix mMatrix = new Matrix();
    private final Matrix mInverse = new Matrix();

    private int mLastWarpX = -9999; // Not a touch coordinate
    private int mLastWarpY;

    private BitmapDrawable mBitmap;
    private CanvasView mCanvasView;
    private boolean attached = false;
    private boolean visible = true;

    private RectF mOval = new RectF();
    private Bitmap mBitmapOval;
    private boolean isSelectedOval = false;
    private float x_1, y_1;

    public void initMorpher() {
        if (mCanvasView != null) {
            mCanvasView.setFocusable(true);

            mBitmap = (BitmapDrawable) mCanvasView.getBackground();

            float w = mCanvasView.getWidth();
            float h = mCanvasView.getHeight();

            // Constructing mesh
            int index = 0;
            for (int y = 0; y <= HEIGHT; y++) {
                float fy = h * y / HEIGHT;
                for (int x = 0; x <= WIDTH; x++) {
                    float fx = w * x / WIDTH;
                    setXY(mMorphMatrix, index, fx, fy);
                    index += 1;
                }
            }

            mMatrix.invert(mInverse);

            //初始化位置
            x_1 = mCanvasView.getWidth() / 2;
            y_1 = mCanvasView.getHeight() / 2;
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
        if (visible) {
            canvas.drawColor(0xFFCCCCCC);

            canvas.concat(mMatrix);
            canvas.drawBitmapMesh(mBitmap.getBitmap(), WIDTH, HEIGHT, mMorphMatrix.getVerts(), 0,
                    null, 0, null);

            if(mBitmapOval != null) {
                mOval.left = x_1 - mBitmapOval.getWidth() / 2;
                mOval.top = y_1 - mBitmapOval.getHeight() / 2;
                mOval.right = mOval.left + mBitmapOval.getWidth();
                mOval.bottom = mOval.top + mBitmapOval.getHeight();
                canvas.drawBitmap(mBitmapOval, null, mOval, null);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isInArea(event.getX(), event.getY(), mOval)) {
                    isSelectedOval = true;
                    x_1 = event.getX();
                    y_1 = event.getY();
                    invalidate();
                } else {
                    isSelectedOval = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isSelectedOval) {
                    x_1 = event.getX();
                    y_1 = event.getY();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void warpHip() {
        if (visible) {
            float hx = mOval.left;
            float hy = mOval.top + (mOval.bottom - mOval.top) / 2;

            float[] pt = { hx, hy };
            mInverse.mapPoints(pt);

            int x = (int) pt[0];
            int y = (int) pt[1];
            if (mLastWarpX != x || mLastWarpY != y) {
                mLastWarpX = x;
                mLastWarpY = y;
                warp(pt[0], pt[1]);
                if (mCanvasView != null) {
                    mCanvasView.invalidate();
                }
            }
        }
    }

    private static void setXY(MorphMatrix morphMatrix, int index, float x, float y) {
        morphMatrix.getVerts()[index * 2] = x;
        morphMatrix.getVerts()[index * 2 + 1] = y;
    }

    private void warp(float cx, float cy) {
        final float K = 10000;
        float[] dst = mMorphMatrix.getVerts();
        float src[] = dst.clone();
        for (int i = 0; i < COUNT * 2; i += 2) {
            float x = src[i];
            float y = src[i + 1];
            float dx = cx - x;
            float dy = cy - y;
            float dd = dx * dx + dy * dy;
            float d = (float) Math.sqrt(dd);
            float pull = K / (dd + 0.000001f);

            pull /= (d + 0.000001f);

            if (pull >= 1) {
                dst[i] = cx;
                dst[i + 1] = cy;
            } else {
                dst[i] = x + dx * pull;
                dst[i + 1] = y + dy * pull;
            }
        }
    }

    public void invalidate() {
        if (mCanvasView != null) {
            mCanvasView.invalidate();
        }
    }

    public BitmapDrawable getBitmapDrawable() {
        return mBitmap;
    }

    public void setOvalBitmap(Bitmap oval) {
        mBitmapOval = oval;
    }

    public void setOpBitmap(Bitmap op) {

    }

    public void setStrength(int strength) {
        warpHip();
    }

    private boolean isInArea(float x, float y, RectF rectF) {
        return (x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom);
    }

    private static class MorphMatrix {
        private float[] verts;

        public MorphMatrix(MorphMatrix morphMatrix) {
            this.verts = new float[morphMatrix.verts.length];
            System.arraycopy(morphMatrix.verts, 0, this.verts, 0, morphMatrix.verts.length);
        }

        public MorphMatrix(final int size) {
            verts = new float[size];
        }

        public float[] getVerts() {
            return verts;
        }

        public void set(MorphMatrix morphMatrix) {
            System.arraycopy(morphMatrix.verts, 0, this.verts, 0, morphMatrix.verts.length);
        }
    }
}
