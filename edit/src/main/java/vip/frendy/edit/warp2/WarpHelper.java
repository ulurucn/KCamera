package vip.frendy.edit.warp2;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import java.util.ArrayList;

public class WarpHelper implements CanvasView.OnCanvasChangeListener {
    private static final String TAG = WarpHelper.class.getSimpleName();

    // Mesh size
    private static final int WIDTH = 9;
    private static final int HEIGHT = 9;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    private MorphMatrix mMorphMatrix = new MorphMatrix(COUNT * 2);
    private final Matrix mMatrix = new Matrix();
    private final Matrix mInverse = new Matrix();

    private ArrayList<MorphMatrix> mMotions = new ArrayList<>();
    private ArrayList<MorphMatrix> mUndoneMotions = new ArrayList<>();

    private BitmapDrawable mBitmap;
    private CanvasView mCanvasView;
    private boolean attached = false;
    private boolean visible = true;
    private boolean original = false;

    // 触点
    private TouchHelper mTouchHelper;
    private float startX, startY;

    private OnWarpCanvasDrawListener mOnWarpCanvasDrawListener;

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
            mMotions.add(new MorphMatrix(mMorphMatrix));

            // 触点
            mTouchHelper = new TouchHelper();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (visible) {
            canvas.concat(mMatrix);
            canvas.drawBitmapMesh(mBitmap.getBitmap(), WIDTH, HEIGHT, mMorphMatrix.getVerts(), 0,
                    null, 0, null);

            // 触点
            if(mTouchHelper != null)
                mTouchHelper.onDraw(canvas);

            if(mOnWarpCanvasDrawListener != null)
                mOnWarpCanvasDrawListener.onWarpCanvasDrawed();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (visible) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mUndoneMotions.clear();
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mMotions.add(new MorphMatrix(mMorphMatrix));
                    warp(startX, startY, event.getX(), event.getY());
                    break;
            }
        }
        // 触点
        if(mTouchHelper != null)
            mTouchHelper.onTouchEvent(mCanvasView, event);

        return true;
    }

    @Override
    public void onPreGenerateBitmap() {}

    public boolean isAttached() {
        return this.attached;
    }

    public void attachCanvasView(CanvasView canvasView) {
        if (canvasView == null) {
            if (mCanvasView != null) {
                mCanvasView.setOnCanvasChangeListener(null);
            }
            attached = false;
            mMotions.clear();
            mUndoneMotions.clear();
        } else {
            attached = true;
            canvasView.setOnCanvasChangeListener(this);
        }
        mCanvasView = canvasView;
    }

    private static void setXY(MorphMatrix morphMatrix, int index, float x, float y) {
        morphMatrix.getVerts()[index * 2] = x;
        morphMatrix.getVerts()[index * 2 + 1] = y;
    }

    //作用范围半径
    private int r = 150;
    private void warp(float startX, float startY, float endX, float endY) {
        //计算拖动距离
        float ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY);
        float dPull = (float) Math.sqrt(ddPull);
        //文献中提到的算法，并不能很好的实现拖动距离 MC 越大变形效果越明显的功能，下面这行代码则是我对该算法的优化
        //dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;

        float[] orig = mMorphMatrix.getVerts();
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

    /*
    * Clear Undo history
    * */
    public void clearUndo() {
        if (mMotions.size() > 1) {
            mMorphMatrix.set(mMotions.get(mMotions.size() - 1));
            mMotions.clear();
            mMotions.add(new MorphMatrix(mMorphMatrix));
        }
    }

    /*
    * Handling Undo click
    * */
    public void undo() {
        if (mCanvasView != null && mMotions.size() > 1) {
            mMorphMatrix.set(mMotions.get(mMotions.size() - 2));
            mUndoneMotions.add(mMotions.remove(mMotions.size() - 1));
            invalidate();
        }
    }

    public boolean isUndoActive() {
        return mMotions.size() > 1;
    }

    /*
    * Handling Redo click
    * */
    public void redo() {
        if (mCanvasView != null && mUndoneMotions.size() > 0) {
            mMorphMatrix.set(mUndoneMotions.remove(mUndoneMotions.size() - 1));
            mMotions.add(new MorphMatrix(mMorphMatrix));
            invalidate();
        }
    }

    public boolean isRedoActive() {
        return mUndoneMotions.size() > 0;
    }

    public void setVisibility(boolean visibility) {
        visible = visibility;
        invalidate();
    }

    public void setOriginal(boolean original) {
        this.original = original;
        if(mMotions.size() > 0) {
            if(!original) {
                mMorphMatrix = mMotions.get(mMotions.size() - 1);
            } else {
                mMorphMatrix = mMotions.get(0);
            }
        }
        invalidate();
    }

    public boolean getOriginal() {
        return original;
    }

    public void invalidate() {
        if (mCanvasView != null) {
            mCanvasView.invalidate();
        }
    }

    /**
     * 触点UI设置
     */
    public void setTouchCirclePaint(Paint paint) {
        if(mTouchHelper != null)
            mTouchHelper.setTouchCirclePaint(paint);
    }

    public void setTouchCirclePaint2(Paint paint) {
        if(mTouchHelper != null)
            mTouchHelper.setTouchCirclePaint2(paint);
    }

    public void setTouchCrossPaint(Paint paint) {
        if(mTouchHelper != null)
            mTouchHelper.setTouchCrossPaint(paint);
    }

    public void setTouchCrossPaint2(Paint paint) {
        if(mTouchHelper != null)
            mTouchHelper.setTouchCrossPaint2(paint);
    }

    public void setTouchLinePaint(Paint paint) {
        if(mTouchHelper != null)
            mTouchHelper.setTouchLinePaint(paint);
    }

    public void setTouchStrokeWidth(int width) {
        if(mTouchHelper != null)
            mTouchHelper.setStrokeWidth(width);
    }

    public BitmapDrawable getBitmapDrawable() {
        return mBitmap;
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

    //监听
    public void setOnWarpCanvasDrawListener(OnWarpCanvasDrawListener listener) {
        mOnWarpCanvasDrawListener = listener;
    }

    public interface OnWarpCanvasDrawListener {
        void onWarpCanvasDrawed();
    }
}
