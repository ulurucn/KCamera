package vip.frendy.edit.warp2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;

import vip.frendy.edit.colorful.ColorfulUtils;

public class ThinfaceView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener{

    private Context mContext;

    /**
     * 缩放相关属性
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private float scaleFactor = 1.0f;
    private Rect mInitImageRect;

    private Rect mImageRect;

    //触点
    private boolean isMultiPointer = false;
    private float mLastX;
    private float mLastY;
    private int lastPointerCount = 0;
    private boolean isCanDrag;
    //防误触相关变量
    private long lastCheckDrawTime = 0;
    private boolean isCanDrawPath = false;

    /**
     * 绘画板宽度
     */
    private int mImageWidth;

    /**
     * 绘画板高度
     */
    private int mImageHeight;

    private int mPadding = 0;

    private boolean enable;

    private ColorfulUtils.Type mType = ColorfulUtils.Type.COLOR; //画板类型

    private Bitmap bmBaseLayer;//原图
    private Bitmap bmCoverLayer;//橡皮擦
    private Bitmap bmColorfulLayer;//颜色


    //触点圈圈半径
    private int touchCircleR = 80;
    //触点圈圈
    private Paint touchCirclePaint;

    //是否显示触点圈圈
    private boolean enableTouchCircle = true;
    private boolean showTouchCircle = false;
    private float touchX, touchY;

    private int color = 0x602a5caa;

    private BlurMaskFilter bmf;

    private OnCanvasUpdatedListener mUpdatedListener;

    private Bitmap mBitmapSrc, mBitmap;
    private boolean attached = false;
    private boolean original = false;


    private int touch_x, touch_y;


    private static final int WIDTH = 9;
    private static final int HEIGHT = 9;
    private static final int COUNT = (WIDTH + 1) * (HEIGHT + 1);

    private MorphMatrix mMorphMatrix = new MorphMatrix(COUNT * 2);
    private final Matrix mMatrix = new Matrix();
    private final Matrix mInverse = new Matrix();

    private ArrayList<MorphMatrix> mMotions = new ArrayList<>();
    private ArrayList<MorphMatrix> mUndoneMotions = new ArrayList<>();

    private BitmapDrawable mBitmapDrawable;
    private boolean visible = true;

    // 触点
    private TouchHelper mTouchHelper;
    private float startX, startY;

    //作用范围半径
    private int r = 150;

    //曲面形变的比率
    private int ratio = 1;

    public ThinfaceView(Context context) {
        super(context);
        this.mContext = context;
        initDrawView();
    }

    public ThinfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initDrawView();
    }

    public ThinfaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initDrawView();
    }

    private void initDrawView() {

        mImageRect = new Rect();
        mInitImageRect = new Rect();
        setWillNotDraw(false);

        mScaleGestureDetector = new ScaleGestureDetector(mContext, this);
    }



    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scale = detector.getScaleFactor();
        scaleFactor *= scale;
        if (scaleFactor < 1.0f){
            scaleFactor = 1.0f;
        }
        if (scaleFactor > 2.0f)
            scaleFactor = 2.0f;

        if (mImageRect != null){
            int addWidth =(int) (mInitImageRect.width() * scaleFactor) - mImageRect.width ();
            int addHeight=(int) (mInitImageRect.height() * scaleFactor) - mImageRect.height();
            float centerWidthRatio = (detector.getFocusX() - mImageRect.left) / mImageRect.width();
            float centerHeightRatio = (detector.getFocusY() - mImageRect.left) / mImageRect.height();

            int leftAdd = (int) (addWidth * centerWidthRatio);
            int topAdd = (int) (addHeight * centerHeightRatio);

            mImageRect.left =  mImageRect.left - leftAdd;
            mImageRect.right = mImageRect.right + (addWidth - leftAdd);
            mImageRect.top = mImageRect.top - topAdd;
            mImageRect.bottom = mImageRect.bottom + (addHeight - topAdd);
            checkCenterWhenScale();
        }

        invalidate();
        return true;
    }

    private void checkCenterWhenScale() {
        int deltaX = 0;
        int deltaY = 0;
        if (mImageRect.left > mInitImageRect.left) {
            deltaX = mInitImageRect.left - mImageRect.left;
        }
        if (mImageRect.right < mInitImageRect.right) {
            deltaX = mInitImageRect.right - mImageRect.right;
        }
        if (mImageRect.top > mInitImageRect.top) {
            deltaY = mInitImageRect.top - mImageRect.top;
        }
        if (mImageRect.bottom < mInitImageRect.bottom) {
            deltaY = mInitImageRect.bottom - mImageRect.bottom;
        }
        mImageRect.offset(deltaX,deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {

        super.dispatchTouchEvent(event);

        float pointerX = 0,pointerY = 0;

        int pointerCount = event.getPointerCount();
        //计算多个触摸点的平均值
        for (int i = 0; i < pointerCount; i++){
            pointerX += event.getX(i);
            pointerY += event.getY(i);
        }
        pointerX = pointerX / pointerCount;
        pointerY = pointerY / pointerCount;
        if(pointerCount > 1){
            isMultiPointer = true;
            //在多指模式，防误触变量重置
            isCanDrawPath = false;
            lastCheckDrawTime = 0;
        }
        if(lastPointerCount != pointerCount){
            mLastX = pointerX;
            mLastY = pointerY;
            isCanDrag = false;
            lastPointerCount = pointerCount;
        }
        if(isMultiPointer){
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if (pointerCount == 1)
                        break;
                    if (mImageRect.width() > mInitImageRect.width()){ //仅仅在放大的状态，图片才可移动
                        int dx = (int) (pointerX - mLastX);
                        int dy = (int) (pointerY - mLastY);
                        if (!isCanDrag)
                            isCanDrag = isCanDrag(dx,dy);
                        if (isCanDrag) {
                            if (mImageRect.left + dx > mInitImageRect.left)
                                dx = mInitImageRect.left - mImageRect.left;
                            if (mImageRect.right + dx < mInitImageRect.right)
                                dx = mInitImageRect.right - mImageRect.right;
                            if (mImageRect.top + dy > mInitImageRect.top)
                                dy = mInitImageRect.top - mImageRect.top;
                            if (mImageRect.bottom + dy < mInitImageRect.bottom)
                                dy = mInitImageRect.bottom - mImageRect.bottom;
                            mImageRect.offset(dx, dy);
                        }
                    }
                    mLastX = pointerX;
                    mLastY = pointerY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    lastPointerCount = 0;
                    isMultiPointer = false;
                    break;
            }
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();

        Log.i("eye", "x: " + touch_x + " y : " + touch_y + " dispatch");

        //防误触
        if(!isCanDrawPath){
            if(lastCheckDrawTime == 0) {
                lastCheckDrawTime = System.currentTimeMillis();
            }
            if(System.currentTimeMillis() - lastCheckDrawTime > 50) { //大于50ms为有效值
                isCanDrawPath = true;
            }
        }
        return true;
    }

    private boolean isCanDrag(int dx, int dy) {
        return Math.sqrt((dx*dx)+(dy*dy)) >= 5.0f;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - mPadding * 2;
        int viewHeight = contentHeight - mPadding * 2;
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;

        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
        mInitImageRect.set(imageLeft,imageTop,imageRight,imageBottom);

    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                touch_x = (int)event.getX();
                touch_y = (int)event.getY();
//
//                Point point = new Point();
//
//                float ratio = (mImageRect.right - mImageRect.left) / (float) mImageWidth;
//                point.x = (int) ((touch_x - mImageRect.left) / ratio);
//                point.y = (int) ((touch_y - mImageRect.top) / ratio);


                invalidate();
                break;
        }
        return true;
    }

    public void setBackgroundResource(String imgPath) {
        File file = new File(imgPath);
        if (file == null || !file.exists()) {
            //Log.w(TAG, "setSrcPath invalid file path " + imgPath);
            return;
        }

        //reset();

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        bmBaseLayer = bitmap;
        mBitmapSrc = bitmap;


        // Constructing mesh
        int index = 0;
        for (int y = 0; y <= HEIGHT; y++) {
            float fy = mImageHeight * y / HEIGHT;
            for (int x = 0; x <= WIDTH; x++) {
                float fx = mImageWidth * x / WIDTH;
                setXY(mMorphMatrix, index, fx, fy);
                index += 1;
            }
        }

        mMatrix.invert(mInverse);
        mMotions.add(new MorphMatrix(mMorphMatrix));

        // 触点
        mTouchHelper = new TouchHelper(40);
        this.r = 100;
        this.ratio = 3;

        requestLayout();
        invalidate();
    }


    public boolean reset() {
        this.mImageWidth = 0;
        this.mImageHeight = 0;
        if(bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        if(bmColorfulLayer != null) {
            bmColorfulLayer.recycle();
            bmColorfulLayer = null;
        }


        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        if(bmColorfulLayer != null) {
            bmColorfulLayer.recycle();
            bmColorfulLayer = null;
        }
    }

    public Bitmap getColorBitmap() {
        if (bmColorfulLayer == null) {
            Bitmap bitmap = Bitmap.createBitmap(this.mImageWidth, this.mImageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bmBaseLayer, 0.0F, 0.0F, (Paint)null);
            canvas.save();
            return bitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(this.mImageWidth, this.mImageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bmBaseLayer, 0.0F, 0.0F, (Paint)null);
            canvas.drawBitmap(bmColorfulLayer, 0.0F, 0.0F, (Paint)null);
            canvas.save();
            return bitmap;
        }
    }

    private static void setXY(MorphMatrix morphMatrix, int index, float x, float y) {
        morphMatrix.getVerts()[index * 2] = x;
        morphMatrix.getVerts()[index * 2 + 1] = y;
    }


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
                e = e / ratio;
                double pullX = e * (endX - startX);
                double pullY = e * (endY - startY);
                verts[i] = (float) (orig[i] + pullX);
                verts[i + 1] = (float) (orig[i + 1] + pullY);
            }
        }
        invalidate();
    }


    /**
     * 设置画板更新监听
     * @param listener
     */
    public void setOnCanvasUpdatedListener(OnCanvasUpdatedListener listener) {
        mUpdatedListener = listener;
    }

    public interface OnCanvasUpdatedListener {
        void OnCanvasUpdated();
    }


    public void setOriginal(boolean original) {
        this.original = original;
        invalidate();
    }

    public boolean getOriginal() {
        return original;
    }

    public void undo() {
        if (mMotions.size() > 1) {
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
        if (mUndoneMotions.size() > 0) {
            mMorphMatrix.set(mUndoneMotions.remove(mUndoneMotions.size() - 1));
            mMotions.add(new MorphMatrix(mMorphMatrix));
            invalidate();
        }
    }

    public boolean isRedoActive() {
        return mUndoneMotions.size() > 0;
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
