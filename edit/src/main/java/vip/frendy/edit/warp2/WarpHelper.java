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
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
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

    // 触点
    private TouchHelper mTouchHelper;

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
            canvas.drawColor(0xFFCCCCCC);

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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touch_down();
            }

            float[] pt = {event.getX(), event.getY()};
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

            if (event.getAction() == MotionEvent.ACTION_UP) {
                touch_up();
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

    public void setDrawingView(CanvasView canvasView) {
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

    private int mLastWarpX = -9999; // Not a touch coordinate
    private int mLastWarpY;

    private void touch_down() {
        mUndoneMotions.clear();
    }

    private void touch_up() {
        if (visible) {
            mMotions.add(new MorphMatrix(mMorphMatrix));
        }
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
        return mMotions.size() > 0;
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
        if (mMotions.size() > 0) {
            if (visible) {
                mMorphMatrix = mMotions.get(mMotions.size() - 1);
            } else {
                mMorphMatrix = mMotions.get(0);
            }
        }
        invalidate();
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
