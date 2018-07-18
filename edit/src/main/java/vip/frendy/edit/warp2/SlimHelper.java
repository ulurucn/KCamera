package vip.frendy.edit.warp2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

public class SlimHelper implements CanvasView.OnCanvasChangeListener {
    private static final String TAG = SlimHelper.class.getSimpleName();

    // Mesh size
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    private MorphMatrix mMorphMatrix = new MorphMatrix(COUNT * 2);
    private MorphMatrix mMorphMatrixOrig = new MorphMatrix(COUNT * 2);
    private final Matrix mMatrix = new Matrix();
    private final Matrix mInverse = new Matrix();

    private BitmapDrawable mBitmap;
    private CanvasView mCanvasView;
    private boolean attached = false;
    private boolean visible = true;

    private RectF mOval = new RectF();
    private RectF mRectOpUp = new RectF();
    private RectF mRectOpDown = new RectF();
    private RectF mRectOpLeft = new RectF();
    private RectF mRectOpRight = new RectF();
    private Bitmap mBitmapOval, mBitmapOpUp, mBitmapOpDown, mBitmapOpLeft, mBitmapOpRight;
    private boolean isSelectedOval = false;
    private boolean isSelectedOpUp = false;
    private boolean isSelectedOpDown = false;
    private boolean isSelectedOpLeft = false;
    private boolean isSelectedOpRight = false;
    private float x_1, y_1;
    private float op_x, op_y;
    private float op_scale_x = 1;
    private float op_scale_y = 1;

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
                    setXY(mMorphMatrixOrig, index, fx, fy);
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
        canvas.drawColor(0xFFCCCCCC);

        canvas.concat(mMatrix);
        canvas.drawBitmapMesh(mBitmap.getBitmap(), WIDTH, HEIGHT, mMorphMatrix.getVerts(), 0,
                null, 0, null);

        if(mBitmapOval != null && visible) {
            float width = mBitmapOval.getWidth() * op_scale_x;
            float height = mBitmapOval.getHeight() * op_scale_y;
            mOval.left = x_1 - width / 2;
            mOval.top = y_1 - height / 2;
            mOval.right = mOval.left + width;
            mOval.bottom = mOval.top + height;
            canvas.drawBitmap(mBitmapOval, null, mOval, null);
        }
        if(mBitmapOpUp != null && visible) {
            mRectOpUp.left = mOval.right - (mOval.right - mOval.left) / 2 - mBitmapOpUp.getWidth() / 2;
            mRectOpUp.top = mOval.top - mBitmapOpUp.getHeight() / 2;
            mRectOpUp.right = mRectOpUp.left + mBitmapOpUp.getWidth();
            mRectOpUp.bottom = mRectOpUp.top + mBitmapOpUp.getHeight();
            canvas.drawBitmap(mBitmapOpUp, null, mRectOpUp, null);
        }
        if(mBitmapOpDown != null && visible) {
            mRectOpDown.left = mOval.right - (mOval.right - mOval.left) / 2 - mBitmapOpDown.getWidth() / 2;
            mRectOpDown.top = mOval.bottom - mBitmapOpDown.getHeight() / 2;
            mRectOpDown.right = mRectOpDown.left + mBitmapOpDown.getWidth();
            mRectOpDown.bottom = mRectOpDown.top + mBitmapOpDown.getHeight();
            canvas.drawBitmap(mBitmapOpDown, null, mRectOpDown, null);
        }
        if(mBitmapOpLeft != null && visible) {
            mRectOpLeft.left = mOval.left - mBitmapOpLeft.getWidth() / 2;
            mRectOpLeft.top = mOval.top + (mOval.bottom - mOval.top) / 2 - mBitmapOpLeft.getHeight() / 2;
            mRectOpLeft.right = mRectOpLeft.left + mBitmapOpLeft.getWidth();
            mRectOpLeft.bottom = mRectOpLeft.top + mBitmapOpLeft.getHeight();
            canvas.drawBitmap(mBitmapOpLeft, null, mRectOpLeft, null);
        }
        if(mBitmapOpRight != null && visible) {
            mRectOpRight.left = mOval.right - mBitmapOpRight.getWidth() / 2;
            mRectOpRight.top = mOval.top + (mOval.bottom - mOval.top) / 2 - mBitmapOpRight.getHeight() / 2;
            mRectOpRight.right = mRectOpRight.left + mBitmapOpRight.getWidth();
            mRectOpRight.bottom = mRectOpRight.top + mBitmapOpRight.getHeight();
            canvas.drawBitmap(mBitmapOpRight, null, mRectOpRight, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(isInArea(event.getX(), event.getY(), mOval)) {
                    isSelectedOval = true;
                    isSelectedOpUp = false;
                    isSelectedOpDown = false;
                    isSelectedOpLeft = false;
                    isSelectedOpRight = false;
                    x_1 = event.getX();
                    y_1 = event.getY();
                    invalidate();
                } else if(mBitmapOpUp != null && isInArea(event.getX(), event.getY(), mRectOpUp)) {
                    isSelectedOval = false;
                    isSelectedOpUp = true;
                    isSelectedOpDown = false;
                    isSelectedOpLeft = false;
                    isSelectedOpRight = false;
                    op_x = event.getX();
                    op_y = event.getY();
                } else if(mBitmapOpDown != null && isInArea(event.getX(), event.getY(), mRectOpDown)) {
                    isSelectedOval = false;
                    isSelectedOpUp = false;
                    isSelectedOpDown = true;
                    isSelectedOpLeft = false;
                    isSelectedOpRight = false;
                    op_x = event.getX();
                    op_y = event.getY();
                } else if(mBitmapOpLeft != null && isInArea(event.getX(), event.getY(), mRectOpLeft)) {
                    isSelectedOval = false;
                    isSelectedOpUp = false;
                    isSelectedOpDown = false;
                    isSelectedOpLeft = true;
                    isSelectedOpRight = false;
                    op_x = event.getX();
                    op_y = event.getY();
                } else if(mBitmapOpRight != null && isInArea(event.getX(), event.getY(), mRectOpRight)) {
                    isSelectedOval = false;
                    isSelectedOpUp = false;
                    isSelectedOpDown = false;
                    isSelectedOpLeft = false;
                    isSelectedOpRight = true;
                    op_x = event.getX();
                    op_y = event.getY();
                } else {
                    isSelectedOval = false;
                    isSelectedOpUp = false;
                    isSelectedOpDown = false;
                    isSelectedOpLeft = false;
                    isSelectedOpRight = false;
                    visible = !visible;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isSelectedOval) {
                    x_1 = event.getX();
                    y_1 = event.getY();
                    invalidate();
                } else if(isSelectedOpUp) {
                    double d = event.getY() - op_y;
                    op_scale_y = 1 - (float) d / mCanvasView.getHeight();
                    invalidate();
                } else if(isSelectedOpDown) {
                    double d = event.getY() - op_y;
                    op_scale_y = 1 + (float) d / mCanvasView.getHeight();
                    invalidate();
                } else if(isSelectedOpLeft) {
                    double d = event.getX() - op_x;
                    op_scale_x = 1 - (float) d / mCanvasView.getWidth();
                    invalidate();
                } else if(isSelectedOpRight) {
                    double d = event.getX() - op_x;
                    op_scale_x = 1 + (float) d / mCanvasView.getWidth();
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
        visible = false;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    //作用范围半径
    private int r = 150;
    private void warp(float startX, float startY, float endX, float endY) {
        //计算拖动距离
        float ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY);
        float dPull = (float) Math.sqrt(ddPull);
        //文献中提到的算法，并不能很好的实现拖动距离 MC 越大变形效果越明显的功能，下面这行代码则是我对该算法的优化
        //dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;

        float[] orig = mMorphMatrixOrig.getVerts();
        float[] verts = mMorphMatrix.getVerts();

        for (int i = 0; i < COUNT * 2; i += 2) {
            //计算每个坐标点与触摸点之间的距离
            float dx = orig[i] - startX;
            float dy = orig[i + 1] - startY;
            float dd = dx * dx + dy * dy;
            float d = (float) Math.sqrt(dd);

            //文献中提到的算法同样不能实现只有圆形选区内的图像才进行变形的功能，这里需要做一个距离的判断
            if (d < r) {
                //变形系数，扭曲度
                double e = (r * r - dd) * (r * r - dd) / ((r * r - dd + dPull * dPull) * (r * r - dd + dPull * dPull));
                double pullX = e * (endX - startX);
                double pullY = e * (endY - startY);
                verts[i] = (float) (orig[i] + pullX);
                verts[i + 1] = (float) (orig[i + 1] + pullY);
            }
        }
        invalidate();
    }

    private void toWarpLeft(int strength) {
        float _startX = mOval.left;
        float _startY = mOval.top;
        int _step = 1;
        int _step_max = (int)(mOval.bottom - mOval.top) / 2;

        while (_step < _step_max) {
            float _scale = 1;
            float _endX = _startX + strength * _scale;
            float _endY = _startY;

            warp(_startX, _startY, _endX, _endY);

            _startY += 1;
            _step += 1;
        }
    }

    private void toWarpRight(int strength) {
        float _startX = mOval.right;
        float _startY = mOval.top;
        int _step = 1;
        int _step_max = (int)(mOval.bottom - mOval.top) / 2;

        while (_step < _step_max) {
            float _scale = 1;
            float _endX = _startX - strength * _scale;
            float _endY = _startY;

            warp(_startX, _startY, _endX, _endY);

            _startY += 1;
            _step += 1;
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

    public void setOpBitmap(Bitmap up, Bitmap down, Bitmap left, Bitmap right) {
        mBitmapOpUp = up;
        mBitmapOpDown = down;
        mBitmapOpLeft = left;
        mBitmapOpRight = right;
    }

    public void setStrength(int strength) {
        toWarpLeft(strength);
        toWarpRight(strength);
    }

    private boolean isInArea(float x, float y, RectF rectF) {
        return (x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom);
    }

    private static void setXY(MorphMatrix morphMatrix, int index, float x, float y) {
        morphMatrix.getVerts()[index * 2] = x;
        morphMatrix.getVerts()[index * 2 + 1] = y;
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
