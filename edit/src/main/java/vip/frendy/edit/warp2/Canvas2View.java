package vip.frendy.edit.warp2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vip.frendy.edit.colorful.ColorfulPath;
import vip.frendy.edit.colorful.ColorfulUtils;

public class Canvas2View extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener{

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

    private Bitmap bmBaseLayer;//原图

    private OnCanvasUpdatedListener mUpdatedListener;

    private Bitmap mBitmapSrc, mBitmap;
    private boolean attached = false;
    private boolean original = false;
    private boolean drawNoting = false;


    private int touch_x, touch_y;

    private List<Point> touchPoints = new ArrayList<>();

    private List<Point> cachedPoints = new ArrayList<>();

    private int mStrength = 10;

    private int radius = 30;

    public Canvas2View(Context context) {
        super(context);
        this.mContext = context;
        initDrawView();
    }

    public Canvas2View(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initDrawView();
    }

    public Canvas2View(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        //Log.i("eye", "x: " + touch_x + " y : " + touch_y + " dispatch");

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

        Log.i("eye", "layout, left: " + imageLeft + " top: " + imageTop + " right: " + imageRight + " bottom: " + imageBottom);
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
        mInitImageRect.set(imageLeft,imageTop,imageRight,imageBottom);

    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (drawNoting) {
            return;
        }

        if (!original) {
            if (mStrength != 0) {

                mBitmap = mBitmapSrc;

                for (Point point : touchPoints) {
                    mBitmap = ShapeUtils.enlarge(mBitmap, point.x, point.y, radius, mStrength);
                }

                if (mUpdatedListener != null) {
                    mUpdatedListener.OnCanvasUpdated();
                }
            }
            canvas.drawBitmap(mBitmap, null, mImageRect, null);
        } else {
            if (bmBaseLayer != null) {
                canvas.drawBitmap(bmBaseLayer, null, mImageRect, null);
            }
        }

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

                Point point = new Point();

                float ratio = (mImageRect.right - mImageRect.left) / (float) mImageWidth;
                point.x = (int) ((touch_x - mImageRect.left) / ratio);
                point.y = (int) ((touch_y - mImageRect.top) / ratio);

                Log.i("eye", "x: " + touch_x + " y : " + touch_y);

                touchPoints.add(point);

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
        requestLayout();
        invalidate();
    }


    public boolean reset() {
        this.mImageWidth = 0;
        this.mImageHeight = 0;
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
    }

    class Point {
        int x;
        int y;
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
        void OnEraserApplyed();
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
                Point lastPath = cachedPoints.get(cachedPoints.size() - 1);
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

    public void setStrength(int strength) {
        this.mStrength = strength;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Bitmap generateBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(this.mImageWidth, this.mImageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(mBitmap, 0.0F, 0.0F, (Paint)null);
        canvas.save();
        return bitmap;
    }

    public void drawNothing(boolean state) {
        this.drawNoting = state;
    }




}
